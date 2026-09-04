package com.iwhalecloud.bote.dto.skill;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 技能广场全量导出流结果（zip 流 + 文件名）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SkillSquareExportStreamDTO {

  private InputStream inputStream;
  private String fileName;

  public static SkillSquareExportStreamDTO of(InputStream inputStream, String fileName) {
    return new SkillSquareExportStreamDTO(inputStream, fileName);
  }
}
