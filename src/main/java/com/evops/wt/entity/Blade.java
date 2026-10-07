package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 叶片档案。
 * turbineId 为资产归属，建档后不因换件改变；
 * 在役时点由半开区间 [installTime, removeTime) 表达，removeTime 为 NULL 表示在役至今。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_blade")
public class Blade extends BaseEntity {
    /** 叶片序列号，全局唯一 */
    private String bladeCode;
    /** 所属风机（资产归属口径） */
    private Long turbineId;
    /** 方位：A/B/C */
    private String position;
    /** 材料，如 玻纤增强环氧树脂 */
    private String material;
    /** 安装时间（在役区间起点） */
    private LocalDateTime installTime;
    /** 拆除时间（在役区间终点，NULL 表示至今） */
    private LocalDateTime removeTime;
    /** 状态：INSTALLED 在役 / REMOVED 已拆除 */
    private String status;
}
