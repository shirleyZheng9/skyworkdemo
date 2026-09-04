package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 插件执行上下文
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@Getter
@Setter
public class PluginExecutionContext {
    /** 租户ID */
    private final Long tenantId;
    /** 模型ID */
    private final Long modelId;
    /** 插件ID */
    private final Long pluginId;
    /** 是否依赖模型 */
    private final String needModel;
    /** 插件参数 */
    private final Map<String, Object> pluginParams;

    public PluginExecutionContext(PluginExecuteParams params) {
        this.tenantId = params.getTenantId();
        this.modelId = params.getModelId();
        this.pluginId = params.getPluginId();
        this.pluginParams = params.getParams();
        this.needModel = params.getNeedModel();
    }

    /**
     * 检查是否支持LLM功能
     */
    public boolean supportsLlm() {
        return BaseConsts.TRUE.equals(needModel);
    }
}
