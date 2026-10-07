package com.evops.wt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 无人机影像，关联巡检工单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wt_drone_image")
public class DroneImage extends BaseEntity {
    private Long inspectionId;
    /** 影像地址（对象存储 key 或 URL） */
    private String imageUrl;
    private LocalDateTime shootTime;
    /** 拍摄部位描述，如 叶面距根部 12m */
    private String locationDesc;
}
