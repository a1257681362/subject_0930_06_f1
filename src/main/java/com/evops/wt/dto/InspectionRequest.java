package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class InspectionRequest {
    /** 工单编号；为空自动生成 */
    private String orderNo;
    @NotNull(message = "风机ID不能为空")
    private Long turbineId;
    /** 被巡检叶片序列号；提供时校验其在巡检时点在役且归属该风机 */
    private String bladeCode;
    @NotNull(message = "巡检时间不能为空")
    private LocalDateTime inspectTime;
    @NotBlank(message = "巡检人不能为空")
    private String inspector;
    private String remark;
}
