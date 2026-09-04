package com.iwhalecloud.bote.dto.knowledge.access.platform;

import java.util.List;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 资源树节点
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
@Schema(description = "资源树节点")
public class ResourceTreeDTO {
    @Schema(description = "资源ID")
    private String wid;

    @Schema(description = "资源名称")
    private String coursewareName;

    @Schema(description = "资源地址")
    private String coursewareUrl;

    @Schema(description = "是否文件夹：0-非文件夹；1-文件夹")
    private String isFolder;

    @Schema(description = "资源封面图标路径")
    private String icon;

    @Schema(description = "父节点ID")
    private String parentResourceWid;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "文档资源才有值:文件后缀")
    private String fileSuffix;

    @Schema(description = "文档资源才有值:文件字节大小")
    private String size;

    @Schema(description = "子节点列表")
    private List<ResourceTreeDTO> children;
}
