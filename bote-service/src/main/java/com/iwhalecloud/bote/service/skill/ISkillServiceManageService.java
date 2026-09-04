package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.CurlParamsDTO;
import com.iwhalecloud.bote.dto.skill.CurlParseResultDTO;
import com.iwhalecloud.bote.dto.skill.OpenApiInfoDTO;
import com.iwhalecloud.bote.dto.skill.ServiceApiDocsParams;
import com.iwhalecloud.bote.dto.skill.ServiceMockParams;
import com.iwhalecloud.bote.dto.skill.SimpleSkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 技能：API 服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillServiceManageService {

  /**
   * 保存 API
   *
   * @param service API
   * @return 结果
   */
  ResultVO<SkillServiceDTO> saveSkillService(SkillServiceDTO service);

  /**
   * 查询单个 API
   *
   * @param tenantId 租户 ID
   * @param serviceId API 主键
   * @return API
   */
  @Nullable
  SkillServiceDTO findSkillService(Long tenantId, Long serviceId);

  /**
   * 查询 API 列表
   *
   * @param queryParams 查询条件
   * @return API 列表
   */
  List<SimpleSkillServiceDTO> querySkillServiceList(SkillQueryParams queryParams);

  /**
   * 查询 API 列表（分页）
   *
   * @param queryParams 查询条件
   * @return API 分页列表
   */
  PageInfo<SkillServiceDTO> querySkillServicePage(SkillQueryParams queryParams);

  /**
   * 查询 API 列表（分页），精简参数，用于其他模块引用
   *
   * @param queryParams 查询条件
   * @return API 分页列表
   */
  PageInfo<SimpleSkillServiceDTO> querySimpleSkillServicePage(SkillQueryParams queryParams);

  /**
   * 删除 API
   *
   * @param tenantId 租户 ID
   * @param serviceId API ID
   * @return 结果
   */
  ResultVO<Void> deleteSkillService(Long tenantId, Long serviceId);

  /**
   * 解析请求地址详情
   *
   * @param serviceDocUrl api-docs 地址
   * @return 结果
   */
  ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerUrl(String serviceDocUrl);

  /**
   * 解析 swagger 文件
   *
   * @param file api-docs 文件
   * @return 结果
   */
  ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerFile(MultipartFile file);

  /**
   * 根据api路径或swagger文件导入api服务
   *
   * @param apiDocsParams 参数
   * @return API服务列表
   */
  ResultVO<List<SkillServiceDTO>> importSwaggerServiceList(@Nullable MultipartFile file, ServiceApiDocsParams apiDocsParams);

  /**
   * 从 OpenAPI JSON 字符串解析 API 信息
   *
   * @param openApiJson OpenAPI JSON 字符串
   * @return 解析结果
   */
  ResultVO<List<OpenApiInfoDTO>> parseServiceFromOpenApiJson(String openApiJson);

  /**
   * 启用服务模拟
   *
   * @param params 参数
   * @return 结果
   */
  ResultVO<Void> toggleServiceMock(ServiceMockParams params);

  /**
   * 解析curl命令并转换为SkillServiceDTO
   *
   * @param curlParams curl命令参数
   * @return 解析后的SkillServiceDTO
   */
  ResultVO<CurlParseResultDTO> parseCurlCommand(CurlParamsDTO curlParams);

  /**
   * 同步来自百应的 API 服务
   *
   * @param services API 集合
   * @param catalogId 不为空时，标识删除目录下的 API
   */
  void syncServiceFromBeyond(List<SkillServiceDTO> services, @Nullable Long catalogId);
}
