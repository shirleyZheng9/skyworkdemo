package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文件资源管理 Mapper
 *
 * <p>注意: bean 名称需要避免和 {@link com.iwhalecloud.bss.litchi.file.mapper.FileInfoMapper} 冲突</p>
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
public interface BoteFileInfoMapper {

  /**
   * 根据文件信息 ID 获取文件信息
   *
   * @param fileInfoId 文件 ID
   * @return 文件信息
   */
  FileInfoVO getFileInfoByFileInfoId(@Param("fileInfoId") Long fileInfoId, @Param("tenantId") Long tenantId);

  /**
   * 根据文件 ID 获取文件信息
   *
   * @param fileId 文件 ID
   * @return 文件信息
   */
  FileInfoVO getFileInfoById(@Param("fileId") Long fileId);

  /**
   * 根据文件 ID 集合获取文件信息
   *
   * @param fileIds 文件 ID 集合
   * @return 文件信息列表
   */
  List<FileInfoVO> queryFileInfoByIds(@Param("fileIds") List<Long> fileIds);

  /**
   * 新增文件信息
   *
   * @param fileInfo 文件信息
   * @return 结果
   */
  int insertFileInfo(@Param("dto") FileInfoVO fileInfo);

  /**
   * 删除文件信息
   *
   * @param fileId 文件 ID
   * @return 结果
   */
  int markDropFileInfo(@Param("fileId") Long fileId);

  /**
   * 查询禁用的文件
   */
  List<FileInfoVO> selectDisabledFiles(@Param("limit") int limit);

  /**
   * 批量删除禁用的文件
   */
  int deleteDisabledFilesByIds(@Param("fileIds") List<Long> fileIds);

  /**
   * 更新文件名称
   */
  void updateFileName(@Param("fileId") Long fileId, @Param("fileName") String fileName);

  /**
   * 更新文件信息
   *
   * @param fileInfo 文件信息
   * @return 结果
   */
  int updateFileInfo(@Param("dto") FileInfoVO fileInfo);
}
