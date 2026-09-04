package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识中台文档内容参数
 *
 * @author fan.cong
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class KnowledgeFileReadParams extends AbstractPluginParams {
    /**
     * RESOURCE:个人资源、KNOW_BASE_RESOURCE:知识库资源
     */
    private String resourceType;

    /**
     * 资源wid，传知识库ID或文档ID
     */
    private String resourceWid;

    public KnowledgeFileReadParams() {
        super(PluginConsts.PLUGIN_CODE_KNOWLEDGE_FILE_READ);
    }
}
