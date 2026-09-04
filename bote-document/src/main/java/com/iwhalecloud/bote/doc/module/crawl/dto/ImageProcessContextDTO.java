package com.iwhalecloud.bote.doc.module.crawl.dto;

import java.util.List;
import java.util.Map;

import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片处理上下文 DTO
 *
 * <p>从 {@code ImageProcessContext} 调整而来。</p>
 */
@Getter
@Setter
@ToString
public class ImageProcessContextDTO {

  @Schema(description = "原始图片 URL 列表")
  private List<String> imageUrls;

  @Schema(description = "图片 URL 替换映射（原始URL -> 新URL）")
  private Map<String, String> urlReplacementMap;

  @Schema(description = "已上传的文件信息列表")
  private List<FileInfoVO> fileInfos;

  @Schema(description = "成功数量")
  private int success;

  @Schema(description = "失败数量")
  private int failed;

  @Schema(description = "跳过数量")
  private int skipped;
}

