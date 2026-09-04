package com.iwhalecloud.bote.common.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 关闭时删除对应临时文件，用于大 zip 导出避免进程内长期持有路径
 */
public class DeleteOnCloseFileInputStream extends FileInputStream {

  private final Path path;

  public DeleteOnCloseFileInputStream(Path path) throws IOException {
    super(path.toFile());
    this.path = path;
  }

  @Override
  public void close() throws IOException {
    try {
      super.close();
    }
    finally {
      try {
        Files.deleteIfExists(path);
      }
      catch (IOException e) {
        // 尽力删除，不掩盖原始 close 异常
      }
    }
  }
}
