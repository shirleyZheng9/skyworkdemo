package com.iwhalecloud.bote.service.skill.impl.helper;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.ontology.OntoSceneDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyActionDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyAppDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyObjectDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyRuleDTO;
import com.iwhalecloud.bote.dto.ontology.query.OntologyQueryParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 增加对本体平台 api的调用
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Component
public class OntologyApiHelper {

  /** 本体应用分页查询接口地址 */
  private static final String ONTOLOGY_APP_PAGE_URL = "/bote/manager/ontoApp/queryOntoAppPage";
  /** 本体场景分页查询接口地址 */
  private static final String ONTOLOGY_SCENE_PAGE_URL = "/bote/manager/ontoScene/queryOntoScenePage";
  /** 本体场景详情接口地址 */
  private static final String ONTOLOGY_SCENE_DETAIL_URL = "/bote/manager/ontoScene/findOntoScene";
  /** 本体对象分页查询接口地址 */
  private static final String ONTOLOGY_OBJECT_PAGE_URL = "/bote/manager/ontoObjectType/queryOntoObjectTypePage";
  /** 本体动作分页查询接口地址 */
  private static final String ONTOLOGY_ACTION_PAGE_URL = "/bote/manager/ontoAction/queryOntoActionPage";
  /** 本体规则分页查询接口地址 */
  private static final String ONTOLOGY_RULE_PAGE_URL = "/bote/manager/ontoRule/queryOntoRulePage";

  /**
   * 查询本体应用分页列表
   */
  public PageInfo<OntologyAppDTO> queryOntologyAppPage(OntologyQueryParams params) {
    String queryOntologyAppPageUrl = getOntologyApiUrl() + ONTOLOGY_APP_PAGE_URL;
    HttpHeaders headers = buildHttpHeaders(params.getTenantId(), null);
    ResultVO<PageInfo<OntologyAppDTO>> result = HttpUtil.post(queryOntologyAppPageUrl, params, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体应用分页列表失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体应用分页列表失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 查询本体场景列表
   */
  public PageInfo<OntoSceneDTO> queryOntologyScenePage(OntologyQueryParams params) {
    String queryOntologyScenePageUrl = getOntologyApiUrl() + ONTOLOGY_SCENE_PAGE_URL;
    HttpHeaders headers = buildHttpHeaders(params.getTenantId(), params.getAppId());
    ResultVO<PageInfo<OntoSceneDTO>> result = HttpUtil.post(queryOntologyScenePageUrl, params, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体场景分页列表失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体场景分页列表失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 查询本体场景详情
   */
  public OntoSceneDTO getOntologySceneDetail(OntologyQueryParams queryParams) {
    String getOntologySceneDetailUrl = getOntologyApiUrl() + ONTOLOGY_SCENE_DETAIL_URL + "?sceneId=" + queryParams.getSceneId();
    HttpHeaders headers = buildHttpHeaders(queryParams.getTenantId(), queryParams.getAppId());
    ResultVO<OntoSceneDTO> result = HttpUtil.get(getOntologySceneDetailUrl, null, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体场景详情失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体场景详情失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 查询本体应用规则分页列表
   */
  public PageInfo<OntologyRuleDTO> queryOntologyRulePage(OntologyQueryParams params) {
    String queryOntologyRulePageUrl = getOntologyApiUrl() + ONTOLOGY_RULE_PAGE_URL;
    HttpHeaders headers = buildHttpHeaders(params.getTenantId(), params.getAppId());
    ResultVO<PageInfo<OntologyRuleDTO>> result = HttpUtil.post(queryOntologyRulePageUrl, params, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体规则分页列表失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体规则分页列表失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 查询本体应用对象分页列表
   */
  public PageInfo<OntologyObjectDTO> queryOntologyObjectPage(OntologyQueryParams params) {
    String queryOntologyObjectPageUrl = getOntologyApiUrl() + ONTOLOGY_OBJECT_PAGE_URL;
    HttpHeaders headers = buildHttpHeaders(params.getTenantId(), params.getAppId());
    ResultVO<PageInfo<OntologyObjectDTO>> result = HttpUtil.post(queryOntologyObjectPageUrl, params, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体对象分页列表失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体对象分页列表失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 查询本体应用动作分页列表
   */
  public PageInfo<OntologyActionDTO> queryOntologyActionPage(OntologyQueryParams params) {
    String queryOntologyActionPageUrl = getOntologyApiUrl() + ONTOLOGY_ACTION_PAGE_URL;
    HttpHeaders headers = buildHttpHeaders(params.getTenantId(), params.getAppId());
    ResultVO<PageInfo<OntologyActionDTO>> result = HttpUtil.post(queryOntologyActionPageUrl, params, new ParameterizedTypeReference<>() {
    }, headers);
    if (result == null) {
      throw new BssException("查询本体动作分页列表失败, 响应为空");
    }
    if (!result.isSuccess()) {
      throw new BssException("查询本体动作分页列表失败" + result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 获取本体平台API基础地址
   *
   * @return 本体平台API基础地址
   */
  private String getOntologyApiUrl() {
    String ontologyBaseUrl = SystemParameter.ONTOLOGY_BASE_URL.getValueFromDb();
    if (StringUtils.isBlank(ontologyBaseUrl)) {
      throw new BssException("未配置本体平台基础地址: ONTOLOGY_BASE_URL");
    }
    return StringUtils.stripEnd(ontologyBaseUrl, "/");
  }

  /**
   * 构建HTTP请求头
   */
  private HttpHeaders buildHttpHeaders(@Nullable Long tenantId, @Nullable Long appId) {
    HttpHeaders headers = new HttpHeaders();
    String token =  SystemParameter.ONTOLOGY_API_KEY.getValueFromDb();
    if (StringUtils.isEmpty(token)) {
      throw new BssException("未配置本体平台API密钥: ONTOLOGY_API_KEY");
    }
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    if (tenantId != null) {
      headers.set("Tenant-Id", String.valueOf(tenantId));
    }
    if (appId != null) {
      headers.set("APP-ID", String.valueOf(appId));
    }
    return headers;
  }
}
