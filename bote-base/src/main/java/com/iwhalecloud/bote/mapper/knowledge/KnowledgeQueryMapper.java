package com.iwhalecloud.bote.mapper.knowledge;

import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 知识库相关查询接口
 *
 * @author chen.linfa
 * @since 2025-11-08
 */
public interface KnowledgeQueryMapper {

  /**
   * 查询知识库简单信息
   */
  @Nullable
  SimpleKnowledgeDTO selectSimpleKnowledgeById(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 批量查询知识库简单信息
   */
  List<SimpleKnowledgeDTO> selectSimpleKnowledgeByIds(@Param("tenantId") Long tenantId, @Param("knowledgeIds") List<Long> knowledgeIds);

  /**
   * 查询简单文档列表
   */
  List<SimpleDocumentDTO> selectSimpleDocumentById(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 查询简单文档列表
   */
  List<SimpleDocumentDTO> selectSimpleDocumentByIds(@Param("tenantId") Long tenantId, @Param("knowledgeIds") List<Long> knowledgeIds);
}
