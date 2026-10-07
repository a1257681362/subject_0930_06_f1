package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维修结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_repair")
public class Repair extends BaseEntity {
    private Long defectId;
    private LocalDateTime repairTime;
    private String repairer;
    private String repairResult;
}
