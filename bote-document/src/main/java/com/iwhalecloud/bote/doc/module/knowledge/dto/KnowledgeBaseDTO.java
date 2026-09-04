package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.entity.KnowledgeBaseEntity;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeExtConfigDTO;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 知识库 DTO
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class KnowledgeBaseDTO extends KnowledgeBaseEntity {
  @Schema(description = "文档列表")
  @DiffField(childNode = true)
  private List<DocumentDTO> documents;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "DOCCHAIN 策略")
  private Map<String, Object> strategy;
  @Schema(description = "文档类型")
  private String documentType;
  @Schema(description = "是否通用的主题类型")
  private Boolean isCommonTopic;
  @Schema(description = "知识库类型扩展配置（具体内容取决于知识库类型）")
  private Map<String, Object> knowledgeTypeExt;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "非结构化文件关联 ID 集合，用于新增知识库同时带有文档")
  private List<String> dcDocumentIds;
  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
  private String permissionType;
  @Schema(description = "是否置顶")
  private Boolean isTopPinned;
  @Schema(description = "是否收藏")
  private Boolean isFavorite;
  @Schema(description = "所有者名称")
  private String ownerName;
  @Schema(description = "非结构化文件关联 ID 集合，用于新增知识库同时带有文档")
  private List<Long> fileInfoIds;
  @Schema(description = "结构化类型的文件关联 ID 集合，用于新增知识库同时带有文档")
  private List<Long> structFileInfoIds;

  /**
   * 是否是DocChain知识库类型
   */
  @JsonIgnore
  public boolean isDocChainType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_DOC_CHAIN.equals(getKnowledgeType());
  }

  /**
   * 是否是关联类型的知识库
   */
  @JsonIgnore
  public boolean isRelated() {
    return DocBaseConsts.TRUE.equals(getIsExist());
  }

  /**
   * 解析扩展配置
   */
  public void parseExtConfig() {
    if (StringUtils.isNotEmpty(getExtConfigJson())) {
      KnowledgeExtConfigDTO extConfig = JsonUtil.parseJsonRequired(getExtConfigJson(), KnowledgeExtConfigDTO.class);
      knowledgeTypeExt = extConfig.getKnowledgeTypeExt();
    }
    // 不返回给前端
    setExtConfigJson(null);
  }

  /**
   * 保存扩展配置
   */
  public void saveExtConfig() {
    if (MapUtils.isNotEmpty(knowledgeTypeExt)) {
      KnowledgeExtConfigDTO extConfig = new KnowledgeExtConfigDTO();
      extConfig.setKnowledgeTypeExt(knowledgeTypeExt);
      setExtConfigJson(JsonUtil.toJsonStringCompact(extConfig));
    }
    else {
      setExtConfigJson(null);
    }
  }
}
