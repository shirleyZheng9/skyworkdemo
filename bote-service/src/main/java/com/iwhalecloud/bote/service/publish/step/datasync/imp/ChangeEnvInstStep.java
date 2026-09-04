package com.iwhalecloud.bote.service.publish.step.datasync.imp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.EnvInstParameterSpec;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceInstDTO;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bote.mapper.skill.DataSourceManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 步骤执行器：环境实例确认与修改
 *
 * @author chen.linfa
 * @since 2024-10-31
 */
public class ChangeEnvInstStep extends AbstractDataSyncStep<List<EnvInstParameterSpec>> {

  public ChangeEnvInstStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public List<EnvInstParameterSpec> convertInputParams(Object params) {
    if (params == null) {
      return Collections.emptyList();
    }
    return JsonUtil.parseJson(JsonUtil.toJsonString(params), new TypeReference<List<EnvInstParameterSpec>>() {
    });
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, List<EnvInstParameterSpec> insts) {
    DataSyncParams params = getOutputParams(PublishStepType.PARSE_FILE, new TypeReference<>() {
    });
    if (BooleanUtils.isTrue(params.getAutoConfirm())) {
      // 自动流程是，同时触发 before after 逻辑
      List<EnvInstParameterSpec> envInsts = collectInst(params);
      return after(envInsts);
    }
    else {
      if (auto) {
        return before();
      }
      return after(insts);
    }
  }

  /**
   * 非自动步骤，前时机，自动触发
   * <p>解析数据包内容，构造可调整的环境实例信息 </p>
   */
  private ResultVO<String> before() {
    DataSyncParams params = getOutputParams(PublishStepType.PARSE_FILE, new TypeReference<>() {
    });
    List<EnvInstParameterSpec> insts = collectInst(params);
    // 可调整环境实例信息，维护在 input, 前端依据此数据进行可视化展示
    step.setInput(CollectionUtils.isEmpty(insts) ? null : JsonUtil.toJsonString(insts));
    return ResultVO.success("break");
  }

  /**
   * 非自动步骤，后时机，手动触发
   * <p>将调整后的环境实例，按照规则，写入到应用数据包对应文件 </p>
   */
  private ResultVO<String> after(List<EnvInstParameterSpec> insts) {
    if (CollectionUtils.isEmpty(insts)) {
      return ResultVO.success();
    }
    DataSyncParams params = getDataSyncParams();
    Map<String, List<EnvInstParameterSpec>> group = insts.stream().collect(Collectors.groupingBy(p -> p.getDataSyncCode() + "-" + p.getTableCode()));
    for (Entry<String, List<EnvInstParameterSpec>> entry : group.entrySet()) {
      List<Map<String, Object>> datas = DataSyncDirUtil.getDataRecords(params, entry.getKey());
      for (EnvInstParameterSpec spec : entry.getValue()) {
        Map<String, Object> data = IterableUtils.find(CollectionUtils.emptyIfNull(datas),
          p -> Objects.equals(MapUtils.getString(p, spec.getPrimaryKey()), spec.getPrimaryKeyValue()));
        if (MapUtils.isNotEmpty(data)) {
          data.put(spec.getCode(), spec.getValue());
        }
      }
      // 数据重新写入磁盘
      DataSyncDirUtil.writeJsonFile(params, entry.getKey(), datas);
    }
    return ResultVO.success();
  }

  /**
   * 收集环境信息
   */
  private List<EnvInstParameterSpec> collectInst(DataSyncParams params) {
    List<EnvInstParameterSpec> insts = new ArrayList<>();
    // 收集 API 网关信息
    collectGatewayInst(params, insts);
    // 收集数据源信息
    collectDataSourceInst(params, insts);
    // 收集门户适配信息
    collectPortalInst(params, insts);
    return insts;
  }

