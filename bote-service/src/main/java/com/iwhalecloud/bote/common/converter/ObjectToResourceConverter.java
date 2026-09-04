package com.iwhalecloud.bote.common.converter;

import com.iwhalecloud.bote.common.io.FileBackedOutputStream;
import com.iwhalecloud.bote.common.io.FileInfoResource;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象转为资源
 *
 * @author qian.sisheng
 * @since 2025-07-08
 */
@Component("boteObjectToResourceConverter")
@RequiredArgsConstructor
public class ObjectToResourceConverter implements Converter<Object, Resource> {
  private final IFileStoreService fileStoreService;

  @Override
  public Resource convert(Object source) {
    // 支持文件 ID
    Long fileId = parseFileId(source);
    if (fileId != null) {
      return getResourceByFileId(fileId);
    }

    Resource result = null;
    if (source instanceof MultipartFile) {
      result = ((MultipartFile) source).getResource();
    }
    else if (source instanceof File) {
      result = new FileSystemResource((File) source);
    }
    else if (source instanceof byte[]) {
      result = new ByteArrayResource((byte[]) source);
    }
    else if (source instanceof InputStream) {
      result = convertInputStream((InputStream) source);
    }
    else if (source instanceof OutputStream) {
      result = convertOutputStream((OutputStream) source);
    }
    if (result == null) {
      throw new BssException("不支持 " + source.getClass().getCanonicalName() + " 转为文件类型");
    }
    return result;
  }

  /**
   * 检查是否是文件 ID
   */
  @Nullable
  private Long parseFileId(Object source) {
    if (source instanceof Long) {
      return (Long) source;
    }
    else if (source instanceof Integer) {
      return ((Integer) source).longValue();
    }
    else if (source instanceof String && StringUtils.isNumeric((String) source) && ((String) source).length() < 20) {
      return Long.parseLong((String) source);
    }
    return null;
  }

  /**
   * 根据文件ID转换成资源
   */
  private Resource getResourceByFileId(Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    if (fileInfo == null) {
      throw new BssException("文件不存在, fileId=" + fileId);
    }
    return new FileInfoResource(fileInfo);
  }

  /**
   * InputStream 转为资源
   */
  private Resource convertInputStream(InputStream source) {
    return new InputStreamResource(source) {
      @Override
      public String getFilename() {
        return "默认名称";
      }
    };
  }

  /**
   * OutputStream 转为资源
   *
   * <p>只有支持读取的部分 OutputStream 实现可以转为资源</p>
   */
  @Nullable
  @SuppressWarnings("UnstableApiUsage")
  private Resource convertOutputStream(OutputStream source) {
    Resource resource = null;
    try {
      if (source instanceof FileBackedOutputStream) {
        resource = new InputStreamResource(((FileBackedOutputStream) source).inputStream());
      }
      else if (source instanceof com.google.common.io.FileBackedOutputStream) {
        resource = new InputStreamResource(((com.google.common.io.FileBackedOutputStream) source).asByteSource().openBufferedStream());
      }
      else if (source instanceof ByteArrayOutputStream) {
        resource = new ByteArrayResource(((ByteArrayOutputStream) source).toByteArray());
      }
    }
    catch (IOException e) {
      throw new BssException("输出流 " + source.getClass().getCanonicalName() + " 转为文件失败: " + ExpUtil.getMsg(e), e);
    }
    return resource;
  }
}
