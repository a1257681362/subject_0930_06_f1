package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BladeRequest {
    @NotBlank(message = "叶片序列号不能为空")
    private String bladeCode;
    @NotNull(message = "所属风机ID不能为空")
    private Long turbineId;
    @NotBlank(message = "方位不能为空")
    private String position;
    @NotBlank(message = "材料不能为空")
    private String material;
    /** 安装时间；为空时取当前时间 */
    private LocalDateTime installTime;
}
