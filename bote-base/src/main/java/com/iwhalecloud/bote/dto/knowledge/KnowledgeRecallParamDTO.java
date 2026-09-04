package com.iwhalecloud.bote.dto.knowledge;

import java.util.List;
import org.springframework.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回请求参数
 *
 * @author bianjp
 * @since 2024-09-27
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeRecallParamDTO {
  /** 租户 ID */
  private Long tenantId;
  /** 空间 ID */
  private Long spaceId;
  /** 知识库 ID */
  private Long knowledgeId;
  /** 知识库 */
  private SimpleKnowledgeDTO knowledge;
  /** 问句 */
  private String query;
  /** 返回片段数量 */
  private Integer maxNum;
  /** 最低分数。范围为 0~1，大于这个分数的片段才会返回 */
  private Float minScore;
  /** 知识库列表(外系统接入,检索和问答支持多个知识库, 百应 知识中台使用) */
  private List<Long> knowledgeIds;
  /** 文档列表 */
  private List<Long> documentIds;
  /** 知识库主题id列表 */
  private List<String> topicIds;
  /** docchain对应文档id列表 */
  private List<String> docIds;
  /** 文档列表 (外系统接入 知识中台使用)*/
  private List<ResourceExtItem> resourceItems;
  /** 知识库列表 */
  private List<SimpleKnowledgeDTO> knowledgeList;
  /**
   * WeKnora 知识库 ID 列表（编排等场景可直接传 WeKnora 侧 ID，无需 {@link #knowledgeList} / knowledgeTypeExt）
   */
  @Nullable
  private List<String> weKnoraKnowledgeBaseIds;
  /** knowledgeGraph 数据库名, 仅支持单个*/
  @Nullable
  private String knowledgeGraphName;
}
