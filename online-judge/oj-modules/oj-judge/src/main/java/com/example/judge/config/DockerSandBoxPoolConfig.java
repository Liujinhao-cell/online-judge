package com.example.judge.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerSandBoxPoolConfig {
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

    @Value("${sandbox.docker.image:openjdk:8-jdk-alpine}")
    private String sandboxImage;

    @Value("${sandbox.docker.workDir:/usr/share/java}")
    private String workDir;

    @Value("${sandbox.docker.pool.size:4}")
    private int poolSize;

    @Value("${sandbox.docker.name-prefix:oj-sandbox-jdk}")
    private String containerNamePrefix;

    // 删除这个字段，不再使用
    // private DockerClient dockerClient;

    @Bean
    public DockerClient createDockerClient() {
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

        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig)
                .withDockerHttpClient(httpClient)
                .build();

        return dockerClient;
    }

    @Bean
    public DockerSandBoxPool createDockerSandBoxPool(DockerClient dockerClient) {
        DockerSandBoxPool dockerSandBoxPool = new DockerSandBoxPool(
                dockerClient,
                sandboxImage,
                workDir,
                memoryLimit,
                memorySwapLimit,
                cpuLimit,
                poolSize,
                containerNamePrefix
        );
        dockerSandBoxPool.initDockerPool(poolSize);
        return dockerSandBoxPool;
    }
}
