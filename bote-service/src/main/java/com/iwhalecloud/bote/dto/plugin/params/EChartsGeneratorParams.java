package com.iwhalecloud.bote.dto.plugin.params;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 生成echarts图表插件参数
 *
 * @author zyt
 * @since 2025-06-30
 */
@Getter
@Setter
@ToString
public class EChartsGeneratorParams {
    /** 上下文信息，包含用户问题描述 */
    private String context;

    /** 数据 */
    private List<Map<String, Object>> data;

    /** 预置格式要求对象 */
    private Map<String, Object> options;
}
