package com.iwhalecloud.bote.dto.orchestration.file;

import com.iwhalecloud.bote.common.util.TikaUtil;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 文件服务器中的文件
 *
 * @author bianjp
 * @since 2025-10-23
 */
@Getter
public final class FileServerFile extends AbstractFile {
  /** 文件信息 */
  private final FileInfoVO fileInfo;

  public FileServerFile(FileInfoVO fileInfo) {
    this.fileInfo = fileInfo;
  }

  @Override
  @Nullable
  public String getFilename() {
    return fileInfo.getFileName();
  }

  @Override
  @Nullable
  public String getFileType() {
    String fileType = fileInfo.getFileType();
    if (StringUtils.isEmpty(fileType) && StringUtils.isNotEmpty(fileInfo.getFileName())) {
      fileType = FilenameUtils.getExtension(fileInfo.getFileName());
    }
    return StringUtils.lowerCase(fileType);
  }

  @Override
  @Nullable
  public String getMimeType() {
    String fileType = getFileType();
    if (fileType == null) {
      return null;
    }
    return TikaUtil.detect("1." + fileType);
  }
}
