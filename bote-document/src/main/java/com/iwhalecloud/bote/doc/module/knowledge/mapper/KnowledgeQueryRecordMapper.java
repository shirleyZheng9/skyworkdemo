package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.iwhalecloud.bote.doc.module.knowledge.entity.KnowledgeQueryRecordEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库查询记录
 *
 * @author bianjp
 * @since 2024-09-28
 */
public interface KnowledgeQueryRecordMapper {
  /**
   * 插入查询记录
   */
  int insertQueryRecord(@Param("dto") KnowledgeQueryRecordEntity queryRecord);

  /**
   * 统计查询记录数量
   */
  int countQueryRecords(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId, @Param("querySource") String querySource);

  /**
   * 查询查询内容列表
   */
  List<String> selectQueryContentList(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId, @Param("querySource") String querySource);

}

