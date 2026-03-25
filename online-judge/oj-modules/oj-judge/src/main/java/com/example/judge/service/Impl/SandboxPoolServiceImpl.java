package com.example.judge.service.Impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.judge.callback.DockerStartResultCallback;
import com.example.judge.callback.StatisticsCallback;
import com.example.judge.config.DockerSandBoxPool;
import com.example.judge.domain.CompileResult;
import com.example.judge.domain.SandBoxExecuteResult;
import com.example.judge.service.ISandboxPoolService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SandboxPoolServiceImpl implements ISandboxPoolService {

    @Autowired
    private DockerSandBoxPool sandBoxPool;

    @Autowired
    private DockerClient dockerClient;

    private String containerId;
    private String userCodeFileName;
    private String containerWorkDir;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        containerId = sandBoxPool.getContainer();
        containerWorkDir = sandBoxPool.getContainerWorkDir(containerId);

        // 创建本地代码文件
        createUserCodeFile(userCode);

        // 将代码复制到容器中
        copyCodeToContainer();

        // 编译代码
        CompileResult compileResult = compileCodeByDocker();
        if (!compileResult.isCompiled()) {
            sandBoxPool.returnContainer(containerId);
            deleteUserCodeFile();
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }

        // 执行代码
        return executeJavaCodeByDocker(inputList);
    }

    private void createUserCodeFile(String userCode) {
        String codeDir = sandBoxPool.getCodeDir(containerId);
        log.info("本地代码目录：{}", codeDir);
        userCodeFileName = codeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;

        if (FileUtil.exist(userCodeFileName)) {
            FileUtil.del(userCodeFileName);
        }
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
        log.info("创建本地代码文件: {}", userCodeFileName);
    }

    /**
     * 将代码复制到容器中
     */
    private void copyCodeToContainer() {
        try {
            // 在容器中创建工作目录
            ExecCreateCmdResponse mkdirResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "mkdir -p " + containerWorkDir)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();
            dockerClient.execStartCmd(mkdirResponse.getId()).exec(new DockerStartResultCallback()).awaitCompletion();

            // 复制代码文件到容器
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(userCodeFileName)
                    .withRemotePath(containerWorkDir + "/")
                    .exec();

            log.info("代码已复制到容器: {}/{}", containerWorkDir, JudgeConstants.USER_CODE_JAVA_CLASS_NAME);
        } catch (Exception e) {
            log.error("复制代码到容器失败", e);
            throw new RuntimeException("复制代码到容器失败: " + e.getMessage(), e);
        }
    }

    private CompileResult compileCodeByDocker() {
        // 在容器内执行编译，使用容器内的工作目录
        String[] compileCmd = {"sh", "-c", "cd " + containerWorkDir + " && javac " + JudgeConstants.USER_CODE_JAVA_CLASS_NAME};
        String cmdId = createExecCmd(compileCmd, null, containerId);

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
            throw new RuntimeException(e);
        }
    }

    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        List<String> outList = new ArrayList<>();
        long maxMemory = 0L;
        long maxUseTime = 0L;

        String className = JudgeConstants.USER_CODE_JAVA_CLASS_NAME.replace(".java", "");

        for (String inputArgs : inputList) {
            // 在容器内执行Java程序
            String[] execCmd;
            if (StrUtil.isEmpty(inputArgs)) {
                execCmd = new String[]{"sh", "-c", "cd " + containerWorkDir + " && java " + className};
            } else {
                execCmd = new String[]{"sh", "-c", "cd " + containerWorkDir + " && java " + className + " " + inputArgs};
            }

            String cmdId = createExecCmd(execCmd, null, containerId);

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
                    return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
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

            outList.add(resultCallback.getMessage().trim());
        }

        sandBoxPool.returnContainer(containerId);
        deleteUserCodeFile();

        return getSanBoxResult(inputList, outList, maxMemory, maxUseTime);
    }

    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }

    private SandBoxExecuteResult getSanBoxResult(List<String> inputList, List<String> outList,
                                                 long maxMemory, long maxUseTime) {
        if (inputList.size() != outList.size()) {
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }

    private void deleteUserCodeFile() {
        FileUtil.del(userCodeFileName);
    }
}