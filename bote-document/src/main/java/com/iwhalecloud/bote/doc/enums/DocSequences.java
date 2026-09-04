package com.iwhalecloud.bote.doc.enums;

import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 序列枚举类
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@RequiredArgsConstructor
@Getter
public enum DocSequences {

  /** 知识库查询记录 */
  KNOWLEDGE_QUERY_RECORD_ID("bt_knowledge_query_record", "id", "seq_knowledge_query_record_id"),
  /** 知识库权限表记录 */
  BT_DC_KB_PERMISSION_ID("bt_dc_kb_permission", "permission_Id", "seq_bt_dc_kb_permission_id"),
  /** 文件信息 */
  FILE_INFO_ID("bt_file_info", "file_info_id", "seq_file_info_id"),
  DOCUMENT_DOCUMENT_ID("bt_document", "document_id", "seq_document_document_id"),
  BT_DOCUMENT_APPROVAL_ID("bt_document_approval", "approval_id", "seq_bt_document_approval_id"),
  /** 文档分片 */
  DOCUMENT_SEGMENT_ID("bt_document_segment", "segment_id", "seq_document_segment_doc_seqment_id"),
  /** 知识库 */
  KNOWLEDGE_BASE_KNOWLEDGE_ID("bt_knowledge_base", "knowledge_id", "seq_knowledge_base_knowledge_id"),
  /** 知识库问答记录 */
  DC_QA_RECORD_ID("bt_dc_qa_record", "knowledge_id", "seq_dc_qa_record_id"),
  /** 知识库问答引用 */
  BDC_QA_CHUNK_REFERENCE_ID("bt_dc_qa_chunk_reference", "knowledge_id", "seq_dc_qa_chunk_reference_id"),
  /** 知识库问答引用 */
  BT_DC_QA_RECORD_KB_REL_ID("bt_dc_qa_record_kb_rel", "rel_id", "seq_bt_dc_qa_record_kb_rel_id"),
  /** 文档关键字 */
  DOCUMENT_KEYWORD_ID("bt_document_keyword", "keyword_id", "seq_document_keyword_keyword_id"),
  /** 分片关键字关系 */
  SEGMENT_KEYWORD_REL_ID("bt_segment_keyword_rel", "rel_id", "seq_segment_keyword_rel_rel_id"),
  /** 文档同义词 */
  DOCUMENT_SYNONYM_ID("bt_document_synonym", "synonym_id", "seq_document_synonym_id"),
  /** 文档关键字与同义词关系 */
  KEYWORD_SYNONYM_REL_ID("bt_keyword_synonym_rel", "rel_id", "seq_keyword_synonym_rel_id"),
  /** 文档参数 */
  DOCUMENT_PARAMETER_ID("bt_document_parameter", "parameter_id", "seq_document_parameter_id"),
//  /** 资源关联 */
//  RESOURCE_ELEMENT_ID("bt_resource_element", "resource_element_id", "seq_resource_element_id"),
  /** 文档内容 */
  DOCUMENT_CONTENT_ID("bt_document_content", "content_id", "seq_document_content_id"),
  /** 目录主键 */
  CATALOG_ID("bt_catalog", "catalog_id", "seq_catalog_id"),
  /** 不要使用，只放在最后方便维护枚举类 */
  DO_NOT_USE(null, null, null);

  /** 表名 */
  private final String table;

  /** 列名 */
  private final String column;

  /** 序列名 */
  private final String name;

  /**
   * 获取下个序列值
   *
   * @return 序列值
   */
  public long next() {
    // return SeqUtil.next(name);
    return IDUtils.nextId();
  }

}
