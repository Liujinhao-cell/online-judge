package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,           // 排除数据源自动配置
        DataSourceTransactionManagerAutoConfiguration.class,// 排除事务管理器
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class //mybatis-plus
        // HibernateJpaAutoConfiguration.class       // 如果有 JPA 相关也排除
})
@EnableDiscoveryClient  // 启用服务发现（Nacos）
@ComponentScan(basePackages = {
        "com.example.gateway",
        "com.example.common.redis"
})
public class OjGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjGatewayApplication.class, args);
    }
}