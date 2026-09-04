package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.AttachmentsEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档附件相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface AttachmentsMapper {

  /**
   * 根据主键查询
   */
  AttachmentsEntity selectByPrimaryKey(@Param("id") Long id);

  /**
   * 根据文档ID查询所有附件
   */
  List<AttachmentsEntity> selectByDocId(@Param("docId") String docId);

  /**
   * 根据文件ID查询
   */
  AttachmentsEntity selectByFileId(@Param("fileId") Long fileId);

  /**
   * 插入记录
   */
  int insert(@Param("entity") AttachmentsEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("id") Long id);
}
