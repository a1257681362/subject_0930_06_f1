package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风机档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_turbine")
public class Turbine extends BaseEntity {
    /** 风机编号，全局唯一 */
    private String turbineCode;
    /** 场址 */
    private String siteName;
    /** 机型 */
    private String model;
    /** 并网时间 */
    private LocalDateTime gridTime;
    /** 状态：RUNNING 运行中 / STOPPED 停机 / DISMANTLED 拆除 */
    private String status;
    private String remark;
}
