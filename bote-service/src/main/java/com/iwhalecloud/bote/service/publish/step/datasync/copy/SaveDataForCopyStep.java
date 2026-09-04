package com.iwhalecloud.bote.service.publish.step.datasync.copy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bote.service.publish.step.datasync.copy.helper.CopyResourceElementHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 步骤执行器：保存数据
 *
 * @author chen.linfa
 * @since 2025-08-08
 */
@SuppressFBWarnings("SECSQLISPRJDBC")
public class SaveDataForCopyStep extends AbstractDataSyncStep<Object> {

  private static final JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);

  private static final CopyResourceElementHelper helper = SpringUtil.getBean(CopyResourceElementHelper.class);

  public SaveDataForCopyStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    DataSyncParams params = getOutputParams(PublishStepType.INITIALIZE_FOR_COPY, new TypeReference<DataSyncParams>() {
    });

    ResultVO<Void> result = consumer.execute(params);
    DataSyncDirUtil.clearWorkspace(params);
    if (result.isSuccess()) {
      updateSpaceId(params.getResetTenantId(), params.getSpaceId());
      updateBotIsDefault(params.getResetTenantId(), params);
      //更新知识库文件个数
      updateKnowlegeFileCounts(params);
      // 血缘关系处理
      copyResourceElement(params, params.getPrimaryIdMappings());
      // 刷新所有缓存
      ThreadPools.getCommon().submit(() -> SpringUtil.getBean(IRefreshCacheService.class).refreshAll(Refreshable.ALL_CACHE_NAME));
      if (MapUtils.isNotEmpty(params.getPrimaryIdMappings())) {
        step.setOutputJson(JsonUtil.toJsonString(params.getPrimaryIdMappings()));
      }
      return ResultVO.success("finished");
    }
    else {
      return ResultVO.fail(result.getResultMsg());
    }
  }

  private void copyResourceElement(DataSyncParams params, Map<String, Map<Long, Long>> primaryKeyMappings) {
    String querySql = "SELECT resource_element_id, tenant_id, resource_type, resource_id, element_type, element_id FROM bt_resource_element WHERE tenant_id=?";
    List<Object> args = new ArrayList<>();
    args.add(params.getTenantId());
    List<Map<String, Object>> elements = jdbcTemplate.query(querySql, new LowerCaseColumnMapRowMapper(), args.toArray());
    if (CollectionUtils.isEmpty(elements)) {
      return;
    }
    // 过滤掉网关和数据源, 复制时不复制网关和数据源
    elements = elements.stream().filter(data -> {
      String elementType = MapUtils.getString(data, "element_type");
      String resourceType = MapUtils.getString(data, "resource_type");
      return !Objects.equals(elementType, DataSyncCodeEnum.SERVICE_PLATFORM.getCode()) && !Objects.equals(elementType,
        DataSyncCodeEnum.DATA_SOURCE.getCode()) && !Objects.equals(resourceType, DataSyncCodeEnum.SERVICE_PLATFORM.getCode())
      && !Objects.equals(resourceType, DataSyncCodeEnum.DATA_SOURCE.getCode());
    }).toList();
    if (!BooleanUtils.isTrue(params.getSyncAll())) {
      // 增量场景，按需收集血缘关系
      elements = helper.compute(params.getCodeAndIds(), elements);
    }
    if (CollectionUtils.isEmpty(elements)) {
      return;
    }
    if (params.isResetPrimary()) {
      // 重置主键场景，所有血缘关系重新生成
      helper.resetId(primaryKeyMappings, elements);
    }
    // 入库处理
    insertResourceElement(params.getResetTenantId(), elements);
  }

  private void insertResourceElement(Long tenantId, List<Map<String, Object>> elements) {
    // 入库处理
    StringBuilder insertSql = new StringBuilder();
    List<Object[]> insertArgs = new ArrayList<>();
    // @formatter:off
    insertSql.append("INSERT INTO bt_resource_element (")
      .append("resource_element_id, tenant_id, resource_type, resource_id, element_type, element_id,")
      .append("status_cd, creator_id, updator_id, created_time, updated_time) values (")
      .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ");
    if (DbUtil.isOracle()) {
      insertSql.append("sysdate, sysdate)");
    }
    else {
      insertSql.append("now(), now())");
    }
    // @formatter:on
    Long userId = SessionUtil.getLoginInfo().getUserId();
    int size = 1000;
    List<List<Map<String, Object>>> partitionList = Lists.partition(elements, size);
    for (List<Map<String, Object>> datas : partitionList) {
      List<Long> exists = queryExists(tenantId, datas);
      for (Map<String, Object> data : datas) {
        Long primaryValue = MapUtils.getLong(data, "resource_element_id");
        // 忽略已存在的关联关系
        if (!exists.contains(primaryValue)) {
          List<Object> dto = new ArrayList<>();
          dto.add(MapUtils.getLong(data, "resource_element_id"));
          dto.add(tenantId);
          dto.add(MapUtils.getString(data, "resource_type"));
          dto.add(MapUtils.getLong(data, "resource_id"));
          dto.add(MapUtils.getString(data, "element_type"));
          dto.add(MapUtils.getLong(data, "element_id"));
          dto.add(BaseConsts.STATUS_CD_VALID);
          dto.add(userId);
          dto.add(userId);
          insertArgs.add(dto.toArray());
        }
        if (Objects.equals(size, insertArgs.size())) {
          TransactionUtil.executeNew(() -> jdbcTemplate.batchUpdate(insertSql.toString(), insertArgs));
          insertArgs.clear();
        }
      }
    }

    TransactionUtil.executeNew(() -> {
      jdbcTemplate.batchUpdate(insertSql.toString(), insertArgs);
    });
  }

  private List<Long> queryExists(Long tenantId, List<Map<String, Object>> datas) {
    List<Long> primaryValues = datas.stream().map(record -> MapUtils.getLong(record, "resource_element_id")).collect(Collectors.toList());
    String querySql = "SELECT resource_element_id FROM bt_resource_element  WHERE resource_element_id IN ("
      + StringUtils.repeat("?", ", ", primaryValues.size()) + ") AND tenant_id =" + tenantId;
    return jdbcTemplate.queryForList(querySql, Long.class, primaryValues.toArray(new Object[0]));
  }

  private void updateSpaceId(Long tenantId, Long spaceId) {
    if (spaceId == null) {
      return;
    }
    // 重置文档库的的spaceId 入参租户id 是重置的租户id
    List<String> tables = List.of("bt_knowledge_base", "bt_document", "bt_dc_document_library", "bt_dc_document", "bt_catalog");
    for (String table : tables) {
      String sql = "UPDATE " + table + " SET space_id=? WHERE tenant_id=?";
      List<Object> params = new ArrayList<>();
      params.add(spaceId);
      params.add(tenantId);
      TransactionUtil.executeNew(() -> jdbcTemplate.update(sql, params.toArray()));
    }
  }

  /**
   * 更新知识库文件个数
   * @param params 入参
   */
  private void updateKnowlegeFileCounts(DataSyncParams params) {
    Map<String, String> codeAndIds = params.getCodeAndIds();
    if (MapUtils.isEmpty(codeAndIds)) {
      return;
    }
    String knowledgeIds = codeAndIds.get(DataSyncCodeEnum.KNOWLEDGE.getCode());
    if (StringUtils.isNotEmpty(knowledgeIds)) {
      Arrays.stream(knowledgeIds.split("/")).forEach(knowledgeId -> {
        List<Object> paramQuery = new ArrayList<>();
        paramQuery.add(Long.parseLong(knowledgeId));
        paramQuery.add(params.getResetTenantId());
        String sqlQuery = "select document_id  from bt_document where status_cd ='00A' and knowledge_id =? and tenant_id =?";
        List<Long> longs = jdbcTemplate.queryForList(sqlQuery, Long.class, paramQuery.toArray());
        String sql = "UPDATE bt_knowledge_base SET file_counts=? WHERE knowledge_id=? and tenant_id=?";
        List<Object> param = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(longs)) {
          param.add(longs.size());
        }
        else {
          param.add(0L);
        }
        param.add(Long.parseLong(knowledgeId));
        param.add(params.getResetTenantId());
        TransactionUtil.executeNew(() -> jdbcTemplate.update(sql, param.toArray()));
      });
    }
  }

  /**
   * 更新机器人的默认状态
   *
   * @param tenantId 租户id
   * @param params   数据同步参数
   */
  private void updateBotIsDefault(Long tenantId, DataSyncParams params) {
    if (CollectionUtils.isEmpty(params.getDefinitions())) {
      return;
    }
    // 查找 bt_bot 表的定义
    DataSyncTableDefinition botDefinition = params.getDefinitions().stream()
      .filter(def -> "bt_bot".equals(def.getTableCode()))
      .findFirst()
      .orElse(null);
    if (botDefinition == null) {
      return;
    }
    List<Map<String, Object>> dataRecords = botDefinition.getDataRecords();
    if (CollectionUtils.isEmpty(dataRecords)) {
      return;
    }
    String primaryKey = botDefinition.getPrimaryKey();
    if (StringUtils.isEmpty(primaryKey)) {
      return;
    }
    List<Long> botIds = dataRecords.stream()
      .map(record -> MapUtils.getLong(record, primaryKey))
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(botIds)) {
      return;
    }
    String sql = "UPDATE bt_bot SET is_default='F' WHERE tenant_id=? AND bot_id IN ("
      + StringUtils.repeat("?", ", ", botIds.size()) + ")";
    List<Object> sqlParams = new ArrayList<>();
    sqlParams.add(tenantId);
    sqlParams.addAll(botIds);
    TransactionUtil.executeNew(() -> jdbcTemplate.update(sql, sqlParams.toArray()));
  }
}
