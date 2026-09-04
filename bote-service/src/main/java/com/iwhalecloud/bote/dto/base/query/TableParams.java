package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 代码生成参数
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@Schema(description = "代码生成参数")
public class TableParams {
  @Schema(description = "生成代码包路径，默认为当前项目包路径")
  private String packageDir;
  @Schema(description = "生成代码包子路径")
  private String subDir;
  @Schema(description = "表编码")
  private String tableCode;
  @Schema(description = "实体描述，作为备注")
  private String entityDesc;
  @Schema(description = "实体编码，用于构造模型对应的文件，例如：${entityCode}+Entity ${entityCode}+DTO ${entityCode}+QueryParams。 首字母大写驼峰格式，示例：AttrSpec")
  private String entityCode;
}
