package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.EChartsGeneratorParams;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECharts图表生成插件执行器
 *
 * @author zyt
 * @since 2025-06-30
 */
@Component
public class EChartsGeneratorPlugin extends AbstractPlugin<EChartsGeneratorParams> {

    private final ModelClientCache modelClientCache;
    private final TenantSettingInfoCache tenantSettingInfoCache;

    public EChartsGeneratorPlugin(ModelClientCache modelClientCache, TenantSettingInfoCache tenantSettingInfoCache) {
        super(EChartsGeneratorParams.class);
        this.modelClientCache = modelClientCache;
        this.tenantSettingInfoCache = tenantSettingInfoCache;
    }

    @Override
    public String getPluginCode() {
        return PluginConsts.ECHARTS_GENERATOR;
    }

    @Override
    public ParameterSpec createRequestParameter() {
        List<ParameterSpec> children = new ArrayList<>();
        children.add(ParameterSpec.newProperty("context", "上下文信息（包含用户问题描述）", AttrDataType.STRING));
        children.add(ParameterSpec.newProperty("data", "数据", AttrDataType.ARRAY));
        children.add(ParameterSpec.newProperty("options", "格式要求对象", AttrDataType.OBJECT));
        return ParameterSpec.newRoot(children);
    }

    @Override
    public ParameterSpec createResponseParameter() {
        // 返回ECharts options对象
        List<ParameterSpec> children = new ArrayList<>();
        children.add(ParameterSpec.newProperty("options", "ECharts配置对象", AttrDataType.OBJECT));
        return ParameterSpec.newRoot(children);
    }

    @Override
    public void validateParams(EChartsGeneratorParams params) {
        Assert.hasText(params.getContext(), "上下文信息不能为空");
        Assert.notNull(params.getData(), "数据对象不能为空");
    }

    @Override
    public Object doRun(EChartsGeneratorParams params) {
        // 获取租户ID和默认大模型
        Long tenantId = getCurrentContext().getTenantId();
        Long modelId = tenantSettingInfoCache.getModelId(tenantId);
        LlmClient llmClient = modelClientCache.getLlmClient(tenantId, modelId);

        // 构建提示词
        String prompt = buildPrompt(params);

        // 调用大模型
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .addSystemMessage(prompt)
                .addUserMessage("请根据以上要求生成ECharts配置")
                .build();

        ChatCompletionResponse response = llmClient.chatCompletion(request);
        String content = response.getMessageContent();

        // 解析返回的JSON
        try {
            // 清理可能的markdown代码块标记
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            Map<String, Object> result = JsonUtil.parseJson(content, new TypeReference<Map<String, Object>>() { });

            // 返回结果
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("options", result);
            return responseMap;
        } catch (Exception e) {
            throw new BssException("解析大模型返回的ECharts配置失败: " + content, e);
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(EChartsGeneratorParams params) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个ECharts专家，根据用户数据和问题描述生成ECharts 5的option配置。遵守以下规则：\n\n");

        prompt.append("1. 输入数据 (JSON格式)：\n");
        prompt.append(JsonUtil.toJsonString(params.getData()));
        prompt.append("\n\n");

        prompt.append("2. 用户问题 (包含图表要求)：\n");
        prompt.append(params.getContext());
        prompt.append("\n\n");

        prompt.append("3.用户的格式要求：\n");
        if (params.getOptions() != null) {
            prompt.append(JsonUtil.toJsonString(params.getOptions()));
        } else {
            prompt.append("{}");
        }
        prompt.append("\n\n");

        prompt.append("4. 你必须：\n");
        prompt.append("✅ 解析用户问题中的图表属性（类型/样式/特殊要求），一切先以用户要求为准，如果用户没有要求则自动匹配最佳图表，并且以用户格式要求为主\n");
        prompt.append("✅ 生成可直接运行的完整JSON配置（仅输出JSON！）\n");
        prompt.append("✅ 用户的格式要求options必须要全部满足！\n");
        prompt.append("✅ 包含以下核心组件：\n");
        prompt.append("   - title（根据问题自动生成标题）\n");
        prompt.append("   - legend（多系列时自动启用）\n");
        prompt.append("   - grid\n");
        prompt.append("   - xAxis/yAxis（自动识别数据类型）\n");
        prompt.append("   - series（严格匹配问题要求的图表类型）\n");
        prompt.append("   - 其他图表所需要的属性（智能处理）\n\n");

        prompt.append("5. 智能处理：\n");
        prompt.append("▸ 自动转换数据格式（时间→日期类型，百分比→formatter）\n");
        prompt.append("▸ 防标签重叠：axisLabel.rotate=30\n");
        prompt.append("▸ 多系列数据自动拆分\n");
        prompt.append("▸ 问题关键词映射：\n");
        prompt.append("   • \"趋势\"→折线图 | \"占比\"→饼图 | \"分布\"→散点图\n");
        prompt.append("   • \"比较\"→柱状图 | \"关系\"→关系图 | \"进度\"→仪表盘\n");
        prompt.append("▸ 尽可能的现实最完整的信息\n\n");

        prompt.append("只输出JSON对象，不要任何其他内容！并且这个输出的json脚本必须要能直接被ECharts识别解析生成图表。");

        return prompt.toString();
    }
}
