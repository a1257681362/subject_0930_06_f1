package com.evops.wt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.wt.dto.BladeReplaceRequest;
import com.evops.wt.dto.BladeRequest;
import com.evops.wt.entity.Blade;
import com.evops.wt.entity.BladeReplacement;
import com.evops.wt.mapper.BladeMapper;
import com.evops.wt.mapper.BladeReplacementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BladeService {

    private static final List<String> POSITIONS = Arrays.asList("A", "B", "C");

    private final BladeMapper bladeMapper;
    private final BladeReplacementMapper replacementMapper;
    private final TurbineService turbineService;

    public BladeService(BladeMapper bladeMapper,
                        BladeReplacementMapper replacementMapper,
                        TurbineService turbineService) {
        this.bladeMapper = bladeMapper;
        this.replacementMapper = replacementMapper;
        this.turbineService = turbineService;
    }

    /**
     * 叶片建档。
     */
    @Transactional
    public Blade create(BladeRequest request) {
        validatePosition(request.getPosition());
        turbineService.getById(request.getTurbineId());
        assertBladeCodeUnique(request.getBladeCode(), null);

        LocalDateTime installTime = request.getInstallTime() == null ? LocalDateTime.now() : request.getInstallTime();
        Blade blade = new Blade();
        blade.setBladeCode(request.getBladeCode());
        blade.setTurbineId(request.getTurbineId());
        blade.setPosition(request.getPosition());
        blade.setMaterial(request.getMaterial());
        blade.setInstallTime(installTime);
        blade.setRemoveTime(null);
        blade.setStatus("INSTALLED");
        bladeMapper.insert(blade);
        return blade;
    }

    /**
     * 换件：同一事务内关闭旧叶片在役区间、建立新叶片档案，并写入换件前后关系。
     * 口径：换件时刻 T 为半开区间分界，旧叶 [.., T)、新叶 [T, ..)，
     * 即临界时刻 T 旧叶已不在役、新叶已在役，二者不重叠、不留空。
     */
    @Transactional
    public BladeReplacement replace(BladeReplaceRequest request) {
        validatePosition(request.getPosition());
        turbineService.getById(request.getTurbineId());
        assertBladeCodeUnique(request.getNewBladeCode(), null);

        LocalDateTime replaceTime = request.getReplaceTime() == null ? LocalDateTime.now() : request.getReplaceTime();

        Blade oldBlade = getByCode(request.getOldBladeCode());
        if (!oldBlade.getTurbineId().equals(request.getTurbineId())) {
            throw new IllegalArgumentException("旧叶片不属于该风机，换件前后归属不一致");
        }
        if (!oldBlade.getPosition().equals(request.getPosition())) {
            throw new IllegalArgumentException("旧叶片方位与换件方位不一致：" + oldBlade.getPosition());
        }
        if ("REMOVED".equals(oldBlade.getStatus()) || oldBlade.getRemoveTime() != null) {
            throw new IllegalArgumentException("旧叶片已于 " + oldBlade.getRemoveTime() + " 拆除，不能重复换件");
        }
        if (replaceTime.isBefore(oldBlade.getInstallTime())) {
            throw new IllegalArgumentException("换件时间不能早于旧叶片安装时间");
        }

        // 1. 关闭旧叶在役区间
        oldBlade.setRemoveTime(replaceTime);
        oldBlade.setStatus("REMOVED");
        bladeMapper.updateById(oldBlade);

        // 2. 新叶建档，资产归属同一风机，在役区间自换件时刻起
        Blade newBlade = new Blade();
        newBlade.setBladeCode(request.getNewBladeCode());
        newBlade.setTurbineId(request.getTurbineId());
        newBlade.setPosition(request.getPosition());
        newBlade.setMaterial(request.getNewMaterial());
        newBlade.setInstallTime(replaceTime);
        newBlade.setRemoveTime(null);
        newBlade.setStatus("INSTALLED");
        bladeMapper.insert(newBlade);

        // 3. 保留前后关系
        BladeReplacement replacement = new BladeReplacement();
        replacement.setTurbineId(request.getTurbineId());
        replacement.setPosition(request.getPosition());
        replacement.setOldBladeId(oldBlade.getId());
        replacement.setNewBladeId(newBlade.getId());
        replacement.setReplaceTime(replaceTime);
        replacement.setReason(request.getReason());
        replacementMapper.insert(replacement);
        return replacement;
    }

    /**
     * 按统一的半开区间口径，查询风机在指定临界时点实际在役的叶片。
     * install_time <= atTime AND (remove_time IS NULL OR remove_time > atTime)
     */
    public List<Blade> listAtTime(Long turbineId, LocalDateTime atTime) {
        turbineService.getById(turbineId);
        LocalDateTime t = atTime == null ? LocalDateTime.now() : atTime;
        return bladeMapper.selectList(new QueryWrapper<Blade>()
                .eq("turbine_id", turbineId)
                .le("install_time", t)
                .and(w -> w.isNull("remove_time").or().gt("remove_time", t))
                .orderByAsc("position", "install_time"));
    }

    public Blade getById(Long id) {
        Blade blade = bladeMapper.selectById(id);
        if (blade == null) {
            throw new IllegalArgumentException("叶片不存在：" + id);
        }
        return blade;
    }

    public Blade getByCode(String bladeCode) {
        Blade blade = bladeMapper.selectOne(new QueryWrapper<Blade>().eq("blade_code", bladeCode));
        if (blade == null) {
            throw new IllegalArgumentException("叶片不存在：" + bladeCode);
        }
        return blade;
    }

    /**
     * 断言叶片在指定时点在役且归属指定风机（巡检/缺陷登记同一口径校验）。
     */
    public Blade assertInServiceAt(Long bladeId, Long turbineId, LocalDateTime atTime) {
        Blade blade = getById(bladeId);
        if (turbineId != null && !blade.getTurbineId().equals(turbineId)) {
            throw new IllegalArgumentException("叶片不属于该风机，资产归属不一致：" + blade.getBladeCode());
        }
        if (atTime != null && !isInServiceAt(blade, atTime)) {
            throw new IllegalArgumentException("叶片 " + blade.getBladeCode()
                    + " 在 " + atTime + " 不在役（安装 " + blade.getInstallTime()
                    + "，拆除 " + blade.getRemoveTime() + "），请按该时点在役叶片登记");
        }
        return blade;
    }

    /** 按序列号断言在役归属，口径同 {@link #assertInServiceAt} */
    public Blade assertInServiceAtByCode(String bladeCode, Long turbineId, LocalDateTime atTime) {
        Blade blade = getByCode(bladeCode);
        return assertInServiceAt(blade.getId(), turbineId, atTime);
    }

    /** 半开区间口径：[installTime, removeTime) */
    public boolean isInServiceAt(Blade blade, LocalDateTime atTime) {
        if (atTime.isBefore(blade.getInstallTime())) {
            return false;
        }
        return blade.getRemoveTime() == null || atTime.isBefore(blade.getRemoveTime());
    }

    public List<Blade> listByTurbine(Long turbineId) {
        return bladeMapper.selectList(new QueryWrapper<Blade>()
                .eq("turbine_id", turbineId)
                .orderByAsc("position", "install_time"));
    }

    public List<BladeReplacement> listReplacements(Long turbineId) {
        return replacementMapper.selectList(new QueryWrapper<BladeReplacement>()
                .eq("turbine_id", turbineId)
                .orderByDesc("replace_time"));
    }

    /** 换件前后关系（补充新旧叶片序列号，便于台账展示） */
    public List<java.util.Map<String, Object>> listReplacementsWithCode(Long turbineId) {
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (BladeReplacement r : listReplacements(turbineId)) {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("position", r.getPosition());
            row.put("oldBladeId", r.getOldBladeId());
            row.put("oldBladeCode", bladeMapper.selectById(r.getOldBladeId()).getBladeCode());
            row.put("newBladeId", r.getNewBladeId());
            row.put("newBladeCode", bladeMapper.selectById(r.getNewBladeId()).getBladeCode());
            row.put("replaceTime", r.getReplaceTime());
            row.put("reason", r.getReason());
            result.add(row);
        }
        return result;
    }

    private void assertBladeCodeUnique(String code, Long excludeId) {
        QueryWrapper<Blade> wrapper = new QueryWrapper<Blade>().eq("blade_code", code);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        Long count = bladeMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("叶片序列号已存在：" + code);
        }
    }

    private void validatePosition(String position) {
        if (!POSITIONS.contains(position)) {
            throw new IllegalArgumentException("方位只支持 A/B/C：" + position);
        }
    }
}