  /**
   * 收集 API 网关环境信息
   */
  private void collectGatewayInst(DataSyncParams params, List<EnvInstParameterSpec> insts) {
    // 第一步，读取数据库中的 API 网关实例
    List<EnvInstParameterSpec> dbInsts = new ArrayList<>();
    ServiceGatewayManageMapper mapper = SpringUtil.getBean(ServiceGatewayManageMapper.class);
    List<ServiceGatewayDTO> datas = mapper.selectGatewayByEnvCode(params.getTenantId(), EnvUtil.getEnvCode());
    for (ServiceGatewayDTO dto : CollectionUtils.emptyIfNull(datas)) {
      dbInsts.add(createGatewaySpec(dto.getPlatformCode(), dto.getPlatformName(), dto.getGatewayId(), "url", "环境地址", dto.getUrl()));
      dbInsts.add(
        createGatewaySpec(dto.getPlatformCode(), dto.getPlatformName(), dto.getGatewayId(), "header_json", "头部信息", dto.getHeaderJson()));
    }

    // 第二步，读取文件中的 API 网关实例
    List<EnvInstParameterSpec> fileInsts = new ArrayList<>();
    List<Map<String, Object>> gatewayRecords = DataSyncDirUtil.getDataRecords(params, "SERVICE_PLATFORM-BT_SERVICE_GATEWAY");
    List<Map<String, Object>> envInsts = ListUtils.emptyIfNull(gatewayRecords).stream()
      .filter(p -> EnvUtil.getEnvCode().equals(MapUtils.getString(p, "env_code")))
      .filter(p -> BaseConsts.STATUS_CD_VALID.equals(MapUtils.getString(p, "status_cd")))
      .collect(Collectors.toList());
    List<Map<String, Object>> platforms = DataSyncDirUtil.getDataRecords(params, "SERVICE_PLATFORM-BT_SERVICE_PLATFORM");
    for (Map<String, Object> record : CollectionUtils.emptyIfNull(envInsts)) {
      Long platformId = MapUtils.getLong(record, "platform_id");
      Long gatewayId = MapUtils.getLong(record, "gateway_id");
      String url = MapUtils.getString(record, "url");
      String headerJson = MapUtils.getString(record, "header_json");

      String platformCode = "";
      String platformName = "";
      Map<String, Object> platform = IterableUtils.find(CollectionUtils.emptyIfNull(platforms),
        p -> Objects.equals(MapUtils.getLong(p, "platform_id"), platformId));
      if (platform != null) {
        platformCode = MapUtils.getString(platform, "platform_code");
        platformName = MapUtils.getString(platform, "platform_name");
      }
      fileInsts.add(createGatewaySpec(platformCode, platformName, gatewayId, "url", "环境地址", url));
      fileInsts.add(createGatewaySpec(platformCode, platformName, gatewayId, "header_json", "头部信息", headerJson));
    }

    // 整合数据库、文件中的数据源。以数据库的为准
    insts.addAll(dbInsts);
    for (EnvInstParameterSpec spec : fileInsts) {
      EnvInstParameterSpec exists = IterableUtils.find(dbInsts,
        p -> Objects.equals(p.getParentCode(), spec.getParentCode()) && Objects.equals(p.getCode(), spec.getCode()));
      if (exists != null) {
        exists.setPrimaryKeyValue(spec.getPrimaryKeyValue());
      }
      else {
        insts.add(spec);
      }
    }
  }

