package com.iwhalecloud.bote.loop.data.domain.dataset.service.handler;

import com.iwhalecloud.bote.loop.data.domain.component.IFileReader;
import com.iwhalecloud.bote.loop.data.domain.component.IUnionFS;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOFile;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导入工作空间
 * 迁移对应关系: Go语言importWorkspace
 * - 功能: 管理文件读取和进度恢复
 * - 字段定义: 各种工作空间字段
 * <p>
 * Java实现说明:
 * - 对应Go的importWorkspace结构体
 * - 使用Lombok注解简化代码
 * - 提供工作空间管理功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java List
 * - Go映射 -> Java Map
 * - Go指针 -> Java包装类型
 */
@Getter
@Setter
@SuppressWarnings("PMD.GuardLogStatement")
public class ImportWorkspace {
  private static final Logger logger = LoggerFactory.getLogger(ImportWorkspace.class);

  private DatasetIOFile source;
  private Map<String, DatasetIOJobProgress> progress = new HashMap<>();
  private IUnionFS fs;
  private String dir;
  private List<String> files;
  private int cursor = 0;

  /**
   * 获取下一个文件
   * 迁移对应关系: Go语言nextFile
   * - 功能: 获取下一个要处理的文件
   * - 返回: 文件读取器
   * - 用途: 文件遍历
   */
  public IFileReader nextFile() {
    if (cursor >= files.size()) {
      return null;
    }

    String name = files.get(cursor);
    DatasetIOJobProgress proc = progress.get(name);
    if (proc != null && proc.getTotal() > 0 && proc.getTotal() <= proc.getProcessed()) {
      // 已处理完成
      cursor++;
      return nextFile();
    }

    String filename = Paths.get(dir, name).toString();
    try {
      IFileReader reader = fs.getROFileSystem().readFile(filename);
      if (reader == null) {
        throw new BssException("failed to read file: " + filename);
      }

      cursor++;
      if (proc != null && proc.getProcessed() > 0) {
        logger.info("resume reading cursor from line {}, file={}", proc.getProcessed(), name);
        reader.seekToOffset(proc.getProcessed());
      }

      return reader;
    }
    catch (IOException e) {
      throw new BssException("failed to read file: " + filename, e);
    }
  }

  /**
   * 检查是否没有更多文件
   * 迁移对应关系: Go语言noMoreFile
   * - 功能: 检查是否没有更多文件
   * - 返回: 是否没有更多文件
   * - 用途: 文件遍历控制
   */
  public boolean noMoreFile() {
    return cursor >= files.size();
  }
}
