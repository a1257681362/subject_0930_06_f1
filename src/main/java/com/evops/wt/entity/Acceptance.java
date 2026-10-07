package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 验收结论
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_acceptance")
public class Acceptance extends BaseEntity {
    private Long defectId;
    private LocalDateTime acceptTime;
    private String acceptor;
    /** PASS 通过 / REJECT 驳回 */
    private String result;
    private String comment;
}
