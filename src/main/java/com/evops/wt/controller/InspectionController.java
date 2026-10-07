package com.evops.wt.controller;

import com.evops.common.ApiResponse;
import com.evops.wt.dto.DroneImageRequest;
import com.evops.wt.dto.InspectionRequest;
import com.evops.wt.entity.DroneImage;
import com.evops.wt.entity.Inspection;
import com.evops.wt.service.InspectionService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wt/inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    /** 创建巡检工单 */
    @PostMapping
    public ApiResponse<Inspection> create(@Valid @RequestBody InspectionRequest request) {
        return ApiResponse.ok(inspectionService.create(request));
    }

    /** 关联无人机影像 */
    @PostMapping("/{id}/images")
    public ApiResponse<DroneImage> addImage(@PathVariable("id") Long id,
                                            @Valid @RequestBody DroneImageRequest request) {
        return ApiResponse.ok(inspectionService.addImage(id, request));
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<DroneImage>> images(@PathVariable("id") Long id) {
        return ApiResponse.ok(inspectionService.listImages(id));
    }

    /** 工单完成 */
    @PostMapping("/{id}/complete")
    public ApiResponse<Inspection> complete(@PathVariable("id") Long id) {
        return ApiResponse.ok(inspectionService.complete(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Inspection> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(inspectionService.getById(id));
    }

    @GetMapping("/by-turbine/{turbineId}")
    public ApiResponse<List<Inspection>> byTurbine(@PathVariable Long turbineId) {
        return ApiResponse.ok(inspectionService.listByTurbine(turbineId));
    }
}
