package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.query.FileInfoQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文件信息管理
 *
 * @author auto
 * @since 2024-09-24
 */
public interface FileInfoManageMapper {

  /**
   * 根据主键获取文件信息
   */
  FileInfoDTO getFileInfo(@Param("tenantId") Long tenantId, @Param("id") Long fileInfoId);

  /**
   * 新增文件信息
   *
   * @param fileInfo 文件信息
   * @return 结果
   */
  int insertFileInfo(@Param("dto") FileInfoDTO fileInfo);

  /**
   * 修改文件 ID
   */
  int updateFileIdByFileInfoId(@Param("tenantId") Long tenantId, @Param("fileInfoId") Long fileInfoId, @Param("fileId") Long fileId, @Param("fileName") String fileName, @Param("updatorId") Long updatorId);

  /**
   * 删除文件信息
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteFileInfo(@Param("tenantId") Long tenantId, @Param("fileInfoId") Long fileInfoId, @Param("updatorId") Long updatorId);

  /**
   * 获取文件信息列表
   *
   * @param queryParams 查询条件
   * @return 文件信息列表
   */
  List<FileInfoDTO> selectFileInfoList(@Param("query") FileInfoQueryParams queryParams);

  /**
   * 获取文件信息列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文件信息分页列表
   */
  Page<FileInfoDTO> selectFileInfoPage(@Param("query") FileInfoQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据文件名称获取文件
   *
   * @param fileName 文件名称
   * @param tenantId 租户ID
   * @return 结果
   */
  FileInfoDTO getFileInfoByName(@Param("fileName") String fileName, @Param("tenantId") Long tenantId, @Param("busiType") String busiType);

  /**
   * 是否存在关联的数据
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 文件 ID
   * @param busiType 业务类型
   */
  boolean existsRelatedData(@Param("tenantId") Long tenantId, @Param("fileInfoId") Long fileInfoId, @Param("busiType") String busiType,
    @Param("template") String template);

  /**
   * 批量查询是否存在关联数据
   *
   * @param tenantId 租户ID
   * @param fileInfoIds 文件信息ID列表
   * @param busiType 业务类型
   * @return 存在关联数据的文件信息列表
   */
  List<FileInfoDTO> batchExistsRelatedData(@Param("tenantId") Long tenantId, @Param("fileInfoIds") List<Long> fileInfoIds, @Param("busiType") String busiType);

  /**
   * 更新文件信息
   *
   * @param fileInfo 文件信息
   * @return 更新结果
   */
  int updateFileInfo(@Param("dto") FileInfoDTO fileInfo);

  /**
   * 更新文件名称
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 文件信息ID
   * @param fileName 文件名称
   */
  void updateFileName(@Param("tenantId") Long tenantId, @Param("fileInfoId") Long fileInfoId, @Param("fileName") String fileName);

  /**
   * 根据文件ID和租户ID获取文件信息
   *
   * @param fileId 文件ID
   * @param tenantId 租户ID
   * @return 文件信息
   */
  FileInfoDTO getFileInfoByFileIdAndTenantId(@Param("fileId") Long fileId, @Param("tenantId") Long tenantId);
}
