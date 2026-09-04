package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库 Entity
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_knowledge_base")
public class KnowledgeBaseEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long knowledgeId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录标识")
  private Long catalogItemId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户标识")
  private Long tenantId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "KNOWLEDGE_TYPE")
  @Schema(description = "知识库类型")
  private String knowledgeType;
  @DiffField(name = "KNOWLEDGE_NAME")
  @Schema(description = "知识库名称")
  @Size(max = 20, message = "知识库名称超过限定长度20")
  private String knowledgeName;
  @DiffField(name = "KNOWLEDGE_DESC")
  @Schema(description = "知识库描述")
  @Size(max = 500, message = "知识库描述超过限定长度500")
  private String knowledgeDesc;
  @DiffField(name = "KNOWLEDGE_ICON")
  @Schema(description = "知识库图标")
  private String knowledgeIcon;
  @DiffField(name = "FILE_COUNTS")
  @Schema(description = "文件数")
  private Long fileCounts;
  @DiffField(name = "KNOWLEDGE_STATUS")
  @Schema(description = "知识库状态: 10A未发布 10B发布中 10C已修改 10D已发布 10E发布失败")
  private String knowledgeStatus;
  @DiffField(name = "PUBLISH_TIME")
  @Schema(description = "发布时间;解析开始时间")
  private Date publishTime;
  @DiffField(name = "ERROR_MESSAGE")
  @Schema(description = "错误信息")
  private String errorMessage;
  @DiffField(name = "TOPIC_ID")
  @Schema(description = "主题 ID")
  private Long topicId;
  @DiffField(name = "KNOWLEDGE_STRATEGY")
  @Schema(description = "DOCCHAIN 策略")
  private String knowledgeStrategy;
  @DiffField(name = "IS_EXIST")
  @Schema(description = "Dochain 知识库是否已存在")
  private String isExist;
  @DiffField(name = "IS_TEMPORARY")
  @Schema(description = "是否临时的")
  private String isTemporary;
  @DiffField(name = "EXT_CONFIG_JSON")
  @Schema(description = "扩展配置 JSON 字符串")
  private String extConfigJson;

  @DiffField(name = "VISIBILITY_SCOPE")
  @Schema(description = "可见范围：PRIVATE-私有，PUBLIC-全员可见，MEMBERS-成员可见")
  private String visibilityScope;
  @DiffField(name = "COLOR")
  @Schema(description = "颜色")
  private String color;
  @DiffField(name = "KB_CODE")
  @Schema(description = "知识库编码")
  private String kbCode;


  @DiffField(name = "OWNER_ID")
  @Schema(description = "知识库所有者")
  private Long ownerId;
}
