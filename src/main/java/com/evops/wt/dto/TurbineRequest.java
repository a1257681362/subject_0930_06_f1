package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class TurbineRequest {
    @NotBlank(message = "风机编号不能为空")
    private String turbineCode;
    @NotBlank(message = "场址不能为空")
    private String siteName;
    @NotBlank(message = "机型不能为空")
    private String model;
    private LocalDateTime gridTime;
    private String remark;
}
