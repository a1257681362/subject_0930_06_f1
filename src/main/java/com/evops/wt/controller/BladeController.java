package com.evops.wt.controller;

import com.evops.common.ApiResponse;
import com.evops.wt.dto.BladeReplaceRequest;
import com.evops.wt.dto.BladeRequest;
import com.evops.wt.entity.Blade;
import com.evops.wt.entity.BladeReplacement;
import com.evops.wt.service.BladeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wt/blades")
public class BladeController {

    private final BladeService bladeService;

    public BladeController(BladeService bladeService) {
        this.bladeService = bladeService;
    }

    /** 叶片建档 */
    @PostMapping
    public ApiResponse<Blade> create(@Valid @RequestBody BladeRequest request) {
        return ApiResponse.ok(bladeService.create(request));
    }

    /** 换件：同事务关闭旧叶区间、建立新叶、保留前后关系 */
    @PostMapping("/replacements")
    public ApiResponse<BladeReplacement> replace(@Valid @RequestBody BladeReplaceRequest request) {
        return ApiResponse.ok(bladeService.replace(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Blade> get(@PathVariable Long id) {
        return ApiResponse.ok(bladeService.getById(id));
    }

    /**
     * 临界时点归属：查询某风机在指定时刻实际在役的叶片。
     * 与巡检/缺陷登记使用同一半开区间口径 [install, remove)。
     */
    @GetMapping("/at-time")
    public ApiResponse<List<Blade>> atTime(@RequestParam Long turbineId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime atTime) {
        return ApiResponse.ok(bladeService.listAtTime(turbineId, atTime));
    }

    /** 风机下全部叶片（含已拆除）及其换件前后关系 */
    @GetMapping("/by-turbine/{turbineId}")
    public ApiResponse<Map<String, Object>> byTurbine(@PathVariable Long turbineId) {
        Map<String, Object> result = new HashMap<>();
        result.put("blades", bladeService.listByTurbine(turbineId));
        result.put("replacements", bladeService.listReplacementsWithCode(turbineId));
        return ApiResponse.ok(result);
    }
}
