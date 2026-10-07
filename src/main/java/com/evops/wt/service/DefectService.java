package com.evops.wt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.wt.dto.AcceptanceRequest;
import com.evops.wt.dto.DefectRequest;
import com.evops.wt.dto.RepairRequest;
import com.evops.wt.entity.*;
import com.evops.wt.mapper.AcceptanceMapper;
import com.evops.wt.mapper.DefectMapper;
import com.evops.wt.mapper.RepairMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DefectService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> SEVERITIES = Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private final DefectMapper defectMapper;
    private final RepairMapper repairMapper;
    private final AcceptanceMapper acceptanceMapper;
    private final InspectionService inspectionService;
    private final BladeService bladeService;

    public DefectService(DefectMapper defectMapper,
                         RepairMapper repairMapper,
                         AcceptanceMapper acceptanceMapper,
                         InspectionService inspectionService,
                         BladeService bladeService) {
        this.defectMapper = defectMapper;
        this.repairMapper = repairMapper;
        this.acceptanceMapper = acceptanceMapper;
        this.inspectionService = inspectionService;
        this.bladeService = bladeService;
    }

    /**
     * 缺陷登记：以巡检时间为临界时点，校验叶片归属与在役口径，
     * 保证缺陷登记的叶片与叶片档案在同一时点口径一致。
     */
    @Transactional
    public Defect register(DefectRequest request) {
        Inspection inspection = inspectionService.getById(request.getInspectionId());
        if (!SEVERITIES.contains(request.getSeverity())) {
            throw new IllegalArgumentException("缺陷等级只支持 LOW/MEDIUM/HIGH/CRITICAL：" + request.getSeverity());
        }

        Blade blade = bladeService.assertInServiceAtByCode(
                request.getBladeCode(), inspection.getTurbineId(), inspection.getInspectTime());

        if (request.getImageId() != null) {
            DroneImage image = inspectionService.getImageById(request.getImageId());
            if (!image.getInspectionId().equals(inspection.getId())) {
                throw new IllegalArgumentException("影像不属于该巡检工单，不能关联");
            }
        }

        Defect defect = new Defect();
        defect.setDefectNo(resolveDefectNo(request.getDefectNo()));
        defect.setInspectionId(inspection.getId());
        defect.setBladeId(blade.getId());
        defect.setImageId(request.getImageId());
        defect.setSeverity(request.getSeverity());
        defect.setDescription(request.getDescription());
        defect.setStatus("REGISTERED");
        defectMapper.insert(defect);
        return defect;
    }

    /**
     * 登记维修结果。已登记或验收驳回的缺陷可维修。
     */
    @Transactional
    public Repair repair(Long defectId, RepairRequest request) {
        Defect defect = getById(defectId);
        if (!"REGISTERED".equals(defect.getStatus()) && !"REJECTED".equals(defect.getStatus())) {
            throw new IllegalArgumentException("当前缺陷状态为 " + defect.getStatus() + "，不能登记维修结果");
        }
        Repair repair = new Repair();
        repair.setDefectId(defect.getId());
        repair.setRepairTime(request.getRepairTime());
        repair.setRepairer(request.getRepairer());
        repair.setRepairResult(request.getRepairResult());
        repairMapper.insert(repair);

        defect.setStatus("REPAIRED");
        defectMapper.updateById(defect);
        return repair;
    }

    /**
     * 验收：通过则关闭缺陷；驳回则回到 REJECTED 可重新维修。
     */
    @Transactional
    public Acceptance accept(Long defectId, AcceptanceRequest request) {
        Defect defect = getById(defectId);
        if (!"REPAIRED".equals(defect.getStatus())) {
            throw new IllegalArgumentException("当前缺陷状态为 " + defect.getStatus() + "，须先完成维修再验收");
        }
        String result = request.getResult();
        if (!"PASS".equals(result) && !"REJECT".equals(result)) {
            throw new IllegalArgumentException("验收结论只支持 PASS/REJECT：" + result);
        }
        LocalDateTime acceptTime = request.getAcceptTime();
        List<Repair> repairs = repairMapper.selectList(new QueryWrapper<Repair>()
                .eq("defect_id", defectId).orderByDesc("repair_time"));
        if (!repairs.isEmpty() && acceptTime.isBefore(repairs.get(0).getRepairTime())) {
            throw new IllegalArgumentException("验收时间不能早于最近一次维修时间");
        }

        Acceptance acceptance = new Acceptance();
        acceptance.setDefectId(defect.getId());
        acceptance.setAcceptTime(acceptTime);
        acceptance.setAcceptor(request.getAcceptor());
        acceptance.setResult(result);
        acceptance.setComment(request.getComment());
        acceptanceMapper.insert(acceptance);

        defect.setStatus("PASS".equals(result) ? "ACCEPTED" : "REJECTED");
        defectMapper.updateById(defect);
        return acceptance;
    }

    public Defect getById(Long id) {
        Defect defect = defectMapper.selectById(id);
        if (defect == null) {
            throw new IllegalArgumentException("缺陷不存在：" + id);
        }
        return defect;
    }

    public List<Defect> listByInspection(Long inspectionId) {
        inspectionService.getById(inspectionId);
        return defectMapper.selectList(new QueryWrapper<Defect>()
                .eq("inspection_id", inspectionId)
                .orderByAsc("severity", "id"));
    }

    public List<Defect> listByBlade(Long bladeId) {
        bladeService.getById(bladeId);
        return defectMapper.selectList(new QueryWrapper<Defect>()
                .eq("blade_id", bladeId)
                .orderByDesc("create_time"));
    }

    public List<Repair> listRepairs(Long defectId) {
        getById(defectId);
        return repairMapper.selectList(new QueryWrapper<Repair>()
                .eq("defect_id", defectId).orderByAsc("repair_time"));
    }

    public List<Acceptance> listAcceptances(Long defectId) {
        getById(defectId);
        return acceptanceMapper.selectList(new QueryWrapper<Acceptance>()
                .eq("defect_id", defectId).orderByAsc("accept_time"));
    }

    private String resolveDefectNo(String defectNo) {
        String candidate = defectNo == null || defectNo.trim().isEmpty()
                ? "DEF" + LocalDateTime.now().format(NO_FMT) + ThreadLocalRandom.current().nextInt(100, 999)
                : defectNo.trim();
        Long count = defectMapper.selectCount(new QueryWrapper<Defect>().eq("defect_no", candidate));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("缺陷编号已存在：" + candidate);
        }
        return candidate;
    }
}
