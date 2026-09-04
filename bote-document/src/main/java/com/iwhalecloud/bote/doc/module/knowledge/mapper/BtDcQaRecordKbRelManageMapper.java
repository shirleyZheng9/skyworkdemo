package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordKbRelDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 问答记录知识库关系表管理
 *
 * @author linmengfan
 * @since 2025-09-15
 */
public interface BtDcQaRecordKbRelManageMapper {

  /**
   * 新增问答记录知识库关系表
   *
   * @param btDcQaRecordKbRel 问答记录知识库关系表
   * @return 结果
   */
  int insertBtDcQaRecordKbRel(@Param("dto") BtDcQaRecordKbRelDTO btDcQaRecordKbRel);

  /**
   * 批量新增问答记录知识库关系表
   *
   * @param btDcQaRecordKbRels 问答记录知识库关系表列表
   * @return 结果
   */
  int batchInsertBtDcQaRecordKbRel(@Param("list") List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels);

  /**
   * 删除属性
   *
   * @param qaId 文档id
   * @param tenantId 租户 ID
   * @return 结果
   */
  int deleteBtDcQaRecordKbRel(@Param("qaId") Long qaId, @Param("tenantId") Long tenantId);

  /**
   * 根据知识库ID删除问答与知识库的关联关系
   *
   * @param qaId 问答记录ID
   * @param knowledgeId 知识库ID
   * @param tenantId 租户 ID
   * @return 结果
   */
  int deleteBtDcQaRecordKbRelByKnowledgeId(@Param("qaId") Long qaId, @Param("knowledgeId") Long knowledgeId, @Param("tenantId") Long tenantId);

}
