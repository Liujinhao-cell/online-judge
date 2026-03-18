package com.example.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.common.core.constants.Constants;
import com.example.common.core.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("========== MyMetaObjectHandler.insertFill 开始执行 ==========");

        // 获取当前用户ID
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        log.info("从 ThreadLocal 获取到的用户ID: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        log.info("当前时间: {}", now);

        // 检查实体类中是否有这些字段
        boolean hasCreateBy = metaObject.hasSetter("createBy");
        boolean hasCreateTime = metaObject.hasSetter("createTime");
        boolean hasUpdateBy = metaObject.hasSetter("updateBy");
        boolean hasUpdateTime = metaObject.hasSetter("updateTime");

        log.info("字段存在性 - createBy: {}, createTime: {}, updateBy: {}, updateTime: {}",
                hasCreateBy, hasCreateTime, hasUpdateBy, hasUpdateTime);

        if (userId != null) {
            // 严格填充，如果字段有值则不会覆盖
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
            log.info("插入填充完成");
        } else {
            log.warn("用户ID为null，无法填充审计字段");
            // 可以设置默认值或抛出异常
        }

        log.info("========== MyMetaObjectHandler.insertFill 执行完成 ==========");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("========== MyMetaObjectHandler.updateFill 开始执行 ==========");

        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        log.info("从 ThreadLocal 获取到的用户ID: {}", userId);

        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            log.info("更新填充完成");
        }

        log.info("========== MyMetaObjectHandler.updateFill 执行完成 ==========");
    }
}
