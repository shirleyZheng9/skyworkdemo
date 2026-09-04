package com.iwhalecloud.bote.service.publish.step.datasync.imp;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 步骤执行器：确认导入
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@SuppressFBWarnings("SECSQLISPRJDBC")
public class ConfirmImportStep extends AbstractDataSyncStep<Object> {

  private static final JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
  private static final ITenantManageService tenantManageService = SpringUtil.getBean(ITenantManageService.class);

  public ConfirmImportStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public Object convertInputParams(Object params) {
    return params;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    DataSyncParams params = getDataSyncParams();
    ResultVO<Void> result = consumer.execute(params);
    // 组装提示信息
    String tip = wrapTipMessage(params, result);
    DataSyncDirUtil.clearWorkspace(params);
    // 自动纠错
    autoRrrorCorrection(params.getTenantId(), params.getSpaceId());
    if (result.isSuccess()) {
      if (StringUtils.isNotEmpty(tip)) {
        step.setOutputJson(tip);
      }
      // 刷新所有缓存
      ThreadPools.getCommon().submit(() -> SpringUtil.getBean(IRefreshCacheService.class).refreshAll(Refreshable.ALL_CACHE_NAME));
      return ResultVO.success("finished");
    }
    else {
      return ResultVO.fail(result.getResultMsg());
    }
  }

  /**
   * 导出成功后，组装提示信息
   * <p>1.首次导入，当前用户加入该项目 </p>
   * <p>2.导入数据包含知识，提醒用户重构相关的知识</p>
   */
  private String wrapTipMessage(DataSyncParams params, ResultVO<Void> result) {
    if (!result.isSuccess()) {
      return null;
    }
    // 检查当前用户是否需要加入该项目
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (!SessionUtil.isSuperAdmin(userId)) {
      tenantManageService.addTenantUser(params.getTenantId(), userId, BaseConsts.ROLE_MANAGE);
    }
    StringBuilder message = new StringBuilder();
    List<Map<String, Object>> knowledges = DataSyncDirUtil.getDataRecords(params, "KNOWLEDGE_BASE-BT_KNOWLEDGE_BASE");
    if (CollectionUtils.isNotEmpty(knowledges)) {
      // 提醒用户重构相关的知识
      String names = knowledges.stream().map(p -> MapUtils.getString(p, "knowledge_name")).collect(Collectors.joining(","));
      message.append("检测到导入了知识【").append(names).append("】请进入知识界面，点击重构，避免知识问答出现异常").append(System.lineSeparator());
    }
    return message.toString();
  }

  /**
   * 导入配置后，自动纠错配置
   * <p>1. 租户功能设置，不可出现多条 funcType 相同的配置数据 </p>
   * <p>2. 门户适配，不可出现多条 portalCode 相同的配置数据 </p>
   * <p>3. 多数据源，不可出现多条 evnCode 相同的配置数据 </p>
   * <p>4. 应用关联智能体，不可出现多条相关的配置数据 </p>
   * <p>5. 网关配置，不可出现多条 evnCode 相同的配置数据 </p>
   * <p>6. 调整工作空间与租户的关系 </p>
   */
  private void autoRrrorCorrection(Long tenantId, Long spaceId) {
    checkSettingInfo(tenantId);
    checkPortal(tenantId);
    checkDataSource(tenantId);
    checkBotSceneRel(tenantId);
    checkGateway(tenantId);
    checkWorkspace(tenantId, spaceId);
  }