  /**
   * 收集数据源信息
   */
  private void collectDataSourceInst(DataSyncParams params, List<EnvInstParameterSpec> insts) {
    // 第一步，读取数据库中的数据源实例
    List<EnvInstParameterSpec> dbInsts = new ArrayList<>();
    DataSourceManageMapper mapper = SpringUtil.getBean(DataSourceManageMapper.class);
    List<DataSourceInstDTO> datas = mapper.selectDataSourceInstByEnvCode(params.getTenantId(), EnvUtil.getEnvCode());
    for (DataSourceInstDTO dto : CollectionUtils.emptyIfNull(datas)) {
      dbInsts.add(createDbSpec(dto.getDataSourceCode(), dto.getDataSourceName(), dto.getDataSourceInstId(), "url", "地址", dto.getUrl()));
      dbInsts.add(
        createDbSpec(dto.getDataSourceCode(), dto.getDataSourceName(), dto.getDataSourceInstId(), "user_name", "用户名", dto.getUserName()));
      dbInsts.add(createDbSpec(dto.getDataSourceCode(), dto.getDataSourceName(), dto.getDataSourceInstId(), "password", "密码", dto.getPassword()));
    }

    // 第二步，读取文件中的数据源实例
    List<EnvInstParameterSpec> fileInsts = new ArrayList<>();
    List<Map<String, Object>> dataSourceRecords = DataSyncDirUtil.getDataRecords(params, "DATA_SOURCE-BT_DATA_SOURCE_INST");
    List<Map<String, Object>> envInsts = CollectionUtils.emptyIfNull(dataSourceRecords).stream()
      .filter(p -> EnvUtil.getEnvCode().equals(MapUtils.getString(p, "env_code")))
      .filter(p -> BaseConsts.STATUS_CD_VALID.equals(MapUtils.getString(p, "status_cd")))
      .collect(Collectors.toList());
    List<Map<String, Object>> dataSources = DataSyncDirUtil.getDataRecords(params, "DATA_SOURCE-BT_DATA_SOURCE");
    for (Map<String, Object> record : CollectionUtils.emptyIfNull(envInsts)) {
      Long dataSourceId = MapUtils.getLong(record, "data_source_id");
      Long dataSourceInstId = MapUtils.getLong(record, "data_source_inst_id");
      String url = MapUtils.getString(record, "url");
      String userName = MapUtils.getString(record, "user_name");
      String password = MapUtils.getString(record, "password");

      String dataSourceCode = "";
      String dataSourceName = "";
      Map<String, Object> dataSource = IterableUtils.find(CollectionUtils.emptyIfNull(dataSources),
        p -> Objects.equals(MapUtils.getLong(p, "data_source_id"), dataSourceId));
      if (dataSource != null) {
        dataSourceCode = MapUtils.getString(dataSource, "data_source_code");
        dataSourceName = MapUtils.getString(dataSource, "data_source_name");
      }
      fileInsts.add(createDbSpec(dataSourceCode, dataSourceName, dataSourceInstId, "url", "地址", url));
      fileInsts.add(createDbSpec(dataSourceCode, dataSourceName, dataSourceInstId, "user_name", "用户名", userName));
      fileInsts.add(createDbSpec(dataSourceCode, dataSourceName, dataSourceInstId, "password", "密码", password));
    }

    // 整合数据库、文件中的数据源。以数据库的为准
    insts.addAll(dbInsts);
    for (EnvInstParameterSpec spec : fileInsts) {
      EnvInstParameterSpec exists = IterableUtils.find(dbInsts,
        p -> Objects.equals(p.getParentCode(), spec.getParentCode()) && Objects.equals(p.getCode(), spec.getCode()));
      if (exists != null) {
        exists.setPrimaryKeyValue(spec.getPrimaryKeyValue());
      }
      else {
        insts.add(spec);
      }
    }
  }

