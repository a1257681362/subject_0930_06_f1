package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 换件记录：保留同方位新旧叶片的前后关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_blade_replacement")
public class BladeReplacement extends BaseEntity {
    private Long turbineId;
    /** 方位：A/B/C */
    private String position;
    /** 换下的旧叶片 */
    private Long oldBladeId;
    /** 换上的新叶片 */
    private Long newBladeId;
    /** 换件时刻（旧叶在役区间止点、新叶在役区间起点） */
    private LocalDateTime replaceTime;
    private String reason;
}
