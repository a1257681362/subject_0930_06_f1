package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BladeReplaceRequest {
    @NotNull(message = "风机ID不能为空")
    private Long turbineId;
    @NotBlank(message = "方位不能为空")
    private String position;
    @NotBlank(message = "旧叶片序列号不能为空")
    private String oldBladeCode;
    @NotBlank(message = "新叶片序列号不能为空")
    private String newBladeCode;
    @NotBlank(message = "新叶片材料不能为空")
    private String newMaterial;
    /** 换件时刻，临界时间口径以此为准；为空取当前时间 */
    private LocalDateTime replaceTime;
    private String reason;
}
