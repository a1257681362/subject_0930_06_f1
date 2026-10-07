package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AcceptanceRequest {
    @NotNull(message = "验收时间不能为空")
    private LocalDateTime acceptTime;
    @NotBlank(message = "验收人不能为空")
    private String acceptor;
    /** PASS / REJECT */
    @NotBlank(message = "验收结论不能为空")
    private String result;
    private String comment;
}
