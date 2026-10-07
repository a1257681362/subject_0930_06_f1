package com.evops.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充。当前骨架未接用户体系，操作人统一记 0（system）。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final Long SYSTEM_USER = 0L;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createBy", Long.class, SYSTEM_USER);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", Long.class, SYSTEM_USER);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateBy", Long.class, SYSTEM_USER);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
