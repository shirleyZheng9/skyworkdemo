/*
  天工重复租户物理删除（整份执行版）
  日期：2026-10-13

  作用：物理删除 14 条重复/错误 tenant 及其成员关系（bt_tenant_user）。
    - 天工已删除的 2 个项目：4 条
    - 跨团队错误 tenant：5 条
    - 00X 历史残留：5 条（项目 2073377510150909954）
  附带处理：
    - 不删用户账号；默认项目指向被删 tenant 的用户，default_tenant_id 置为 NULL
    - tgsync_project_sync 中指向被删 tenant 的行，改为 DELETE_SUCCESS 并清空 bote_tenant_id

  执行方式：整个文件一次执行即可，不需要手动 COMMIT。推荐用文件导入（遇错自动停止）：
    mysql --default-character-set=utf8mb4 -h 主机 -P 端口 -u 用户 -p bote < 重复租户物理删除_整份执行版_20261013.sql

  安全机制：
    - 清单写死在 dedup_tenant_list_20261013 表中，每行包含：tenant_id、天工项目ID、预期状态、同项目应保留的 tenant。
    - 删除前逐行校验：tenant_id + 天工项目ID + 状态 完全一致；同项目应保留的 tenant 仍然有效
      （天工已删除项目无保留 tenant）；该行已成功备份。全部满足才删除，否则跳过并在结果中列出。
    - 先整表备份 4 张表。任何一张备份失败，校验都无法通过，不会删除任何数据。
    - 重复执行安全：已删除的行校验不再通过，不会重复处理。

  执行后请把第四段「执行结果」的输出截图或复制发回确认。
  预期结果：total = 14，deleted = 14，skipped = 0；4.3 四个数都是 0；4.4 为空。
*/


/* ================================================================
   一、删除清单（写死）
   ================================================================ */
