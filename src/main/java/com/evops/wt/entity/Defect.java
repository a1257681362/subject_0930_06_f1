package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 缺陷登记。
 * 状态机：REGISTERED 已登记 -> REPAIRED 已维修 -> ACCEPTED 已验收；
 * 验收驳回进入 REJECTED，可再次登记维修结果回到 REPAIRED。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_defect")
public class Defect extends BaseEntity {
    /** 缺陷编号，全局唯一 */
    private String defectNo;
    private Long inspectionId;
    private Long bladeId;
    /** 关联的无人机影像，可为空 */
    private Long imageId;
    /** 缺陷等级：LOW / MEDIUM / HIGH / CRITICAL */
    private String severity;
    private String description;
    /** REGISTERED / REPAIRED / ACCEPTED / REJECTED */
    private String status;
}
