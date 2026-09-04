package com.iwhalecloud.bote.dto.base;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 代码生成定义
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@Builder
public class CodeGenerateDefinition {
  /** 包路径 */
  private String packageDir;
  /** 包子路径 */
  private String subDir;
  /** 作者 */
  private String author;
  /** 创建时间 */
  private String currentTime;
  /** 表定义 */
  private List<TableDefinition> tables;
  /** 压缩工作目录 */
  private String compressDir;
  /** 解压工作目录 */
  private String decompressDir;
  /** 自动生成的文件列表，left: 文件路径，right: 文件名称 */
  private List<Pair<String, String>> files;
}
