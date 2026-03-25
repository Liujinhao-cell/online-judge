package com.example.judge.service.Impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.judge.callback.DockerStartResultCallback;
import com.example.judge.callback.StatisticsCallback;
import com.example.judge.domain.CompileResult;
import com.example.judge.domain.SandBoxExecuteResult;
import com.example.judge.service.ISandboxService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SandboxServiceImpl implements ISandboxService {

    @Value("${sandbox.docker.host:tcp://47.118.20.7:2375}")
    private String dockerHost;

    @Value("${sandbox.limit.memory:256000000}")
    private Long memoryLimit;

    @Value("${sandbox.limit.memory-swap:256000000}")
    private Long memorySwapLimit;

    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    private String containerId;
    private DockerClient dockerClient;
    private String userCodeDir;
    private String userCodeFileName;

    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        try {
            createUserCodeFile(userId, userCode);
            initDockerSandBox();

            // 将代码复制到容器中
            copyCodeToContainer();

            // 编译代码
            CompileResult compileResult = compileCodeByDocker();
            if (!compileResult.isCompiled()) {
                deleteContainer();
                deleteUserCodeFile();
                return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
            }

            // 执行代码
            return executeJavaCodeByDocker(inputList);
        } catch (Exception e) {
            log.error("判题执行异常", e);
            deleteContainer();
            deleteUserCodeFile();
            return SandBoxExecuteResult.fail(CodeRunStatus.UNKNOWN_FAILED, e.getMessage());
        }
    }

    private void createUserCodeFile(Long userId, String userCode) {
        String examCodeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.EXAM_CODE_DIR;
        if (!FileUtil.exist(examCodeDir)) {
            FileUtil.mkdir(examCodeDir);
        }
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        userCodeDir = examCodeDir + File.separator + userId + Constants.UNDERLINE_SEPARATOR + time;
        userCodeFileName = userCodeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
        log.info("创建用户代码文件: {}", userCodeFileName);
    }

    /**
     * 将代码文件复制到容器中
     */
    private void copyCodeToContainer() {
        try {
            // 创建容器内的目标目录
            ExecCreateCmdResponse mkdirResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "mkdir -p " + JudgeConstants.DOCKER_USER_CODE_DIR)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();
            dockerClient.execStartCmd(mkdirResponse.getId()).exec(new DockerStartResultCallback()).awaitCompletion();

            // 使用 copyToContainer 方法（新版 API）
            try (FileInputStream fis = new FileInputStream(userCodeFileName)) {
                dockerClient.copyArchiveToContainerCmd(containerId)
                        .withHostResource(userCodeFileName)
                        .withRemotePath(JudgeConstants.DOCKER_USER_CODE_DIR + "/")
                        .exec();
            }
            log.info("代码已复制到容器: {}", JudgeConstants.DOCKER_USER_CODE_DIR + "/" + JudgeConstants.USER_CODE_JAVA_CLASS_NAME);
        } catch (Exception e) {
            log.error("复制代码到容器失败", e);
            throw new RuntimeException("复制代码到容器失败: " + e.getMessage(), e);
        }
    }

    private void initDockerSandBox() {
        // 配置 Docker 客户端（新版 API）
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(false)
                .build();

        // 使用 ApacheDockerHttpClient 替代已弃用的 NettyDockerCmdExecFactory
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.getDockerHost())
                .sslConfig(clientConfig.getSSLConfig())
                .maxConnections(100)
                .build();

        dockerClient = DockerClientBuilder.getInstance(clientConfig)
                .withDockerHttpClient(httpClient)
                .build();

        // 拉取镜像
        pullJavaEnvImage();

        // 创建容器
        HostConfig hostConfig = getHostConfig();
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(JudgeConstants.JAVA_CONTAINER_NAME + System.currentTimeMillis());

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .withWorkingDir(JudgeConstants.DOCKER_USER_CODE_DIR)
                .exec();

        containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();
        log.info("容器创建并启动成功, containerId: {}", containerId);
    }

    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && JudgeConstants.JAVA_ENV_IMAGE.equals(repoTags[0])) {
                log.info("镜像已存在: {}", JudgeConstants.JAVA_ENV_IMAGE);
                return;
            }
        }

        log.info("开始拉取镜像: {}", JudgeConstants.JAVA_ENV_IMAGE);
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
            log.info("镜像拉取完成");
        } catch (InterruptedException e) {
            log.error("拉取镜像失败", e);
            throw new RuntimeException(e);
        }
    }

    private HostConfig getHostConfig() {
        HostConfig hostConfig = new HostConfig();

        // 限制 docker 容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");
        hostConfig.withReadonlyRootfs(false);
        hostConfig.withAutoRemove(true);

        return hostConfig;
    }

    private CompileResult compileCodeByDocker() {
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);

        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        CompileResult compileResult = new CompileResult();
        try {
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    .awaitCompletion();

            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
                log.error("编译失败: {}", resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
                log.info("编译成功");
            }
            return compileResult;
        } catch (InterruptedException e) {
            log.error("编译过程异常", e);
            throw new RuntimeException(e);
        }
    }

    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        List<String> outList = new ArrayList<>();
        long maxMemory = 0L;
        long maxUseTime = 0L;

        for (int i = 0; i < inputList.size(); i++) {
            String inputArgs = inputList.get(i);
            log.debug("执行测试用例 {}: 输入参数: {}", i + 1, inputArgs);

            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);

            StopWatch stopWatch = new StopWatch();
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            stopWatch.start();

            DockerStartResultCallback resultCallback = new DockerStartResultCallback();
            try {
                dockerClient.execStartCmd(cmdId)
                        .exec(resultCallback)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);

                if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                    log.warn("执行失败: {}", resultCallback.getErrorMessage());
                    return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
                log.error("执行过程异常", e);
                throw new RuntimeException(e);
            }

            stopWatch.stop();
            statsCmd.close();

            long userTime = stopWatch.getLastTaskTimeMillis();
            maxUseTime = Math.max(userTime, maxUseTime);
            Long memory = statisticsCallback.getMaxMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }

            String output = resultCallback.getMessage().trim();
            outList.add(output);
            log.debug("执行结果: {}", output);
        }

        deleteContainer();
        deleteUserCodeFile();

        return getSanBoxResult(inputList, outList, maxMemory, maxUseTime);
    }

    private SandBoxExecuteResult getSanBoxResult(List<String> inputList, List<String> outList,
                                                 long maxMemory, long maxUseTime) {
        if (inputList.size() != outList.size()) {
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }

    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        String[] finalCmd = javaCmdArr;
        if (!StrUtil.isEmpty(inputArgs)) {
            String[] inputArray = inputArgs.split(" ");
            finalCmd = ArrayUtil.append(javaCmdArr, inputArray);
        }

        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(finalCmd)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }

    private void deleteContainer() {
        if (dockerClient != null && containerId != null) {
            try {
                dockerClient.stopContainerCmd(containerId).exec();
                dockerClient.removeContainerCmd(containerId).exec();
                dockerClient.close();
                log.info("容器已删除: {}", containerId);
            } catch (Exception e) {
                log.warn("删除容器失败: {}", e.getMessage());
            }
        }
    }

    private void deleteUserCodeFile() {
        try {
            FileUtil.del(userCodeFileName);
            FileUtil.del(userCodeDir);
            log.info("用户代码文件已删除: {}", userCodeFileName);
        } catch (Exception e) {
            log.warn("删除用户代码文件失败: {}", e.getMessage());
        }
    }
}