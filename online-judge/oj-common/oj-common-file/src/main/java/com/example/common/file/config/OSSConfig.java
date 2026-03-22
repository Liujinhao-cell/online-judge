package com.example.common.file.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(OSSProperties.class)
public class OSSConfig {

    @Autowired
    private OSSProperties prop;

    public OSS ossClient;

    @Bean
    public OSS ossClient() {
        // 详细打印配置信息
        log.info("=== OSS配置信息开始 ===");
        log.info("endpoint: {}", prop.getEndpoint());
        log.info("region: {}", prop.getRegion());
        log.info("accessKeyId: {}", prop.getAccessKeyId());
        log.info("accessKeySecret: {}", prop.getAccessKeySecret() != null ? "已配置(长度:" + prop.getAccessKeySecret().length() + ")" : "null");
        log.info("bucketName: {}", prop.getBucketName());
        log.info("pathPrefix: {}", prop.getPathPrefix());
        log.info("=== OSS配置信息结束 ===");

        // 检查配置
        if (prop.getAccessKeyId() == null || prop.getAccessKeyId().trim().isEmpty()) {
            log.error("OSS AccessKeyId 为空或null");
            throw new RuntimeException("OSS AccessKeyId 未配置");
        }

        if (prop.getAccessKeySecret() == null || prop.getAccessKeySecret().trim().isEmpty()) {
            log.error("OSS AccessKeySecret 为空或null");
            throw new RuntimeException("OSS AccessKeySecret 未配置");
        }
        try {
            DefaultCredentialProvider credentialsProvider =
                    CredentialsProviderFactory.newDefaultCredentialProvider(
                            prop.getAccessKeyId(),
                            prop.getAccessKeySecret()
                    );

            // 创建ClientBuilderConfiguration
            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

            // 使⽤内⽹endpoint进⾏上传
            ossClient = OSSClientBuilder.create()
                    .endpoint(prop.getEndpoint())
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(prop.getRegion())
                    .build();

            log.info("OSS client initialized successfully");
            return ossClient;

        } catch (Exception e) {
            log.error("Failed to create OSS client", e);
            throw new RuntimeException("OSS client creation failed", e);
        }
    }

    @PreDestroy
    public void closeOSSClient() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS client closed successfully");
        }
    }
}
