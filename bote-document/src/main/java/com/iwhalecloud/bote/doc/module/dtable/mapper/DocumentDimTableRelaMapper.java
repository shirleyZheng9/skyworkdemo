package com.iwhalecloud.bote.doc.module.dtable.mapper;

import com.iwhalecloud.bote.doc.module.dtable.entity.DocumentDimTableRelaEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档与多维表格关联关系相关数据库操作
 *
 * @author auto
 * @since 2026-01-09
 */
public interface DocumentDimTableRelaMapper {

  /**
   * 根据文档ID查询
   *
   * @param documentId 文档唯一编码
   * @return 关联关系实体
   */
  List<DocumentDimTableRelaEntity> selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 插入记录
   *
   * @param rela 关联关系实体
   * @return 插入数量
   */
  int insert(@Param("rela") DocumentDimTableRelaEntity rela);

  /**
   * 根据文档ID逻辑删除
   *
   * @param documentId 文档唯一编码
   * @return 更新数量
   */
  int deleteByDocumentId(@Param("documentId") String documentId);
}
