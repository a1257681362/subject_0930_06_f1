package com.evops.wt.controller;

import com.evops.common.ApiResponse;
import com.evops.wt.dto.TurbineRequest;
import com.evops.wt.entity.Turbine;
import com.evops.wt.service.TurbineService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wt/turbines")
public class TurbineController {

    private final TurbineService turbineService;

    public TurbineController(TurbineService turbineService) {
        this.turbineService = turbineService;
    }

    /** 风机建档 */
    @PostMapping
    public ApiResponse<Turbine> create(@Valid @RequestBody TurbineRequest request) {
        return ApiResponse.ok(turbineService.create(request));
    }

    @GetMapping
    public ApiResponse<List<Turbine>> list() {
        return ApiResponse.ok(turbineService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Turbine> get(@PathVariable Long id) {
        return ApiResponse.ok(turbineService.getById(id));
    }
}
