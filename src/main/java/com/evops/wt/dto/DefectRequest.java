package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DefectRequest {
    /** 缺陷编号；为空自动生成 */
    private String defectNo;
    @NotNull(message = "巡检工单ID不能为空")
    private Long inspectionId;
    @NotBlank(message = "叶片序列号不能为空")
    private String bladeCode;
    /** 关联无人机影像ID，可为空；提供时须归属该工单 */
    private Long imageId;
    @NotBlank(message = "缺陷等级不能为空")
    private String severity;
    private String description;
}
