package com.evops.wt.controller;

import com.evops.common.ApiResponse;
import com.evops.wt.dto.AcceptanceRequest;
import com.evops.wt.dto.DefectRequest;
import com.evops.wt.dto.RepairRequest;
import com.evops.wt.entity.Acceptance;
import com.evops.wt.entity.Defect;
import com.evops.wt.entity.Repair;
import com.evops.wt.service.DefectService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wt/defects")
public class DefectController {

    private final DefectService defectService;

    public DefectController(DefectService defectService) {
        this.defectService = defectService;
    }

    /** 缺陷登记（按巡检时点校验叶片在役归属） */
    @PostMapping
    public ApiResponse<Defect> register(@Valid @RequestBody DefectRequest request) {
        return ApiResponse.ok(defectService.register(request));
    }

    /** 登记维修结果 */
    @PostMapping("/{id}/repairs")
    public ApiResponse<Repair> repair(@PathVariable Long id, @Valid @RequestBody RepairRequest request) {
        return ApiResponse.ok(defectService.repair(id, request));
    }

    /** 验收：PASS 通过 / REJECT 驳回 */
    @PostMapping("/{id}/acceptance")
    public ApiResponse<Acceptance> accept(@PathVariable Long id, @Valid @RequestBody AcceptanceRequest request) {
        return ApiResponse.ok(defectService.accept(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Defect> get(@PathVariable Long id) {
        return ApiResponse.ok(defectService.getById(id));
    }

    @GetMapping("/{id}/repairs")
    public ApiResponse<List<Repair>> repairs(@PathVariable Long id) {
        return ApiResponse.ok(defectService.listRepairs(id));
    }

    @GetMapping("/{id}/acceptance")
    public ApiResponse<List<Acceptance>> acceptance(@PathVariable Long id) {
        return ApiResponse.ok(defectService.listAcceptances(id));
    }

    @GetMapping("/by-inspection/{inspectionId}")
    public ApiResponse<List<Defect>> byInspection(@PathVariable Long inspectionId) {
        return ApiResponse.ok(defectService.listByInspection(inspectionId));
    }

    @GetMapping("/by-blade/{bladeId}")
    public ApiResponse<List<Defect>> byBlade(@PathVariable Long bladeId) {
        return ApiResponse.ok(defectService.listByBlade(bladeId));
    }
}