  private void checkSettingInfo(Long tenantId) {
    String sql = "SELECT setting_id, func_type, bot_id FROM bt_tenant_setting_info WHERE status_cd=? AND tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(BaseConsts.STATUS_CD_VALID);
    args.add(tenantId);
    Map<String, List<Map<String, Object>>> map = CollectionUtils.emptyIfNull(
        jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray())).stream()
      .collect(Collectors.groupingBy(p -> MapUtils.getString(p, "func_type") + CacheConsts.COLON + MapUtils.getString(p, "bot_id")));
    List<Long> ids = new ArrayList<>();
    for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          ids.add(MapUtils.getLong(entry.getValue().get(i), "setting_id"));
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ids)) {
      String updateSql =
        "UPDATE bt_tenant_setting_info SET status_cd=?, remark=? WHERE tenant_id=? AND setting_id IN (" + StringUtils.repeat("?", ", ", ids.size())
          + " ) ";
      args.clear();
      args.add(BaseConsts.STATUS_CD_INVALID);
      args.add("自动纠错屏蔽数据");
      args.add(tenantId);
      args.addAll(ids);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(updateSql, args.toArray()));
    }
  }

  private void checkPortal(Long tenantId) {
    String sql = "SELECT id, portal_code FROM bt_external_portal WHERE status_cd=? AND default_tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(BaseConsts.STATUS_CD_VALID);
    args.add(tenantId);
    Map<String, List<Map<String, Object>>> map = CollectionUtils.emptyIfNull(
        jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray())).stream()
      .collect(Collectors.groupingBy(p -> MapUtils.getString(p, "portal_code")));
    List<Long> ids = new ArrayList<>();
    for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          ids.add(MapUtils.getLong(entry.getValue().get(i), "id"));
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ids)) {
      String updateSql =
        "UPDATE bt_external_portal SET status_cd=?, remark=? WHERE default_tenant_id=? AND id IN (" + StringUtils.repeat("?", ", ", ids.size())
          + " ) ";
      args.clear();
      args.add(BaseConsts.STATUS_CD_INVALID);
      args.add("自动纠错屏蔽数据");
      args.add(tenantId);
      args.addAll(ids);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(updateSql, args.toArray()));
    }
  }

  private void checkDataSource(Long tenantId) {
    String sql = "SELECT data_source_inst_id, data_source_id, env_code FROM bt_data_source_inst WHERE status_cd=? AND tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(BaseConsts.STATUS_CD_VALID);
    args.add(tenantId);
    Map<String, List<Map<String, Object>>> map = CollectionUtils.emptyIfNull(
        jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray())).stream()
      .collect(Collectors.groupingBy(p -> MapUtils.getString(p, "data_source_id") + MapUtils.getString(p, "env_code")));
    List<Long> ids = new ArrayList<>();
    for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          ids.add(MapUtils.getLong(entry.getValue().get(i), "data_source_inst_id"));
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ids)) {
      String updateSql =
        "UPDATE bt_data_source_inst SET status_cd=?, remark=? WHERE tenant_id=? AND data_source_inst_id IN (" + StringUtils.repeat("?", ", ",
          ids.size()) + " ) ";
      args.clear();
      args.add(BaseConsts.STATUS_CD_INVALID);
      args.add("自动纠错屏蔽数据");
      args.add(tenantId);
      args.addAll(ids);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(updateSql, args.toArray()));
    }
  }

  private void checkBotSceneRel(Long tenantId) {
    String sql = "SELECT rel_id, bot_id, scene_id FROM bt_bot_scene_rel WHERE status_cd=? AND tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(BaseConsts.STATUS_CD_VALID);
    args.add(tenantId);
    Map<String, List<Map<String, Object>>> map = CollectionUtils.emptyIfNull(
        jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray())).stream()
      .collect(Collectors.groupingBy(p -> MapUtils.getString(p, "bot_id") + MapUtils.getString(p, "scene_id")));
    List<Long> ids = new ArrayList<>();
    for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          ids.add(MapUtils.getLong(entry.getValue().get(i), "rel_id"));
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ids)) {
      String updateSql =
        "UPDATE bt_bot_scene_rel SET status_cd=?, remark=? WHERE tenant_id=? AND rel_id IN (" + StringUtils.repeat("?", ", ", ids.size()) + " ) ";
      args.clear();
      args.add(BaseConsts.STATUS_CD_INVALID);
      args.add("自动纠错屏蔽数据");
      args.add(tenantId);
      args.addAll(ids);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(updateSql, args.toArray()));
    }
  }

  private void checkGateway(Long tenantId) {
    String sql = "SELECT gateway_id, platform_id, env_code FROM bt_service_gateway WHERE status_cd=? AND tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(BaseConsts.STATUS_CD_VALID);
    args.add(tenantId);
    Map<String, List<Map<String, Object>>> map = CollectionUtils.emptyIfNull(
        jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray())).stream()
      .collect(Collectors.groupingBy(p -> MapUtils.getString(p, "platform_id") + MapUtils.getString(p, "env_code")));
    List<Long> ids = new ArrayList<>();
    for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          ids.add(MapUtils.getLong(entry.getValue().get(i), "gateway_id"));
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ids)) {
      String updateSql =
        "UPDATE bt_service_gateway SET status_cd=?, remark=? WHERE tenant_id=? AND gateway_id IN (" + StringUtils.repeat("?", ", ", ids.size())
          + " ) ";
      args.clear();
      args.add(BaseConsts.STATUS_CD_INVALID);
      args.add("自动纠错屏蔽数据");
      args.add(tenantId);
      args.addAll(ids);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(updateSql, args.toArray()));
    }
  }

  private void checkWorkspace(Long tenantId, Long spaceId) {
    if (spaceId == null) {
      return;
    }
    // 同步更新与空间关联的关键业务表的 space_id
    List<String> tables = List.of("bt_tenant", "bt_knowledge_base", "bt_document", "bt_dc_document_library", "bt_dc_document", "bt_catalog");
    for (String table : tables) {
      String sql = "UPDATE " + table + " SET space_id=? WHERE tenant_id=?";
      List<Object> params = new ArrayList<>();
      params.add(spaceId);
      params.add(tenantId);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(sql, params.toArray()));
    }
  }
}
