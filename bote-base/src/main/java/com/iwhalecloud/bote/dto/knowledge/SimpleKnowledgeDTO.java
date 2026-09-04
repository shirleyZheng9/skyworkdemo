package com.iwhalecloud.bote.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * 知识库简单信息
 *
 * @author bianjp
 * @since 2025-04-24
 */
@Getter
@Setter
@ToString
public class SimpleKnowledgeDTO {
  /** 知识库 ID */
  private Long knowledgeId;
  /** 知识库名称 */
  private String knowledgeName;
  /** 知识库类型 */
  private String knowledgeType;
  /** 是否是通用的主题类型，仅用于 DocChain */
  private Boolean commonTopic;
  /** DocChain 主题 ID */
  private String topicId;
  /** 知识库拥有者 ID */
  private Long ownerId;
  /** 租户ID */
  private Long tenantId;
  /** 空间 ID */
  private Long spaceId;
  /** 可见范围：PRIVATE-私有，PUBLIC-全员可见，MEMBERS-成员可见 */
  private String visibilityScope;
  /** 知识库类型扩展配置（具体内容取决于知识库类型） */
  private Map<String, Object> knowledgeTypeExt;
  /** 关联文档列表 */
  private List<SimpleDocumentDTO> documents;
  /** docchain 知识库是否已存在*/
  private String isExist;
  /** 目录标识 */
  private Long catalogItemId;
  /** 知识库编码 */
  private String kbCode;

  // 查询数据库使用，不存储到缓存中
  @JsonIgnore
  private String knowledgeStrategy;
  @JsonIgnore
  private String extConfigJson;

  /**
   * 解析扩展配置
   */
  public void parseExtConfig() {
    commonTopic = checkIsCommonTopic();
    if (StringUtils.isNotEmpty(extConfigJson)) {
      KnowledgeExtConfigDTO extConfig = JsonUtil.parseJsonRequired(extConfigJson, KnowledgeExtConfigDTO.class);
      knowledgeTypeExt = extConfig.getKnowledgeTypeExt();
    }
    knowledgeStrategy = null;
    extConfigJson = null;
  }

  /**
   * 检查是否是通用的主题类型
   */
  private boolean checkIsCommonTopic() {
    if (Strings.CS.contains(knowledgeStrategy, "chat_with_neo4j")) {
      try {
        JsonNode node = JsonUtil.readTree(knowledgeStrategy);
        String topicType = node.path("chat_with_neo4j").asText(null);
        return StringUtils.isEmpty(topicType) || KnowledgeConsts.TOPIC_TYPE_COMMON.equalsIgnoreCase(topicType);
      }
      catch (Exception e) {
        // 忽略解析错误
      }
    }
    // 历史数据可能策略为空，或者策略中没有 chat_with_neo4j 配置，都当作是通用主题
    return true;
  }
  @JsonIgnore
  public boolean isRelated() {
    return CommonConsts.TRUE.equals(getIsExist());
  }
}
