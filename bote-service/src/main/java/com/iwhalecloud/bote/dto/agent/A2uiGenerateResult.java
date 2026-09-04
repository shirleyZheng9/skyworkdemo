package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.agent.tools.A2uiTools.A2uiEvent;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * A2UI 生成结果
 *
 * @param success 是否成功
 * @param error 错误信息
 * @param events 事件列表
 * @author bianjp
 * @since 2026-04-15
 */
public record A2uiGenerateResult(boolean success, @Nullable String error, @Nullable List<A2uiEvent> events) {
}
