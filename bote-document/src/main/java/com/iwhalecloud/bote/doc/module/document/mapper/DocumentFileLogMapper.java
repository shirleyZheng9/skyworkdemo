package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentFileLogEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档上传文件日志表数据库操作
 *
 * @author bote-doc
 * @since 2026-04-15
 */
public interface DocumentFileLogMapper {

  /**
   * 插入一条日志
   *
   * @param log 日志实体
   * @return 影响行数
   */
  int insert(@Param("log") DocumentFileLogEntity log);

  /**
   * 按日志主键查询一条记录，并关联文档表、文件表补充名称与内容来源
   *
   * @param id 日志主键
   * @param tenantId 租户 ID
   * @return 明细 DTO，不存在时返回 null
   */
  DocumentFileLogDTO selectDetailById(@Param("id") Long id, @Param("tenantId") Long tenantId);

  /**
   * 按文档 ID 查询有效日志（关联文档表、文件表），按创建时间倒序
   *
   * @param documentId 文档 ID
   * @param tenantId 租户 ID
   * @return 列表，无记录时返回空列表
   */
  List<DocumentFileLogDTO> selectDetailListByDocumentId(@Param("documentId") String documentId, @Param("tenantId") Long tenantId);
}
