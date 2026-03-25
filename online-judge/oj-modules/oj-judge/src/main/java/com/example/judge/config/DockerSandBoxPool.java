package com.example.judge.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.example.common.core.constants.JudgeConstants;
import com.example.judge.callback.DockerStartResultCallback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import lombok.extern.slf4j.Slf4j;
import com.github.dockerjava.api.model.Container;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class DockerSandBoxPool {
    private DockerClient dockerClient;
    private String sandboxImage;
    private String workDir;
    private Long memoryLimit;
    private Long memorySwapLimit;
    private Long cpuLimit;
    private int poolSize;
    private String containerNamePrefix;
    private BlockingQueue<String> containerQueue;
    private Map<String, String> containerNameMap;
    private Map<String, String> containerWorkDirMap;

    public DockerSandBoxPool(DockerClient dockerClient, String sandboxImage,
                             String workDir, Long memoryLimit,
                             Long memorySwapLimit, Long cpuLimit,
                             int poolSize, String containerNamePrefix) {
        this.dockerClient = dockerClient;
        this.sandboxImage = sandboxImage;
        this.workDir = workDir;
        this.memoryLimit = memoryLimit;
        this.memorySwapLimit = memorySwapLimit;
        this.cpuLimit = cpuLimit;
        this.poolSize = poolSize;
        this.containerQueue = new ArrayBlockingQueue<>(poolSize);
        this.containerNamePrefix = containerNamePrefix;
        this.containerNameMap = new HashMap<>();
        this.containerWorkDirMap = new HashMap<>();
    }

    public void initDockerPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            createContainer(containerNamePrefix + "-" + i);
        }
    }

    public String getContainer() {
        try {
            return containerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void returnContainer(String containerId) {
        containerQueue.add(containerId);
    }

    public String getContainerWorkDir(String containerId) {
        return containerWorkDirMap.get(containerId);
    }

    /**
     * 等待容器完全启动
     */
    private void waitForContainerRunning(String containerId) {
        int maxRetries = 30;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                com.github.dockerjava.api.command.InspectContainerResponse inspectResponse =
                        dockerClient.inspectContainerCmd(containerId).exec();
                String state = inspectResponse.getState().getStatus();
                log.debug("检查容器状态: containerId={}, state={}", containerId, state);

                if ("running".equals(state)) {
                    log.info("容器已启动: {}", containerId);
                    return;
                }

                Thread.sleep(1000);
                retryCount++;
            } catch (Exception e) {
                log.warn("检查容器状态失败: {}", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
                retryCount++;
            }
        }
        throw new RuntimeException("容器启动超时: " + containerId);
    }

    private void createContainer(String containerName) {
        String containerWorkDir = "/tmp/" + containerName;

        // 检查容器是否已存在
        List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
        boolean containerExists = false;

        if (!CollectionUtil.isEmpty(containerList)) {
            for (Container container : containerList) {
                String[] containerNames = container.getNames();
                if (containerNames != null && containerNames.length > 0) {
                    String actualContainerName = containerNames[0].startsWith("/")
                            ? containerNames[0].substring(1)
                            : containerNames[0];

                    if (containerName.equals(actualContainerName)) {
                        String containerId = container.getId();
                        String state = container.getState();

                        log.info("发现已存在的容器: {}, 状态: {}", containerName, state);

                        // 如果容器未运行，启动它
                        if (!"running".equals(state)) {
                            dockerClient.startContainerCmd(containerId).exec();
                            log.info("启动容器: {}", containerName);
                            // 等待容器完全启动
                            waitForContainerRunning(containerId);
                        }

                        // 等待2秒确保容器完全就绪
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // 确保容器内的工作目录存在
                        try {
                            ExecCreateCmdResponse mkdirResponse = dockerClient.execCreateCmd(containerId)
                                    .withCmd("sh", "-c", "mkdir -p " + containerWorkDir)
                                    .withAttachStderr(true)
                                    .withAttachStdout(true)
                                    .exec();
                            dockerClient.execStartCmd(mkdirResponse.getId()).exec(new DockerStartResultCallback()).awaitCompletion();
                            log.info("容器 {} 工作目录已就绪: {}", containerName, containerWorkDir);
                        } catch (Exception e) {
                            log.error("创建工作目录失败: {}", e.getMessage());
                            // 如果失败，删除容器重新创建
                            log.info("尝试删除并重新创建容器: {}", containerName);
                            try {
                                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                            } catch (Exception ex) {
                                log.error("删除容器失败: {}", ex.getMessage());
                            }
                            containerExists = false;
                            break;
                        }

                        containerQueue.add(containerId);
                        containerNameMap.put(containerId, containerName);
                        containerWorkDirMap.put(containerId, containerWorkDir);
                        containerExists = true;
                        break;
                    }
                }
            }
        }

        if (containerExists) {
            return;
        }

        // 创建新容器
        try {
            // 拉取镜像
            pullJavaEnvImage();

            // 创建容器配置
            HostConfig hostConfig = new HostConfig();
            hostConfig.withMemory(memoryLimit);
            hostConfig.withMemorySwap(memorySwapLimit);
            hostConfig.withCpuCount(cpuLimit);
            hostConfig.withNetworkMode("none");
            hostConfig.withReadonlyRootfs(false);
            hostConfig.withAutoRemove(true);

            CreateContainerCmd containerCmd = dockerClient
                    .createContainerCmd(sandboxImage)
                    .withName(containerName)
                    .withHostConfig(hostConfig)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .withWorkingDir(workDir);

            CreateContainerResponse createContainerResponse = containerCmd.exec();
            String containerId = createContainerResponse.getId();

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器创建并启动: {}, containerId: {}", containerName, containerId);

            // 等待容器完全启动
            waitForContainerRunning(containerId);

            // 等待2秒确保容器完全就绪
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 在容器内创建工作目录
            ExecCreateCmdResponse mkdirResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "mkdir -p " + containerWorkDir)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();
            dockerClient.execStartCmd(mkdirResponse.getId()).exec(new DockerStartResultCallback()).awaitCompletion();
            log.info("容器 {} 工作目录创建成功: {}", containerName, containerWorkDir);

            containerQueue.add(containerId);
            containerNameMap.put(containerId, containerName);
            containerWorkDirMap.put(containerId, containerWorkDir);

        } catch (InterruptedException e) {
            log.error("创建容器失败", e);
            throw new RuntimeException(e);
        }
    }

    public String getCodeDir(String containerId) {
        String containerName = containerNameMap.get(containerId);
        log.info("containerName：{}", containerName);
        return System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL + File.separator + containerName;
    }

    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && sandboxImage.equals(repoTags[0])) {
                log.info("镜像已存在: {}", sandboxImage);
                return;
            }
        }

        log.info("开始拉取镜像: {}", sandboxImage);
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(sandboxImage);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
            log.info("镜像拉取完成: {}", sandboxImage);
        } catch (InterruptedException e) {
            log.error("拉取镜像失败: {}", sandboxImage, e);
            throw new RuntimeException(e);
        }
    }

    public void destroyPool() {
        log.info("开始销毁容器池");
        while (!containerQueue.isEmpty()) {
            String containerId = containerQueue.poll();
            if (containerId != null) {
                try {
                    dockerClient.stopContainerCmd(containerId).exec();
                    dockerClient.removeContainerCmd(containerId).exec();
                    log.info("销毁容器: {}", containerId);
                } catch (Exception e) {
                    log.error("销毁容器失败: {}", containerId, e);
                }
            }
        }
    }
}