CREATE TABLE dedup_tenant_list_20261013 (
                                          tenant_id        BIGINT       NOT NULL PRIMARY KEY COMMENT '待删除 tenant',
                                          ext_tenant_id    BIGINT       NOT NULL COMMENT '天工项目ID',
                                          expected_status  CHAR(3)      NOT NULL COMMENT '预期状态',
                                          keep_tenant_id   BIGINT       NULL     COMMENT '同项目应保留的 tenant；NULL 表示天工已删除，全部删除',
                                          reason           VARCHAR(32)  NOT NULL,
                                          tenant_name      VARCHAR(256),
                                          check_ok         TINYINT      NOT NULL DEFAULT 0 COMMENT '1=校验通过'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重复租户删除清单 20261013';

INSERT INTO dedup_tenant_list_20261013 (tenant_id, ext_tenant_id, expected_status, keep_tenant_id, reason, tenant_name) VALUES
                                                                                                                          (1389055998515253248,2071768838161854465,'00A',NULL,'天工已删除项目','信息技术开发系统'),
                                                                                                                          (1391225793977290752,2071768838161854465,'00A',NULL,'天工已删除项目','销讯通管理系统'),
                                                                                                                          (1391814117154791424,2074526957364871169,'00A',NULL,'天工已删除项目','test'),
                                                                                                                          (1410293532920868864,2074526957364871169,'00A',NULL,'天工已删除项目','test'),
                                                                                                                          (1417880343610671104,2071843399050645506,'00A',1389130558879211520,'跨团队错误tenant','设备智能运维管理系统'),
                                                                                                                          (1407919992359587840,2076564305299816449,'00A',1393851465056948224,'跨团队错误tenant','中国移动天工数据'),
                                                                                                                          (1417690725862883328,2089966796954820610,'00A',1407253956702863360,'跨团队错误tenant','能耗管理项目'),
                                                                                                                          (1415647256654921728,2092500236233506818,'00A',1409787396006055936,'跨团队错误tenant','知识管家智能体'),
                                                                                                                          (1417890375288713216,2099666838921424898,'00A',1416953998665543680,'跨团队错误tenant','产拓培训项目'),
                                                                                                                          (1390664669972307968,2073377510150909954,'00X',1390697588593823744,'00X历史残留','工业智能场景演示项目'),
                                                                                                                          (1390692360926699520,2073377510150909954,'00X',1390697588593823744,'00X历史残留','工业设备本体预测性维护系统'),
                                                                                                                          (1391805501773221888,2073377510150909954,'00X',1390697588593823744,'00X历史残留','工业设备本体预测性维护系统'),
                                                                                                                          (1391987413871529984,2073377510150909954,'00X',1390697588593823744,'00X历史残留','工业设备智能数据治理'),
                                                                                                                          (1392025792311107584,2073377510150909954,'00X',1390697588593823744,'00X历史残留','工业设备智能数据治理');


/* ================================================================
   二、整表备份
   ================================================================ */
CREATE TABLE bak_bt_tenant_dedup_20261013 LIKE bt_tenant;
INSERT INTO bak_bt_tenant_dedup_20261013 SELECT * FROM bt_tenant;

CREATE TABLE bak_bt_tenant_user_dedup_20261013 LIKE bt_tenant_user;
INSERT INTO bak_bt_tenant_user_dedup_20261013 SELECT * FROM bt_tenant_user;

CREATE TABLE bak_bt_user_dedup_20261013 LIKE bt_user;
INSERT INTO bak_bt_user_dedup_20261013 SELECT * FROM bt_user;

CREATE TABLE bak_tgsync_project_sync_dedup_20261013 LIKE tgsync_project_sync;
INSERT INTO bak_tgsync_project_sync_dedup_20261013 SELECT * FROM tgsync_project_sync;


/* ================================================================
   三、逐行校验 + 删除（自动提交）
   ================================================================ */

-- 3.1 逐行校验，全部满足才标记 check_ok = 1
UPDATE dedup_tenant_list_20261013 f
  JOIN bt_tenant t ON t.tenant_id = f.tenant_id
  JOIN bak_bt_tenant_dedup_20261013 bt ON bt.tenant_id = f.tenant_id
  SET f.check_ok = 1
WHERE t.ext_tenant_id = f.ext_tenant_id
  AND t.status_cd = f.expected_status
-- 同项目应保留的 tenant 必须仍然有效（天工已删除项目不需要）
  AND (f.keep_tenant_id IS NULL OR EXISTS (
  SELECT 1 FROM bt_tenant k
  WHERE k.tenant_id = f.keep_tenant_id AND k.ext_tenant_id = f.ext_tenant_id AND k.status_cd = '00A'))
-- 与该 tenant 相关的成员关系、用户默认项目、tgsync 行，备份中的数量必须与原表一致（确认已备份）
  AND (SELECT COUNT(*) FROM bak_bt_tenant_user_dedup_20261013 x WHERE x.tenant_id = f.tenant_id)
  = (SELECT COUNT(*) FROM bt_tenant_user y WHERE y.tenant_id = f.tenant_id)
  AND (SELECT COUNT(*) FROM bak_bt_user_dedup_20261013 x WHERE x.default_tenant_id = f.tenant_id)
  = (SELECT COUNT(*) FROM bt_user y WHERE y.default_tenant_id = f.tenant_id)
  AND (SELECT COUNT(*) FROM bak_tgsync_project_sync_dedup_20261013 x WHERE x.bote_tenant_id = f.tenant_id)
  = (SELECT COUNT(*) FROM tgsync_project_sync y WHERE y.bote_tenant_id = f.tenant_id);

-- 3.2 删除并提交（只处理 check_ok = 1 的行）
START TRANSACTION;

UPDATE bt_user u
  JOIN dedup_tenant_list_20261013 f ON f.tenant_id = u.default_tenant_id AND f.check_ok = 1
  SET u.default_tenant_id = NULL,
    u.updated_time = NOW();

UPDATE tgsync_project_sync s
  JOIN dedup_tenant_list_20261013 f ON f.tenant_id = s.bote_tenant_id AND f.check_ok = 1
  SET s.sync_status = 'DELETE_SUCCESS',
    s.bote_tenant_id = NULL;

DELETE tu FROM bt_tenant_user tu
JOIN dedup_tenant_list_20261013 f ON f.tenant_id = tu.tenant_id AND f.check_ok = 1;

DELETE t FROM bt_tenant t
JOIN dedup_tenant_list_20261013 f ON f.tenant_id = t.tenant_id AND f.check_ok = 1
           AND t.ext_tenant_id = f.ext_tenant_id AND t.status_cd = f.expected_status;

COMMIT;


/* ================================================================
   四、执行结果（请把这段输出发回确认）
   ================================================================ */

-- 4.1 汇总：预期 total=14, deleted=14, skipped=0
SELECT COUNT(*)                         AS total,
       SUM(t.tenant_id IS NULL)         AS deleted,
       SUM(t.tenant_id IS NOT NULL)     AS skipped
FROM dedup_tenant_list_20261013 f
       LEFT JOIN bt_tenant t ON t.tenant_id = f.tenant_id;

-- 4.2 被跳过的行（预期为空）
SELECT f.tenant_id, f.tenant_name, f.reason, f.ext_tenant_id, f.expected_status,
       t.ext_tenant_id AS db_ext_tenant_id, t.status_cd AS db_status, f.keep_tenant_id
FROM dedup_tenant_list_20261013 f
       JOIN bt_tenant t ON t.tenant_id = f.tenant_id;

-- 4.3 被删 tenant 的残留（预期 4 个数都是 0）
SELECT
  (SELECT COUNT(*) FROM bt_tenant_user x JOIN dedup_tenant_list_20261013 f ON f.tenant_id = x.tenant_id AND f.check_ok = 1) AS remain_member,
  (SELECT COUNT(*) FROM bt_user x JOIN dedup_tenant_list_20261013 f ON f.tenant_id = x.default_tenant_id AND f.check_ok = 1) AS remain_default,
  (SELECT COUNT(*) FROM tgsync_project_sync x JOIN dedup_tenant_list_20261013 f ON f.tenant_id = x.bote_tenant_id AND f.check_ok = 1) AS remain_tgsync,
  (SELECT COUNT(*) FROM bt_tenant x JOIN dedup_tenant_list_20261013 f ON f.tenant_id = x.tenant_id AND f.check_ok = 1) AS remain_tenant;

-- 4.4 这 8 个项目的重复检查（预期为空）
SELECT ext_tenant_id, COUNT(*) AS cnt, GROUP_CONCAT(CONCAT(tenant_id, ':', status_cd)) AS tenants
FROM bt_tenant
WHERE ext_tenant_id IN (
                        2071768838161854465, 2074526957364871169, 2071843399050645506, 2073377510150909954,
                        2076564305299816449, 2089966796954820610, 2092500236233506818, 2099666838921424898)
GROUP BY ext_tenant_id
HAVING COUNT(*) > 1;


/* ================================================================
   五、回滚（只在出问题时执行：从第二段备份恢复本次删除的数据）
   ================================================================ */
/*
START TRANSACTION;
INSERT INTO bt_tenant
  SELECT b.* FROM bak_bt_tenant_dedup_20261013 b JOIN dedup_tenant_list_20261013 f ON f.tenant_id = b.tenant_id AND f.check_ok = 1
  WHERE NOT EXISTS (SELECT 1 FROM bt_tenant x WHERE x.tenant_id = b.tenant_id);
INSERT INTO bt_tenant_user
  SELECT b.* FROM bak_bt_tenant_user_dedup_20261013 b JOIN dedup_tenant_list_20261013 f ON f.tenant_id = b.tenant_id AND f.check_ok = 1
  WHERE NOT EXISTS (SELECT 1 FROM bt_tenant_user x WHERE x.tenant_user_id = b.tenant_user_id);
UPDATE bt_user u
  JOIN bak_bt_user_dedup_20261013 b ON b.user_id = u.user_id
  JOIN dedup_tenant_list_20261013 f ON f.tenant_id = b.default_tenant_id AND f.check_ok = 1
  SET u.default_tenant_id = b.default_tenant_id
  WHERE u.default_tenant_id IS NULL;
UPDATE tgsync_project_sync s
  JOIN bak_tgsync_project_sync_dedup_20261013 b ON b.tg_tenant_id = s.tg_tenant_id AND b.tg_project_id = s.tg_project_id
  JOIN dedup_tenant_list_20261013 f ON f.tenant_id = b.bote_tenant_id AND f.check_ok = 1
  SET s.sync_status = b.sync_status, s.bote_tenant_id = b.bote_tenant_id;
COMMIT;
*/
