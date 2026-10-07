package com.evops.wt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class DroneImageRequest {
    @NotBlank(message = "影像地址不能为空")
    private String imageUrl;
    private LocalDateTime shootTime;
    private String locationDesc;
}
