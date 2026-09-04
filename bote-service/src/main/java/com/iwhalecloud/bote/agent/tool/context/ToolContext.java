package com.iwhalecloud.bote.agent.tool.context;

import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.sandbox.api.SandboxClient;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.websocket.context.WebSocketChatContext;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import org.springframework.lang.Nullable;

/**
 * 工具调用上下文
 *
 * @param sessionId 会话 ID
 * @param userId 用户 ID
 * @param botId 应用 ID
 * @param tenantId 租户 ID
 * @param spaceId 用户所在空间 ID
 * @param sceneId 场景 ID
 * @param chatContext 会话上下文
 * @param webSocketChatContext WebSocket 会话上下文
 * @param sandboxMode 沙箱模式
 * @param sandboxClient 沙箱客户端
 * @param envVariables 环境变量，执行 shell 命令时使用
 * @param skills 技能列表
 * @param subagents 子智能体列表
 * @param toolCallbacks 当前智能体可用工具列表
 * @param loadSkillListener 加载技能监听器
 * @param channel 当前渠道
 * @param clientOs 客户端操作系统（仅用于桌面客户端）
 * @param workDir 工作目录
 * @param modelClient 当前场景使用的大模型客户端（部分工具需要）
 * @author bianjp
 * @since 2026-03-09
 */
@Builder(toBuilder = true)
public record ToolContext(Long sessionId,
                          Long userId,
                          Long botId,
                          Long tenantId,
                          Long spaceId,
                          Long sceneId,
                          ChatContext chatContext,
                          WebSocketChatContext webSocketChatContext,
                          SandboxMode sandboxMode,
                          SandboxClient sandboxClient,
                          @Nullable
                          Map<String, String> envVariables,
                          List<AgentSkillSpec> skills,
                          @Nullable
                          List<SimpleBotSceneDTO> subagents,
                          @Nullable
                          List<ToolCallback> toolCallbacks,
                          @Nullable
                          Consumer<AgentSkillSpec> loadSkillListener,
                          @Nullable
                          String channel,
                          @Nullable
                          String clientOs,
                          String workDir,
                          @Nullable
                          LlmClient modelClient) {

}
