package com.iwhalecloud.bote.beyond;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.beyond.BeyondFileDTO;
import com.iwhalecloud.bote.entity.beyond.BeyondFileEntity;
import com.iwhalecloud.bote.mapper.beyond.BeyondFileMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 百应文件辅助类
 *
 * @author bianjp
 * @since 2025-07-19
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
@RequiredArgsConstructor
public class BeyondFileHelper {
  private final BeyondFileMapper beyondFileMapper;
  private final BeyondApiClient beyondApiClient;
  private final IFileStoreService fileStoreService;

  /**
   * 下载百应的文件，上传到我们的文件服务器
   *
   * <p>记录百应文件与博特文件的关联关系，已经上传的文件不重复上传</p>
   */
  @Nullable
  public List<Long> uploadFiles(Long tenantId, List<BeyondFileDTO> files) {
    if (CollectionUtils.isEmpty(files)) {
      return null;
    }
    // 校验参数
    validateFiles(files);

    // 查询已经上传过的文件，避免重复上传
    List<Long> beyondFileIds = files.stream().map(BeyondFileDTO::getFileId).distinct().collect(Collectors.toList());
    // 百应文件 ID -> 博特文件 ID 映射
    Map<Long, Long> uploadedFileIdMap = beyondFileMapper.selectFileIdsByBeyondFileIds(beyondFileIds).stream()
      .collect(Collectors.toMap(BeyondFileEntity::getBeyondFileId, BeyondFileEntity::getFileId, (a, b) -> a));

    // 博特的文件 ID 列表
    List<Long> fileIds = new ArrayList<>(files.size());
    for (BeyondFileDTO file : files) {
      Long fileId = uploadedFileIdMap.get(file.getFileId());
      // 没上传过，上传文件
      if (fileId == null) {
        UploadConfigVO uploadConfig = new UploadConfigVO();
        uploadConfig.setSubFolder("beyond/chat");
        fileId = uploadFile(tenantId, file, uploadConfig);
      }
      fileIds.add(fileId);
    }

    return fileIds;
  }

  /**
   * 上传单个文件
   */
  private Long uploadFile(Long tenantId, BeyondFileDTO file, UploadConfigVO uploadConfig) {
    // 百应传的 fileType 不太准确，我们自己提取文件扩展名
    String extension = StringUtils.trimToNull(FilenameUtils.getExtension(file.getFileName()));
    uploadConfig.setOriginalFileName(file.getFileName());
    uploadConfig.setFileType(extension);
    uploadConfig.setIsPicture(FileTypeUtil.isPicture(extension));

    // 下载文件
    File tmpFile = beyondApiClient.downloadFile(file.getFileUrl());
    try {
      uploadConfig.setFileSize(tmpFile.length());
      // 上传文件
      FileInfoVO fileInfo = fileStoreService.uploadFile(tmpFile, uploadConfig);

      // 记录关联关系
      BeyondFileEntity entity = new BeyondFileEntity();
      entity.setId(IDUtils.nextId());
      entity.setTenantId(tenantId);
      entity.setBeyondFileId(file.getFileId());
      entity.setFileId(fileInfo.getFileId());
      entity.setStatusCd(BaseConsts.STATUS_CD_VALID);
      entity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      // 使用单独的事务，确保上传成功后关联关系一定会记录
      TransactionUtil.executeNew(() -> beyondFileMapper.insert(entity));

      return fileInfo.getFileId();
    }
    finally {
      FileUtils.deleteQuietly(tmpFile);
    }
  }

  /**
   * 校验文件参数
   */
  private void validateFiles(List<BeyondFileDTO> files) {
    for (BeyondFileDTO file : files) {
      Assert.notNull(file.getFileId(), "文件 ID 不能为空");
      Assert.hasText(file.getFileName(), "文件名称不能为空");
      Assert.hasText(file.getFileUrl(), "文件下载地址不能为空");
      Assert.hasLength(file.getFileUrl(), "文件下载地址不能为空");
      //noinspection HttpUrlsUsage
      Assert.isTrue(file.getFileUrl().startsWith("http://") || file.getFileUrl().startsWith("https://"), "文件下载地址不合法");
    }
  }
}
