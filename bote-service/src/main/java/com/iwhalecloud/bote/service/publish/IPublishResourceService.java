package com.iwhalecloud.bote.service.publish;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogItem;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailRequest;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRequest;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordQueryParams;
import com.iwhalecloud.bote.dto.publish.ResourceUnpublishRequest;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 发布资源服务接口
 *
 * @author lizuyin
 * @since 2025-07-23
 */
public interface IPublishResourceService {

  /**
   * 查询发布目录树
   *
   * @param request 目录检索请求参数
   * @return 目录树响应数据
   */
  ResultVO<List<BeyondCatalogItem>> queryCatalogTree(BeyondCatalogTreeRequest request);

  /**
   * 查询组织树信息
   *
   * @param request 组织检索请求参数
   * @return 组织树响应数据
   */
  ResultVO<List<BeyondOrgItem>> getOrgTree(BeyondOrgTreeRequest request);

  /**
   * 查询组织管理员信息
   *
   * @param request 组织管理员检索请求参数
   * @return 组织管理员响应数据
   */
  ResultVO<List<BeyondOrgAdminItem>> getOrgAdmin(BeyondOrgAdminRequest request);

  /**
   * 查询组织详情信息
   *
   * @param request 组织详情检索请求参数
   * @return 组织详情响应数据
   */
  ResultVO<BeyondOrgDetailItem> getOrgDetail(BeyondOrgDetailRequest request);

  /**
   * 百应平台发布资源
   *
   * @param request 资源发布请求参数
   * @return 发布结果
   */
  ResultVO<?> beyondPublishResource(ResourcePublishRequest request);

  /**
   * 微信公众号发布资源
   *
   * @param request 资源发布请求参数
   * @return 发布结果
   */
  ResultVO<String> wechatPublishResource(ResourcePublishRequest request);

  /**
   * 企业微信发布资源
   *
   * @param request 资源发布请求参数
   * @return 发布结果
   */
  ResultVO<String> weWorkPublishResource(ResourcePublishRequest request);

  /**
   * 钉钉机器人发布资源
   *
   * @param request 资源发布请求参数
   * @return 发布结果
   */
  ResultVO<String> dingTalkPublishResource(ResourcePublishRequest request);

  /**
   * 飞书机器人发布资源
   *
   * @param request 资源发布请求参数
   * @return 发布结果
   */
  ResultVO<String> feishuPublishResource(ResourcePublishRequest request);

  /**
   * 根据回调编码获取发布记录
   *
   * @param callbackCode 回调编码
   * @return 发布记录
   */
  ResourcePublishRecordDTO getRecordByCallbackCode(String callbackCode);

  /**
   * 智能体发布为 A2A
   */
  ResultVO<String> a2aPublishResource(ResourcePublishRequest request);

  /**
   * 取消发布资源
   */
  ResultVO<Void> unpublishResource(ResourceUnpublishRequest request);

  /**
   * 查询指定资源的发布记录
   */
  List<ResourcePublishRecordDTO> queryPublishRecordsByResourceId(Long tenantId, String resourceType, Long resourceId);

  /**
   * 分页查询资源发布记录
   *
   * @param params 查询参数
   * @return 分页结果
   */
  PageInfo<ResourcePublishRecordDTO> queryResourcePublishRecordPage(ResourcePublishRecordQueryParams params);

  /**
   * 根据资源和渠道查询发布记录
   *
   * @param tenantId 租户ID
   * @param resourceId 资源ID
   * @param publishChannel 发布渠道
   * @return 发布记录
   */
  @Nullable
  ResourcePublishRecordDTO getRecordByResourceAndChannel(Long tenantId, Long resourceId, String publishChannel);
}
