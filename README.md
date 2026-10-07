# EVOPS 风电叶片巡检台账

Spring Boot 骨架项目上扩展的风电叶片运维台账。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

## 启动

1. `mvn -q -DskipTests compile`
2. `mvn spring-boot:run`（自动执行 `src/main/resources/schema.sql`）
3. H2 数据文件默认写入工作区 `data/evops`

业务接口启用 HTTP Basic 认证：`bootstrap / bootstrap`（健康检查 `/api/health` 免认证）。

## 业务模型

- **风机** `t_wt_turbine`：风机编号（唯一）、场址、机型、并网时间。
- **叶片** `t_wt_blade`：序列号（全局唯一）、方位（A/B/C）、材料；
  `turbine_id` 为资产归属，建档后不因换件改变；在役状态用半开区间
  `[install_time, remove_time)` 表达，`remove_time` 为空表示在役至今。
- **换件** `t_wt_blade_replacement`：同事务关闭旧叶区间、建立新叶档案并保留新旧前后关系。
  换件时刻 T 为统一临界口径：旧叶止于 T（不含）、新叶始于 T（含），二者不重叠、不留空。
- **巡检工单** `t_wt_inspection` + **无人机影像** `t_wt_drone_image`。
- **缺陷** `t_wt_defect`：等级 LOW/MEDIUM/HIGH/CRITICAL；状态机
  `REGISTERED → REPAIRED → ACCEPTED`，验收驳回为 `REJECTED`，可重新维修后再验收。
- **维修** `t_wt_repair`（可多次，保留返修历史）、**验收** `t_wt_acceptance`。
- 电量结算暂未实现，后续在同一模型上扩展。

巡检挂叶片、缺陷登记均以**巡检时间**为临界时点，按与叶片档案相同的半开区间口径
校验在役与归属，保证档案归属和业务登记同一时间口径。临界时点归属可通过
`GET /api/wt/blades/at-time` 直接核验。

## 主要接口

| 流程 | 接口 |
|---|---|
| 风机建档/查询 | `POST/GET /api/wt/turbines`，`GET /api/wt/turbines/{id}` |
| 叶片建档 | `POST /api/wt/blades` |
| 换件（保留前后关系） | `POST /api/wt/blades/replacements` |
| 临界时点在役叶片 | `GET /api/wt/blades/at-time?turbineId=&atTime=` |
| 风机叶片台账+换件链 | `GET /api/wt/blades/by-turbine/{turbineId}` |
| 巡检工单 | `POST /api/wt/inspections`，`POST /api/wt/inspections/{id}/complete` |
| 无人机影像 | `POST /api/wt/inspections/{id}/images` |
| 缺陷登记 | `POST /api/wt/defects` |
| 维修结果 | `POST /api/wt/defects/{id}/repairs` |
| 验收（PASS/REJECT） | `POST /api/wt/defects/{id}/acceptance` |

根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。
