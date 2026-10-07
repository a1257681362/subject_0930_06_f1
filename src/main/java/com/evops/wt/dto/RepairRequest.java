package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RepairRequest {
    @NotNull(message = "维修时间不能为空")
    private LocalDateTime repairTime;
    @NotBlank(message = "维修人不能为空")
    private String repairer;
    private String repairResult;
}
