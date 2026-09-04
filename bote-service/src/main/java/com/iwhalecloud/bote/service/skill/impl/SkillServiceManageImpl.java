package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.CurlParseUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.SwaggerApiUtil;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.skill.CurlParamsDTO;
import com.iwhalecloud.bote.dto.skill.CurlParseResultDTO;
import com.iwhalecloud.bote.dto.skill.OpenApiInfoDTO;
import com.iwhalecloud.bote.dto.skill.ServiceApiDocsParams;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.ServiceMockDTO;
import com.iwhalecloud.bote.dto.skill.ServiceMockParams;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceMockManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillServiceManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.IServicePlatformManageService;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 技能：API 服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class SkillServiceManageImpl implements ISkillServiceManageService {
  private static final Logger logger = LoggerFactory.getLogger(SkillServiceManageImpl.class);
  private final SkillServiceManageMapper serviceManageMapper;
  private final QuerySkillMapper querySkillMapper;
  private final ServiceGatewayManageMapper gatewayManageMapper;
  private final ServicePlatformManageMapper platformManageMapper;
  private final IServicePlatformManageService platformManageService;
  private final ICatalogManageService catalogManageService;
  private final ServiceMockManageMapper serviceMockManageMapper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillServiceDTO> saveSkillService(SkillServiceDTO service) {
    if (service.getCopyServiceId() != null) {
      return copySkillService(service);
    }
    if (serviceManageMapper.existsServiceCode(service)) {
      return BaseErrorConstant.CHECK_CODE.toResult(service.getServiceCode());
    }

    // 校验模拟响应报文配置
    ResultVO<SkillServiceDTO> validationResult = validateMockResponse(service);
    if (validationResult != null) {
      return validationResult;
    }

    ServicePlatformDTO platform = service.getPlatform();
    if (platform != null) {
      service.setPlatformId(platform.getPlatformId());
    }
    fillServiceMockList(service);
    service.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillServiceDTO old = service.getServiceId() == null ? null : findSkillService(service.getTenantId(), service.getServiceId());
    DataDifference<SkillServiceDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, service, true, service.getTenantId(),
      OperClassEnum.SKILL_API);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 校验模拟响应报文配置
   */
  @Nullable
  private ResultVO<SkillServiceDTO> validateMockResponse(SkillServiceDTO service) {
    // 未开启接口模拟时不校验
    if (!BaseConsts.TRUE.equals(service.getIsMock())) {
      return null;
    }

    // 校验缺省模拟报文
    Assert.hasLength(service.getMockResponseJson(), "请配置缺省模拟报文");
    try {
      JsonUtil.parseJsonRequired(service.getMockResponseJson(), Object.class);
    }
    catch (RuntimeException e) {
      return ResultVO.fail("缺省模拟报文不合法: " + e.getMessage());
    }

    // 校验模拟报文用例
    for (ServiceMockDTO mock : ListUtils.emptyIfNull(service.getServiceMockList())) {
      Assert.hasLength(mock.getConditionJson(), () -> "请配置模拟报文【" + mock.getMockName() + "】的条件");
      Assert.hasLength(mock.getRspJson(), () -> "请配置模拟报文【" + mock.getMockName() + "】的报文");
      try {
        JsonUtil.parseJsonRequired(mock.getConditionJson(), IfCondition.class);
      }
      catch (RuntimeException e) {
        return ResultVO.fail("模拟报文【" + mock.getMockName() + "】的条件不合法: " + e.getMessage());
      }
      try {
        JsonUtil.parseJsonRequired(mock.getRspJson(), Object.class);
      }
      catch (RuntimeException e) {
        return ResultVO.fail("模拟报文【" + mock.getMockName() + "】的报文不合法: " + e.getMessage());
      }
    }
    return null;
  }

  private void fillServiceMockList(SkillServiceDTO service) {
    if (CollectionUtils.isEmpty(service.getServiceMockList())) {
      return;
    }
    service.getServiceMockList().forEach(p -> {
      p.setTenantId(service.getTenantId());
      p.setStatusCd(BaseConsts.STATUS_CD_VALID);
    });
  }

  private ResultVO<SkillServiceDTO> copySkillService(SkillServiceDTO service) {
    SkillServiceDTO oldService = findSkillService(service.getTenantId(), service.getCopyServiceId());
    if (oldService == null) {
      return BaseErrorConstant.BOT_SKILL_SERVICE_NOT_EXIST.toResult(service.getServiceId());
    }
    if (serviceManageMapper.existsServiceCode(service)) {
      return BaseErrorConstant.CHECK_CODE.toResult(service.getServiceCode());
    }
    ServicePlatformDTO platform = service.getPlatform();
    if (platform != null) {
      service.setPlatformId(platform.getPlatformId());
    }
    CollectionUtils.emptyIfNull(service.getServiceMockList()).forEach(p -> {
      p.setServiceId(null);
      p.setRspId(null);
    });
    DataDifferenceStarter.computeSaveAndLog(null, service, false, service.getTenantId(), OperClassEnum.SKILL_API);
    return ResultVO.success(service);
  }

  @Override
  @Nullable
  public SkillServiceDTO findSkillService(Long tenantId, Long serviceId) {
    SkillServiceDTO service = serviceManageMapper.getSkillService(tenantId, serviceId, BaseConsts.STATUS_CD_VALID);
    if (service == null) {
      return null;
    }
    service.setServiceMockList(serviceMockManageMapper.selectServiceMockList(service.getTenantId(), serviceId));
    service.setPlatform(platformManageService.findServicePlatform(tenantId, service.getPlatformId()));
    return service;
  }

  @Override
  public List<SimpleSkillServiceDTO> querySkillServiceList(SkillQueryParams params) {
    return querySkillMapper.selectSkillServiceList(params);
  }

  @Override
  public PageInfo<SkillServiceDTO> querySkillServicePage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    PageInfo<SkillServiceDTO> result = serviceManageMapper.selectSkillServicePage(params, params.buildRowBounds()).toPageInfo();
    buildSkillService(params.getTenantId(), result.getList());
    return result;
  }

  @Override
  public PageInfo<SimpleSkillServiceDTO> querySimpleSkillServicePage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillServicePage(params, params.buildRowBounds()).toPageInfo();
  }

  private void buildSkillService(Long tenantId, List<SkillServiceDTO> skillServiceList) {
    List<Long> platformIds = CollectionUtils.emptyIfNull(skillServiceList).stream().map(SkillServiceDTO::getPlatformId).collect(Collectors.toList());
    List<ServicePlatformDTO> servicePlatDefList = platformManageService.queryServicePlatformByIds(tenantId, platformIds);
    for (SkillServiceDTO botSkillService : skillServiceList) {
      botSkillService.setPlatform(IterableUtils.find(servicePlatDefList, p -> p.getPlatformId().equals(botSkillService.getPlatformId())));
    }
  }

  @Override
  public ResultVO<Void> deleteSkillService(Long tenantId, Long serviceId) {
    SkillServiceDTO botSkillService = findSkillService(tenantId, serviceId);
    if (botSkillService == null) {
      return BaseErrorConstant.BOT_SKILL_SERVICE_NOT_EXIST.toResult(serviceId);
    }
    if (resourceElementService.existsRelatedResource(tenantId, serviceId, DataSyncCodeEnum.SKILL_SERVICE.getCode())) {
      return ResultVO.fail("服务已存在关联配置数据，不允许删除");
    }
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    serviceManageMapper.deleteSkillService(botSkillService.getTenantId(), serviceId, loginInfo.getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_API.name()).clear(tenantId, serviceId);
    return ResultVO.success();
  }

  @Override
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerUrl(String serviceDocUrl) {
    ResultVO<OpenAPI> result = getOpenApiByDocUrl(serviceDocUrl);
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    return parseOpenApiInfo(result.getResultObject());
  }

  @Override
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerFile(MultipartFile file) {
    ResultVO<OpenAPI> result = getOpenApiByFile(file);
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    return parseOpenApiInfo(result.getResultObject());
  }

  @Override
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromOpenApiJson(String openApiJson) {
    ResultVO<OpenAPI> result = getOpenApiByJson(openApiJson);
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    return parseOpenApiInfo(result.getResultObject());
  }

  /**
   * 解析OpenAPI信息的通用方法
   *
   * @param api OpenAPI
   * @return 解析结果
   */
  private ResultVO<List<OpenApiInfoDTO>> parseOpenApiInfo(OpenAPI api) {
    List<OpenApiInfoDTO> openApiInfoList = new ArrayList<>();
    if (api.getPaths() == null) {
      return ResultVO.success(openApiInfoList);
    }
    api.getPaths().forEach((interfaceUrl, path) -> {
      OpenApiInfoDTO openApiInfoDTO = new OpenApiInfoDTO();
      if (path.getGet() != null) {
        openApiInfoDTO.setServiceMethod(BaseConsts.REQUEST_TYPE_GET.toLowerCase());
        openApiInfoDTO.setServiceName(path.getGet().getSummary());
      }
      else if (path.getPost() != null) {
        openApiInfoDTO.setServiceMethod(BaseConsts.REQUEST_TYPE_POST.toLowerCase());
        openApiInfoDTO.setServiceName(path.getPost().getSummary());
      }
      openApiInfoDTO.setPath(interfaceUrl);
      openApiInfoList.add(openApiInfoDTO);
    });
    return ResultVO.success(openApiInfoList);
  }

  /**
   * 根据url获取OpenAPI文档
   */
  private ResultVO<OpenAPI> getOpenApiByDocUrl(String serviceDocUrl) {
    OpenAPI api;
    try {
      api = new OpenAPIV3Parser().read(serviceDocUrl);
    }
    catch (Exception e) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult(e);
    }
    if (api == null) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult();
    }
    return ResultVO.success(api);
  }

  /**
   * 根据swagger文件获取OpenAPI文档
   */
  private ResultVO<OpenAPI> getOpenApiByFile(MultipartFile file) {
    SwaggerParseResult swaggerParseResult;
    try (InputStream inputStream = file.getInputStream()) {
      swaggerParseResult = new OpenAPIV3Parser().readContents(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }
    catch (Exception e) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult(e);
    }
    if (swaggerParseResult.getOpenAPI() == null) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult();
    }
    return ResultVO.success(swaggerParseResult.getOpenAPI());
  }

  /**
   * 根据OpenAPI JSON字符串获取OpenAPI文档
   */
  private ResultVO<OpenAPI> getOpenApiByJson(String openApiJson) {
    SwaggerParseResult swaggerParseResult;
    try {
      swaggerParseResult = new OpenAPIV3Parser().readContents(openApiJson);
    }
    catch (Exception e) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult(e);
    }
    if (swaggerParseResult.getOpenAPI() == null) {
      return BaseErrorConstant.GET_OPEN_API_FAILED.toResult();
    }
    return ResultVO.success(swaggerParseResult.getOpenAPI());
  }

  @Override
  @Transactional
  public ResultVO<List<SkillServiceDTO>> importSwaggerServiceList(@Nullable MultipartFile file, ServiceApiDocsParams apiDocsParams) {
    try {
      // 解析 OpenAPI 文档
      ResultVO<OpenAPI> openApiResult = getOpenApiSource(apiDocsParams, file);
      if (!openApiResult.isSuccess()) {
        return new ResultVO<>(openApiResult);
      }
      // 筛选需要导入的API路径
      List<String> selectedApiPaths = CollectionUtils.emptyIfNull(apiDocsParams.getOpenApiInfoList()).stream().map(OpenApiInfoDTO::getPath).toList();
      // 转换为技能服务对象
      List<SkillServiceDTO> skillServices = fillAppServiceList(openApiResult.getResultObject(), apiDocsParams, selectedApiPaths);
      // 保存到数据库
      if (CollectionUtils.isNotEmpty(skillServices)) {
        // 检查服务编码是否已存在
        List<String> serviceCodes = skillServices.stream().map(SkillServiceDTO::getServiceCode).toList();
        List<SkillServiceDTO> existingServices = serviceManageMapper.selectServiceByServiceCodes(serviceCodes, apiDocsParams.getTenantId());
        if (CollectionUtils.isNotEmpty(existingServices)) {
          return BaseErrorConstant.CHECK_CODE.toResult(existingServices.getFirst().getServiceCode());
        }
        serviceManageMapper.batchInsertSkillService(skillServices);
      }
      return ResultVO.success(skillServices);
    }
    catch (Exception e) {
      logger.error("Failed to import swagger service list. error={}", e.getMessage(), e);
      throw new BssException("导入失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取OpenAPI
   */
  private ResultVO<OpenAPI> getOpenApiSource(ServiceApiDocsParams apiDocsQueryParams, @Nullable MultipartFile file) {
    // 优先使用URL
    if (StringUtils.isNotEmpty(apiDocsQueryParams.getServiceDocUrl())) {
      return getOpenApiByDocUrl(apiDocsQueryParams.getServiceDocUrl());
    }
    if (file != null) {
      return getOpenApiByFile(file);
    }
    return getOpenApiByJson(apiDocsQueryParams.getOpenApiJson());
  }


  /**
   * 转换OpenAPI文档为技能服务对象
   */
  private List<SkillServiceDTO> fillAppServiceList(OpenAPI api, ServiceApiDocsParams params, List<String> chooseApiPaths) {
    List<SkillServiceDTO> appServices = new ArrayList<>();
    api.getPaths().forEach((interfaceUrl, path) -> {
      if (!chooseApiPaths.contains(interfaceUrl)) {
        return;
      }
      SkillServiceDTO service = createSkillService(api, params, interfaceUrl, path);
      if (service != null) {
        appServices.add(service);
      }
    });
    return appServices;
  }

  /**
   * 根据路径信息创建技能服务对象
   */
  @Nullable
  private SkillServiceDTO createSkillService(OpenAPI api, ServiceApiDocsParams params, String interfaceUrl, PathItem path) {
    SkillServiceDTO service = new SkillServiceDTO();

    // 处理GET请求
    if (path.getGet() != null) {
      return createGetService(api, params, interfaceUrl, path, service);
    }
    // 处理POST请求
    else if (path.getPost() != null) {
      return createPostService(api, params, interfaceUrl, path, service);
    }
    // 其他请求类型不处理
    else {
      return null;
    }
  }

  /**
   * 创建GET类型的技能服务
   */
  private SkillServiceDTO createGetService(OpenAPI api, ServiceApiDocsParams params, String interfaceUrl,
    PathItem path, SkillServiceDTO service) {
    Operation operation = path.getGet();
    service.setServiceName(
      StringUtils.isEmpty(operation.getSummary()) ? StringUtils.substringAfterLast(interfaceUrl, "/") : operation.getSummary());
    service.setReqMethod(BaseConsts.REQUEST_TYPE_GET.toUpperCase());

    // 解析参数
    parseGetParameters(api, operation, service);

    // 解析响应
    service.setResponseJson(SwaggerApiUtil.getResponseJson(operation, api.getComponents().getSchemas()));

    // 设置公共属性
    setCommonServiceProperties(service, params, interfaceUrl);
    return service;
  }

  /**
   * 解析GET请求的参数
   */
  private void parseGetParameters(OpenAPI api, Operation operation, SkillServiceDTO service) {
    if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
      // 路径参数请求
      if ("path".equals(operation.getParameters().getFirst().getIn())) {
        service.setPathJson(SwaggerApiUtil.getRequestJson(operation, api.getComponents().getSchemas()));
      } else {
        // 查询参数
        service.setQueryJson(SwaggerApiUtil.getRequestJson(operation, api.getComponents().getSchemas()));
      }
    }
  }

  /**
   * 创建POST类型的技能服务
   */
  private SkillServiceDTO createPostService(OpenAPI api, ServiceApiDocsParams params, String interfaceUrl,
    PathItem path, SkillServiceDTO service) {
    Operation operation = path.getPost();
    service.setServiceName(
      StringUtils.isEmpty(operation.getSummary()) ? StringUtils.substringAfterLast(interfaceUrl, "/") : operation.getSummary());
    service.setReqMethod(BaseConsts.REQUEST_TYPE_POST.toUpperCase());

    // 解析参数和响应
    service.setBodyJson(SwaggerApiUtil.getRequestJson(operation, api.getComponents().getSchemas()));
    service.setResponseJson(SwaggerApiUtil.getResponseJson(operation, api.getComponents().getSchemas()));

    // 设置公共属性
    setCommonServiceProperties(service, params, interfaceUrl);
    return service;
  }

  /**
   * 设置服务的公共属性
   */
  private void setCommonServiceProperties(SkillServiceDTO service, ServiceApiDocsParams params, String interfaceUrl) {
    // 请求路径 aa/bb/cc
    service.setServiceCode(interfaceUrl.substring(interfaceUrl.lastIndexOf("/") + 1));
    service.setRelativePath(interfaceUrl);
    service.setTenantId(params.getTenantId());
    service.setCatalogItemId(params.getCatalogItemId() == null ? Long.valueOf(-1L) : params.getCatalogItemId());
    service.setPlatformId(params.getPlatformId());
    service.setServiceId(Sequences.SKILL_SERVICE_ID.next());
    service.setStatusCd(BaseConsts.STATUS_CD_VALID);
    service.setCreatorId(SessionUtil.getLoginInfo().getUserId());
  }


  @Override
  @Transactional
  public ResultVO<Void> toggleServiceMock(ServiceMockParams params) {
    params.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    serviceManageMapper.updateServiceMock(params);
    return ResultVO.success();
  }

  @Override
  public ResultVO<CurlParseResultDTO> parseCurlCommand(CurlParamsDTO curlParams) {
    try {
      // 解析curl命令
      CurlParseResultDTO service = CurlParseUtil.parseCurl(curlParams);
      // 从相对路径生成服务编码和名称
      String serviceCode = StringUtils.substringAfterLast(service.getRelativePath(), "/");
      service.setServiceCode(serviceCode);
      service.setServiceName(serviceCode + "服务");
      return ResultVO.success(service);
    }
    catch (Exception e) {
      return ResultVO.fail("解析curl命令失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void syncServiceFromBeyond(List<SkillServiceDTO> services, @Nullable Long catalogId) {
    Long tenantId = BaseConsts.PLATFORM_TENANT_ID;
    if (catalogId != null) {
      serviceManageMapper.deleteServiceByCatalogId(tenantId, catalogId, 1L);
    }
    else {
      for (SkillServiceDTO dto : CollectionUtils.emptyIfNull(services)) {
        // 同步网关信息
        Long platformId;
        ServiceGatewayDTO gateway = dto.getPlatform().getGateways().getFirst();
        ServiceGatewayDTO gatewayDb = gatewayManageMapper.selectGatewayByUrl(tenantId, BaseConsts.ENV_CODE_DEV, gateway.getUrl());
        if (gatewayDb != null) {
          platformId = gatewayDb.getPlatformId();
        }
        else {
          platformId = IDUtils.nextId();
          dto.getPlatform().setPlatformId(platformId);
          dto.getPlatform().setCreatorId(1L);
          platformManageMapper.insertServicePlatform(dto.getPlatform());

          gateway.setGatewayId(IDUtils.nextId());
          gateway.setPlatformId(platformId);
          gateway.setCreatorId(1L);
          gatewayManageMapper.batchInsertServiceGateway(Collections.singletonList(gateway));
        }

        SkillServiceDTO service = serviceManageMapper.getSkillService(tenantId, dto.getServiceId(), null);
        if (service != null) {
          service.setServiceName(dto.getServiceName());
          service.setServiceCode(dto.getServiceCode());
          service.setReqMethod(dto.getReqMethod());
          service.setHeaderJson(dto.getHeaderJson());
          service.setPathJson(dto.getPathJson());
          service.setQueryJson(dto.getQueryJson());
          service.setBodyJson(dto.getBodyJson());
          service.setResponseJson(dto.getResponseJson());
          service.setRelativePath(dto.getRelativePath());
          service.setCatalogItemId(dto.getCatalogItemId());
          service.setPlatformId(platformId);
          service.setUpdatorId(1L);
          serviceManageMapper.updateSkillService(service);
          // 刷新缓存
          ThreadPools.getCommon()
            .submit(() -> SpringUtil.getBean(IRefreshCacheService.class).refresh(CacheConsts.CACHE_NAME_API, "-1:" + service.getAppId()));
        }
        else {
          dto.setPlatformId(platformId);
          dto.setCreatorId(1L);
          serviceManageMapper.insertSkillService(dto);
        }
      }
    }
  }
}