  /**
   * 收集门户适配信息
   */
  private void collectPortalInst(DataSyncParams params, List<EnvInstParameterSpec> insts) {
    // 第一步，读取数据库中的门户适配
    List<EnvInstParameterSpec> dbInsts = new ArrayList<>();
    ExternalPortalMapper mapper = SpringUtil.getBean(ExternalPortalMapper.class);
    List<ExternalPortalDTO> datas = mapper.selectAllPortalList(params.getTenantId());
    for (ExternalPortalDTO dto : CollectionUtils.emptyIfNull(datas)) {
      Long id = dto.getId();
      String code = dto.getPortalCode();
      String name = dto.getPortalName();
      if (BaseConsts.PORTAL_TYPE_SSO.equals(dto.getPortalType())) {
        dbInsts.add(createPortalSpec(code, name, id, "secret_key", "密钥", dto.getSecretKey()));
        dbInsts.add(createPortalSpec(code, name, id, "auto_create_tenant", "自动创建租户", dto.getAutoCreateTenant()));
        dbInsts.add(createPortalSpec(code, name, id, "sso_script", "扩展脚本", dto.getSsoScript()));
      }
      else {
        dbInsts.add(createPortalSpec(code, name, id, "logged_url", "检查接口地址", dto.getLoggedUrl()));
        dbInsts.add(createPortalSpec(code, name, id, "cookie_name", "cookie名称", dto.getCookieName()));
        dbInsts.add(createPortalSpec(code, name, id, "url_param_name", "URL参数名称", dto.getUrlParamName()));
      }
    }

    // 第二步，读取文件中的门户适配
    String fileName = "EXTERNAL_PORTAL-BT_EXTERNAL_PORTAL";
    List<EnvInstParameterSpec> fileInsts = new ArrayList<>();
    List<Map<String, Object>> envInsts = CollectionUtils.emptyIfNull(DataSyncDirUtil.getDataRecords(params, fileName)).stream()
      .filter(p -> BaseConsts.STATUS_CD_VALID.equals(MapUtils.getString(p, "status_cd")))
      .collect(Collectors.toList());
    for (Map<String, Object> record : CollectionUtils.emptyIfNull(envInsts)) {
      Long id = MapUtils.getLong(record, "id");
      String code = MapUtils.getString(record, "portal_code");
      String name = MapUtils.getString(record, "portal_name");
      String loggedUrl = MapUtils.getString(record, "logged_url");
      String cookieName = MapUtils.getString(record, "cookie_name");
      String urlParamName = MapUtils.getString(record, "url_param_name");
      String secretKey = MapUtils.getString(record, "secret_key");
      String autoCreateTenant = MapUtils.getString(record, "auto_create_tenant");
      if (BaseConsts.PORTAL_TYPE_SSO.equals(MapUtils.getString(record, "portal_type"))) {
        fileInsts.add(createPortalSpec(code, name, id, "secret_key", "密钥", secretKey));
        fileInsts.add(createPortalSpec(code, name, id, "auto_create_tenant", "自动创建租户", autoCreateTenant));
      }
      else {
        fileInsts.add(createPortalSpec(code, name, id, "logged_url", "检查接口地址", loggedUrl));
        fileInsts.add(createPortalSpec(code, name, id, "cookie_name", "cookie名称", cookieName));
        fileInsts.add(createPortalSpec(code, name, id, "url_param_name", "URL参数名称", urlParamName));
      }
    }

    // 整合数据库、文件中的数据源。以数据库的为准
    insts.addAll(dbInsts);
    for (EnvInstParameterSpec spec : fileInsts) {
      EnvInstParameterSpec exists = IterableUtils.find(dbInsts,
        p -> Objects.equals(p.getParentCode(), spec.getParentCode()) && Objects.equals(p.getCode(), spec.getCode()));
      if (exists != null) {
        exists.setPrimaryKeyValue(spec.getPrimaryKeyValue());
      }
      else {
        insts.add(spec);
      }
    }
  }

  private EnvInstParameterSpec createGatewaySpec(String parentCode, String parentName, Long instId, String attrCode, String attrName,
    String attrValue) {
    EnvInstParameterSpec spec = new EnvInstParameterSpec();
    spec.setDataSyncCode("SERVICE_PLATFORM");
    spec.setTableCode("bt_service_gateway");
    spec.setCode(attrCode);
    spec.setName(attrName);
    spec.setValue(attrValue);
    spec.setPrimaryKey("gateway_id");
    spec.setPrimaryKeyValue(instId.toString());

    spec.setParentCode(parentCode);
    spec.setGroupKey("gateway");
    spec.setGroupName("网关");
    spec.setParentName(parentName);
    return spec;
  }

  private EnvInstParameterSpec createDbSpec(String parentCode, String parentName, Long instId, String attrCode, String attrName, String attrValue) {
    EnvInstParameterSpec spec = new EnvInstParameterSpec();
    spec.setDataSyncCode("DATA_SOURCE");
    spec.setTableCode("bt_data_source_inst");
    spec.setCode(attrCode);
    spec.setName(attrName);
    spec.setValue(attrValue);
    spec.setPrimaryKey("data_source_inst_id");
    spec.setPrimaryKeyValue(instId.toString());

    spec.setParentCode(parentCode);
    spec.setGroupKey("dataSource");
    spec.setGroupName("数据源");
    spec.setParentName(parentName);
    return spec;
  }

  private EnvInstParameterSpec createPortalSpec(String parentCode, String parentName, Long instId, String attrCode, String attrName,
    String attrValue) {
    EnvInstParameterSpec spec = new EnvInstParameterSpec();
    spec.setDataSyncCode("EXTERNAL_PORTAL");
    spec.setTableCode("bt_external_portal");
    spec.setCode(attrCode);
    spec.setName(attrName);
    spec.setValue(attrValue);
    spec.setPrimaryKey("id");
    spec.setPrimaryKeyValue(instId.toString());

    spec.setParentCode(parentCode);
    spec.setGroupKey("portal");
    spec.setGroupName("门户适配");
    spec.setParentName(parentName);
    return spec;
  }
}

