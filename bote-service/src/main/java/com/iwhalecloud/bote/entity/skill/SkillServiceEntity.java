package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：API Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_service")
public class SkillServiceEntity extends BaseEntity {
  @DiffId
  @Schema(description = "服务ID")
  private Long serviceId;
  @DiffField(name = "SERVICE_CODE")
  @Schema(description = "服务编码")
  @Size(max = 50, message = "服务编码超过限定长度50")
  private String serviceCode;
  @DiffField(name = "SERVICE_NAME")
  @Schema(description = "服务名称")
  @Size(max = 20, message = "服务名称超过限定长度20")
  private String serviceName;
  @DiffField(name = "RELATIVE_PATH")
  @Schema(description = "相对路径")
  private String relativePath;
  @DiffField(name = "PLATFORM_ID")
  @Schema(description = "平台定义ID")
  private Long platformId;
  @DiffField(name = "REQ_METHOD")
  @Schema(description = "请求方式")
  private String reqMethod;
  @DiffField(name = "HEADER_JSON")
  @Schema(description = "header参数json")
  private String headerJson;
  @DiffField(name = "PATH_JSON")
  @Schema(description = "path参数json")
  private String pathJson;
  @DiffField(name = "QUERY_JSON")
  @Schema(description = "query参数json")
  private String queryJson;
  @DiffField(name = "BODY_JSON")
  @Schema(description = "body参数json")
  private String bodyJson;
  @DiffField(name = "RESPONSE_JSON")
  @Schema(description = "响应参数")
  private String responseJson;
  @DiffField(name = "MOCK_RESPONSE_JSON")
  @Schema(description = "模拟响应报文")
  private String mockResponseJson;
  @DiffField(name = "IS_MOCK")
  @Schema(description = "是否开启模拟: T:是, F:否")
  private String isMock;
  @DiffField(name = "IS_SSE")
  @Schema(description = "是否是 SSE 接口")
  private String isSse;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
  @Schema(description = "请求体类型, 如Multipart/form-data")
  @DiffField(name = "BODY_TYPE")
  private String bodyType;
  @DiffField(name = "POST_SCRIPT")
  @Schema(description = "后置脚本内容")
  private String postScript;
  @DiffField(name = "CONNECT_TIMEOUT")
  @Schema(description = "请求超时时间（毫秒），示例值：5000")
  private Integer connectTimeout;
  @DiffField(name = "READ_TIMEOUT")
  @Schema(description = "读取超时时间（毫秒），示例值：15000")
  private Integer readTimeout;
  @DiffField(name = "IS_ENCRYPT")
  @Schema(description = "是否接口加密")
  private String isEncrypt;
}
