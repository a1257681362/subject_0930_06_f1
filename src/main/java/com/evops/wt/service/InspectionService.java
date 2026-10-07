package com.evops.wt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.wt.dto.DroneImageRequest;
import com.evops.wt.dto.InspectionRequest;
import com.evops.wt.entity.Blade;
import com.evops.wt.entity.DroneImage;
import com.evops.wt.entity.Inspection;
import com.evops.wt.mapper.DroneImageMapper;
import com.evops.wt.mapper.InspectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class InspectionService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InspectionMapper inspectionMapper;
    private final DroneImageMapper droneImageMapper;
    private final TurbineService turbineService;
    private final BladeService bladeService;

    public InspectionService(InspectionMapper inspectionMapper,
                             DroneImageMapper droneImageMapper,
                             TurbineService turbineService,
                             BladeService bladeService) {
        this.inspectionMapper = inspectionMapper;
        this.droneImageMapper = droneImageMapper;
        this.turbineService = turbineService;
        this.bladeService = bladeService;
    }

    /**
     * 创建巡检工单。指定叶片时按巡检时点校验在役归属（与档案同一时间口径）。
     */
    @Transactional
    public Inspection create(InspectionRequest request) {
        turbineService.getById(request.getTurbineId());
        Blade blade = null;
        if (request.getBladeCode() != null && !request.getBladeCode().trim().isEmpty()) {
            blade = bladeService.assertInServiceAtByCode(
                    request.getBladeCode(), request.getTurbineId(), request.getInspectTime());
        }

        Inspection inspection = new Inspection();
        inspection.setOrderNo(resolveOrderNo(request.getOrderNo()));
        inspection.setTurbineId(request.getTurbineId());
        inspection.setBladeId(blade == null ? null : blade.getId());
        inspection.setInspectTime(request.getInspectTime());
        inspection.setInspector(request.getInspector());
        inspection.setStatus("PENDING");
        inspection.setRemark(request.getRemark());
        inspectionMapper.insert(inspection);
        return inspection;
    }

    @Transactional
    public DroneImage addImage(Long inspectionId, DroneImageRequest request) {
        Inspection inspection = getById(inspectionId);
        DroneImage image = new DroneImage();
        image.setInspectionId(inspection.getId());
        image.setImageUrl(request.getImageUrl());
        image.setShootTime(request.getShootTime());
        image.setLocationDesc(request.getLocationDesc());
        droneImageMapper.insert(image);
        return image;
    }

    public Inspection complete(Long inspectionId) {
        Inspection inspection = getById(inspectionId);
        if ("CANCELED".equals(inspection.getStatus())) {
            throw new IllegalArgumentException("工单已取消，不能完成");
        }
        inspection.setStatus("DONE");
        inspectionMapper.updateById(inspection);
        return inspection;
    }

    public Inspection getById(Long id) {
        Inspection inspection = inspectionMapper.selectById(id);
        if (inspection == null) {
            throw new IllegalArgumentException("巡检工单不存在：" + id);
        }
        return inspection;
    }

    public List<DroneImage> listImages(Long inspectionId) {
        getById(inspectionId);
        return droneImageMapper.selectList(new QueryWrapper<DroneImage>()
                .eq("inspection_id", inspectionId)
                .orderByAsc("shoot_time", "id"));
    }

    public DroneImage getImageById(Long imageId) {
        DroneImage image = droneImageMapper.selectById(imageId);
        if (image == null) {
            throw new IllegalArgumentException("无人机影像不存在：" + imageId);
        }
        return image;
    }

    public List<Inspection> listByTurbine(Long turbineId) {
        turbineService.getById(turbineId);
        return inspectionMapper.selectList(new QueryWrapper<Inspection>()
                .eq("turbine_id", turbineId)
                .orderByDesc("inspect_time"));
    }

    private String resolveOrderNo(String orderNo) {
        String candidate = orderNo == null || orderNo.trim().isEmpty()
                ? "INSP" + LocalDateTime.now().format(NO_FMT) + ThreadLocalRandom.current().nextInt(100, 999)
                : orderNo.trim();
        Long count = inspectionMapper.selectCount(new QueryWrapper<Inspection>().eq("order_no", candidate));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("工单编号已存在：" + candidate);
        }
        return candidate;
    }
}
