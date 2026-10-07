package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 巡检工单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_inspection")
public class Inspection extends BaseEntity {
    /** 工单编号，全局唯一 */
    private String orderNo;
    private Long turbineId;
    /** 被巡检叶片；为空表示整机组/待分配 */
    private Long bladeId;
    private LocalDateTime inspectTime;
    private String inspector;
    /** 状态：PENDING 待检 / DONE 已完成 / CANCELED 已取消 */
    private String status;
    private String remark;
}
