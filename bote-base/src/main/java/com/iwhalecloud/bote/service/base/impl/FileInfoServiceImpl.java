package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bss.litchi.file.service.IFileInfoService;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreProcessor;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 文件资源服务
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class FileInfoServiceImpl implements IFileInfoService {
  private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);

  private final BoteFileInfoMapper fileInfoMapper;

  @Override
  @Nullable
  public FileInfoVO getFileInfoById(Long fileId) {
    return fileInfoMapper.getFileInfoById(fileId);
  }

  @Override
  public List<FileInfoVO> queryFileInfoByIds(List<Long> fileIds) {
    if (CollectionUtils.isEmpty(fileIds)) {
      return Collections.emptyList();
    }
    return fileInfoMapper.queryFileInfoByIds(fileIds);
  }

  @Override
  public int insertFileInfo(FileInfoVO fileInfo) {
    return fileInfoMapper.insertFileInfo(fileInfo);
  }

  @Override
  public int markDropFileInfo(Long fileId) {
    return fileInfoMapper.markDropFileInfo(fileId);
  }

  @Override
  public Long getNextFileId() {
    return IDUtils.nextId();
  }

  /**
   * 清理文件
   *
   * <p>删除文件需要频繁连接文件服务器，耗时较长且可能不稳定，因此其它定时任务中不适合直接删除文件，应该先标记为禁用(statusCd=00D), 然后由此任务负责删除</p>
   */
  public void clearFile() {
    int batchSize = 100;
    int maxBatchCount = 1000;
    int total = 0;
    int success = 0;
    // 分批删除
    for (int i = 0; i < maxBatchCount; i++) {
      List<FileInfoVO> files = fileInfoMapper.selectDisabledFiles(batchSize);
      if (files.isEmpty()) {
        break;
      }
      total += files.size();
      // 从文件服务器删除文件
      success += deleteFiles(files);
      // 删除文件记录
      fileInfoMapper.deleteDisabledFilesByIds(files.stream().map(FileInfoVO::getFileId).collect(Collectors.toList()));
      if (files.size() < batchSize) {
        break;
      }
    }
    logger.debug("Clear file finished: total={}, success={}", total, success);
  }

  /**
   * 删除文件
   */
  private int deleteFiles(List<FileInfoVO> files) {
    int successCount = 0;
    for (FileInfoVO file : files) {
      try {
        // 文件状态为禁用，因此无法使用 IFileStoreService.deleteFileQuietly, 需要使用底层的 IFileStoreProcessor
        IFileStoreProcessor processor = FileStoreUtils.getStoreProcessor(file.getStoreType());
        processor.delete(file.getFilePathInServer());
        successCount++;
      }
      catch (Exception e) {
        // 忽略删除失败
        logger.error("Failed to delete file: fileId={}", file.getFileId(), e);
      }
    }
    return successCount;
  }

}
