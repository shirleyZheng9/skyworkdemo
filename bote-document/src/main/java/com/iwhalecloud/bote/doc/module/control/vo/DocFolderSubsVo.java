package com.iwhalecloud.bote.doc.module.control.vo;

import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "文件夹子元素")
public class DocFolderSubsVo {
  @Schema(description = "文件夹子元素一级")
  private PageInfo<NodeInfoTreeVo> nodes;

  @Schema(description = "文件数")
  private Long docCount;

  @Schema(description = "文件夹数")
  private Long folderCount;

}
