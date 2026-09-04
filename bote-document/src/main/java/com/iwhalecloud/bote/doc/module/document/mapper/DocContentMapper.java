package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.dto.DcDocContentDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocContentEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 文档内容相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface DocContentMapper {

  /**
   * 根据主键查询
   */
  DcDocContentEntity selectByPrimaryKey(@Param("id") Long id);

  /**
   * 根据文档ID查询内容
   */
  DcDocContentEntity selectByDocId(@Param("docId") String docId);

  /**
   * 根据文档ID查询内容
   */
  DcDocContentDTO selectDTOByDocId(@Param("docId") String docId);

  /**
   * 插入记录
   */
  int insert(@Param("entity") DcDocContentEntity entity);

  /**
   * 更新记录
   */
  int updateByPrimaryKey(@Param("entity") DcDocContentEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("id") Long id);
}
