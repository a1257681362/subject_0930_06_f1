package com.evops.wt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.wt.dto.TurbineRequest;
import com.evops.wt.entity.Turbine;
import com.evops.wt.mapper.TurbineMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TurbineService {

    private final TurbineMapper turbineMapper;

    public TurbineService(TurbineMapper turbineMapper) {
        this.turbineMapper = turbineMapper;
    }

    public Turbine create(TurbineRequest request) {
        Long count = turbineMapper.selectCount(
                new QueryWrapper<Turbine>().eq("turbine_code", request.getTurbineCode()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("风机编号已存在：" + request.getTurbineCode());
        }
        Turbine turbine = new Turbine();
        turbine.setTurbineCode(request.getTurbineCode());
        turbine.setSiteName(request.getSiteName());
        turbine.setModel(request.getModel());
        turbine.setGridTime(request.getGridTime());
        turbine.setStatus("RUNNING");
        turbine.setRemark(request.getRemark());
        turbineMapper.insert(turbine);
        return turbine;
    }

    public Turbine getById(Long id) {
        Turbine turbine = turbineMapper.selectById(id);
        if (turbine == null) {
            throw new IllegalArgumentException("风机不存在：" + id);
        }
        return turbine;
    }

    public List<Turbine> list() {
        return turbineMapper.selectList(new QueryWrapper<Turbine>().orderByDesc("create_time"));
    }
}
