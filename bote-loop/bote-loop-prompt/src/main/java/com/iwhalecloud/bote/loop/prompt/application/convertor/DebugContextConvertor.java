package com.iwhalecloud.bote.loop.prompt.application.convertor;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CompareConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CompareGroupDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugContextDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugCoreDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugMessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugToolCallDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MockToolDTO;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CompareConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CompareGroup;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugContext;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugCore;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugMessage;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugToolCall;
import com.iwhalecloud.bote.loop.prompt.domain.entity.MockTool;
import java.util.ArrayList;
import java.util.List;

/**
 * 调试上下文转换器
 * 迁移对应关系: Go语言modules/prompt/application/convertor.DebugContextConvertor
 * - 功能: 将调试上下文领域对象(DO)与数据传输对象(DTO)之间进行转换
 * - 主要方法:
 * * debugContextDTO2DO - DebugContext DTO转DO
 * * debugContextDO2DTO - DebugContext DO转DTO
 * * debugCoreDTO2DO - DebugCore DTO转DO
 * * debugCoreDO2DTO - DebugCore DO转DTO
 * * debugMessageDTO2DO - DebugMessage DTO转DO
 * * debugMessageDO2DTO - DebugMessage DO转DTO
 * * debugToolCallDTO2DO - DebugToolCall DTO转DO
 * * debugToolCallDO2DTO - DebugToolCall DO转DTO
 * * mockToolDTO2DO - MockTool DTO转DO
 * * mockToolDO2DTO - MockTool DO转DTO
 * * debugConfigDTO2DO - DebugConfig DTO转DO
 * * debugConfigDO2DTO - DebugConfig DO转DTO
 * * compareConfigDTO2DO - CompareConfig DTO转DO
 * * compareConfigDO2DTO - CompareConfig DO转DTO
 * * compareGroupDTO2DO - CompareGroup DTO转DO
 * * compareGroupDO2DTO - CompareGroup DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * - 处理嵌套对象转换
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go ptr.Of -> Java直接赋值
 */
public final class DebugContextConvertor {

  private DebugContextConvertor() {
    // 工具类，禁止实例化
  }

  // ==================== DTO转DO方法 ====================

  /**
   * DebugContext DTO转DO
   * 迁移对应关系: Go语言convertor.DebugContextDTO2DO
   * - 功能: 将DebugContext DTO转换为领域对象
   * - 参数: PromptID、用户ID、DebugContext DTO
   * - 返回: DebugContext领域对象
   */
  public static DebugContext debugContextDTO2DO(Long promptId, String userId, DebugContextDTO dto) {
    if (dto == null) {
      return DebugContext.builder()
        .promptId(promptId)
        .userId(userId)
        .build();
    }

    return DebugContext.builder()
      .promptId(promptId)
      .userId(userId)
      .debugCore(debugCoreDTO2DO(dto.getDebugCore()))
      .debugConfig(debugConfigDTO2DO(dto.getDebugConfig()))
      .compareConfig(compareConfigDTO2DO(dto.getCompareConfig()))
      .build();
  }

  /**
   * DebugCore DTO转DO
   * 迁移对应关系: Go语言convertor.DebugCoreDTO2DO
   * - 功能: 将DebugCore DTO转换为领域对象
   * - 参数: DebugCore DTO
   * - 返回: DebugCore领域对象
   */
  public static DebugCore debugCoreDTO2DO(DebugCoreDTO dto) {
    if (dto == null) {
      return null;
    }

    return DebugCore.builder()
      .mockContexts(debugMessagesDTO2DO(dto.getMockContexts()))
      .mockVariables(ManageConvertor.batchVariableValDTO2DO(dto.getMockVariables()))
      .mockTools(mockToolsDTO2DO(dto.getMockTools()))
      .build();
  }

  /**
   * 批量DebugMessage DTO转DO
   * 迁移对应关系: Go语言convertor.DebugMessagesDTO2DO
   * - 功能: 批量将DebugMessage DTO转换为领域对象
   * - 参数: DebugMessage DTO列表
   * - 返回: DebugMessage领域对象列表
   */
  public static List<DebugMessage> debugMessagesDTO2DO(List<DebugMessageDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<DebugMessage> debugMessages = new ArrayList<>();
    for (DebugMessageDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      debugMessages.add(debugMessageDTO2DO(dto));
    }
    return debugMessages;
  }

  /**
   * DebugMessage DTO转DO
   * 迁移对应关系: Go语言convertor.DebugMessageDTO2DO
   * - 功能: 将DebugMessage DTO转换为领域对象
   * - 参数: DebugMessage DTO
   * - 返回: DebugMessage领域对象
   */
  public static DebugMessage debugMessageDTO2DO(DebugMessageDTO dto) {
    if (dto == null) {
      return null;
    }

    return DebugMessage.builder()
      .role(ManageConvertor.roleDTO2DO(dto.getRole()))
      .reasoningContent(dto.getReasoningContent())
      .content(dto.getContent())
      .parts(ManageConvertor.batchContentPartDTO2DO(dto.getParts()))
      .toolCallId(dto.getToolCallId())
      .toolCalls(debugToolCallsDTO2DO(dto.getToolCalls()))
      .debugId(dto.getDebugId())
      .inputTokens(dto.getInputTokens())
      .outputTokens(dto.getOutputTokens())
      .costMS(dto.getCostMs())
      .build();
  }

  /**
   * 批量DebugToolCall DTO转DO
   * 迁移对应关系: Go语言convertor.DebugToolCallsDTO2DO
   * - 功能: 批量将DebugToolCall DTO转换为领域对象
   * - 参数: DebugToolCall DTO列表
   * - 返回: DebugToolCall领域对象列表
   */
  public static List<DebugToolCall> debugToolCallsDTO2DO(List<DebugToolCallDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<DebugToolCall> debugToolCalls = new ArrayList<>();
    for (DebugToolCallDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      debugToolCalls.add(debugToolCallDTO2DO(dto));
    }
    return debugToolCalls;
  }

  /**
   * DebugToolCall DTO转DO
   * 迁移对应关系: Go语言convertor.DebugToolCallDTO2DO
   * - 功能: 将DebugToolCall DTO转换为领域对象
   * - 参数: DebugToolCall DTO
   * - 返回: DebugToolCall领域对象
   */
  public static DebugToolCall debugToolCallDTO2DO(DebugToolCallDTO dto) {
    if (dto == null) {
      return null;
    }

    return DebugToolCall.builder()
      .toolCall(ManageConvertor.toolCallDTO2DO(dto.getToolCall()))
      .mockResponse(dto.getMockResponse())
      .debugTraceKey(dto.getDebugTraceKey())
      .build();
  }

  /**
   * 批量MockTool DTO转DO
   * 迁移对应关系: Go语言convertor.MockToolsDTO2DO
   * - 功能: 批量将MockTool DTO转换为领域对象
   * - 参数: MockTool DTO列表
   * - 返回: MockTool领域对象列表
   */
  public static List<MockTool> mockToolsDTO2DO(List<MockToolDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<MockTool> mockTools = new ArrayList<>();
    for (MockToolDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      mockTools.add(mockToolDTO2DO(dto));
    }
    return mockTools;
  }

  /**
   * MockTool DTO转DO
   * 迁移对应关系: Go语言convertor.MockToolDTO2DO
   * - 功能: 将MockTool DTO转换为领域对象
   * - 参数: MockTool DTO
   * - 返回: MockTool领域对象
   */
  public static MockTool mockToolDTO2DO(MockToolDTO dto) {
    if (dto == null) {
      return null;
    }

    return MockTool.builder()
      .name(dto.getName())
      .mockResponse(dto.getMockResponse())
      .build();
  }

  /**
   * DebugConfig DTO转DO
   * 迁移对应关系: Go语言convertor.DebugConfigDTO2DO
   * - 功能: 将DebugConfig DTO转换为领域对象
   * - 参数: DebugConfig DTO
   * - 返回: DebugConfig领域对象
   */
  public static DebugConfig debugConfigDTO2DO(DebugConfigDTO dto) {
    if (dto == null) {
      return null;
    }

    return DebugConfig.builder()
      .singleStepDebug(dto.getSingleStepDebug())
      .build();
  }

  /**
   * CompareConfig DTO转DO
   * 迁移对应关系: Go语言convertor.CompareConfigDTO2DO
   * - 功能: 将CompareConfig DTO转换为领域对象
   * - 参数: CompareConfig DTO
   * - 返回: CompareConfig领域对象
   */
  public static CompareConfig compareConfigDTO2DO(CompareConfigDTO dto) {
    if (dto == null) {
      return null;
    }

    return CompareConfig.builder()
      .groups(batchCompareGroupDTO2DO(dto.getGroups()))
      .build();
  }

  /**
   * 批量CompareGroup DTO转DO
   * 迁移对应关系: Go语言convertor.BatchCompareGroupDTO2DO
   * - 功能: 批量将CompareGroup DTO转换为领域对象
   * - 参数: CompareGroup DTO列表
   * - 返回: CompareGroup领域对象列表
   */
  public static List<CompareGroup> batchCompareGroupDTO2DO(List<CompareGroupDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<CompareGroup> compareGroups = new ArrayList<>();
    for (CompareGroupDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      compareGroups.add(compareGroupDTO2DO(dto));
    }
    return compareGroups;
  }

  /**
   * CompareGroup DTO转DO
   * 迁移对应关系: Go语言convertor.CompareGroupDTO2DO
   * - 功能: 将CompareGroup DTO转换为领域对象
   * - 参数: CompareGroup DTO
   * - 返回: CompareGroup领域对象
   */
  public static CompareGroup compareGroupDTO2DO(CompareGroupDTO dto) {
    if (dto == null) {
      return null;
    }

    return CompareGroup.builder()
      .promptDetail(ManageConvertor.promptDetailDTO2DO(dto.getPromptDetail()))
      .debugCore(debugCoreDTO2DO(dto.getDebugCore()))
      .build();
  }

  // ==================== DO转DTO方法 ====================

  /**
   * DebugContext DO转DTO
   * 迁移对应关系: Go语言convertor.DebugContextDO2DTO
   * - 功能: 将DebugContext领域对象转换为DTO
   * - 参数: DebugContext领域对象
   * - 返回: DebugContext DTO
   */
  public static DebugContextDTO debugContextDO2DTO(DebugContext debugContext) {
    if (debugContext == null) {
      return null;
    }

    return DebugContextDTO.builder()
      .debugCore(debugCoreDO2DTO(debugContext.getDebugCore()))
      .debugConfig(debugConfigDO2DTO(debugContext.getDebugConfig()))
      .compareConfig(compareConfigDO2DTO(debugContext.getCompareConfig()))
      .build();
  }

  /**
   * DebugCore DO转DTO
   * 迁移对应关系: Go语言convertor.DebugCoreDO2DTO
   * - 功能: 将DebugCore领域对象转换为DTO
   * - 参数: DebugCore领域对象
   * - 返回: DebugCore DTO
   */
  public static DebugCoreDTO debugCoreDO2DTO(DebugCore debugCore) {
    if (debugCore == null) {
      return null;
    }

    return DebugCoreDTO.builder()
      .mockContexts(debugMessagesDO2DTO(debugCore.getMockContexts()))
      .mockVariables(ManageConvertor.batchVariableValDO2DTO(debugCore.getMockVariables()))
      .mockTools(mockToolsDO2DTO(debugCore.getMockTools()))
      .build();
  }

  /**
   * 批量DebugMessage DO转DTO
   * 迁移对应关系: Go语言convertor.DebugMessagesDO2DTO
   * - 功能: 批量将DebugMessage领域对象转换为DTO
   * - 参数: DebugMessage领域对象列表
   * - 返回: DebugMessage DTO列表
   */
  public static List<DebugMessageDTO> debugMessagesDO2DTO(List<DebugMessage> debugMessages) {
    if (debugMessages == null || debugMessages.isEmpty()) {
      return null;
    }

    List<DebugMessageDTO> debugMessageDTOs = new ArrayList<>();
    for (DebugMessage debugMessage : debugMessages) {
      if (debugMessage == null) {
        continue;
      }
      debugMessageDTOs.add(debugMessageDO2DTO(debugMessage));
    }
    return debugMessageDTOs;
  }

  /**
   * DebugMessage DO转DTO
   * 迁移对应关系: Go语言convertor.DebugMessageDO2DTO
   * - 功能: 将DebugMessage领域对象转换为DTO
   * - 参数: DebugMessage领域对象
   * - 返回: DebugMessage DTO
   */
  public static DebugMessageDTO debugMessageDO2DTO(DebugMessage debugMessage) {
    if (debugMessage == null) {
      return null;
    }

    return DebugMessageDTO.builder()
      .role(ManageConvertor.roleDO2DTO(debugMessage.getRole()))
      .reasoningContent(debugMessage.getReasoningContent())
      .content(debugMessage.getContent())
      .parts(ManageConvertor.batchContentPartDO2DTO(debugMessage.getParts()))
      .toolCallId(debugMessage.getToolCallId())
      .toolCalls(ManageConvertor.batchDebugToolCallDO2DTO(debugMessage.getToolCalls()))
      .debugId(debugMessage.getDebugId())
      .inputTokens(debugMessage.getInputTokens())
      .outputTokens(debugMessage.getOutputTokens())
      .costMs(debugMessage.getCostMS())
      .build();
  }

  /**
   * 批量MockTool DO转DTO
   * 迁移对应关系: Go语言convertor.MockToolsDO2DTO
   * - 功能: 批量将MockTool领域对象转换为DTO
   * - 参数: MockTool领域对象列表
   * - 返回: MockTool DTO列表
   */
  public static List<MockToolDTO> mockToolsDO2DTO(List<MockTool> mockTools) {
    if (mockTools == null || mockTools.isEmpty()) {
      return null;
    }

    List<MockToolDTO> mockToolDTOs = new ArrayList<>();
    for (MockTool mockTool : mockTools) {
      if (mockTool == null) {
        continue;
      }
      mockToolDTOs.add(mockToolDO2DTO(mockTool));
    }
    return mockToolDTOs;
  }

  /**
   * MockTool DO转DTO
   * 迁移对应关系: Go语言convertor.MockToolDO2DTO
   * - 功能: 将MockTool领域对象转换为DTO
   * - 参数: MockTool领域对象
   * - 返回: MockTool DTO
   */
  public static MockToolDTO mockToolDO2DTO(MockTool mockTool) {
    if (mockTool == null) {
      return null;
    }

    return MockToolDTO.builder()
      .name(mockTool.getName())
      .mockResponse(mockTool.getMockResponse())
      .build();
  }

  /**
   * DebugConfig DO转DTO
   * 迁移对应关系: Go语言convertor.DebugConfigDO2DTO
   * - 功能: 将DebugConfig领域对象转换为DTO
   * - 参数: DebugConfig领域对象
   * - 返回: DebugConfig DTO
   */
  public static DebugConfigDTO debugConfigDO2DTO(DebugConfig debugConfig) {
    if (debugConfig == null) {
      return null;
    }

    return DebugConfigDTO.builder()
      .singleStepDebug(debugConfig.getSingleStepDebug())
      .build();
  }

  /**
   * CompareConfig DO转DTO
   * 迁移对应关系: Go语言convertor.CompareConfigDO2DTO
   * - 功能: 将CompareConfig领域对象转换为DTO
   * - 参数: CompareConfig领域对象
   * - 返回: CompareConfig DTO
   */
  public static CompareConfigDTO compareConfigDO2DTO(CompareConfig compareConfig) {
    if (compareConfig == null) {
      return null;
    }

    return CompareConfigDTO.builder()
      .groups(batchCompareGroupDO2DTO(compareConfig.getGroups()))
      .build();
  }

  /**
   * 批量CompareGroup DO转DTO
   * 迁移对应关系: Go语言convertor.BatchCompareGroupDO2DTO
   * - 功能: 批量将CompareGroup领域对象转换为DTO
   * - 参数: CompareGroup领域对象列表
   * - 返回: CompareGroup DTO列表
   */
  public static List<CompareGroupDTO> batchCompareGroupDO2DTO(List<CompareGroup> compareGroups) {
    if (compareGroups == null || compareGroups.isEmpty()) {
      return null;
    }

    List<CompareGroupDTO> compareGroupDTOs = new ArrayList<>();
    for (CompareGroup compareGroup : compareGroups) {
      if (compareGroup == null) {
        continue;
      }
      compareGroupDTOs.add(compareGroupDO2DTO(compareGroup));
    }
    return compareGroupDTOs;
  }

  /**
   * CompareGroup DO转DTO
   * 迁移对应关系: Go语言convertor.CompareGroupDO2DTO
   * - 功能: 将CompareGroup领域对象转换为DTO
   * - 参数: CompareGroup领域对象
   * - 返回: CompareGroup DTO
   */
  public static CompareGroupDTO compareGroupDO2DTO(CompareGroup compareGroup) {
    if (compareGroup == null) {
      return null;
    }

    return CompareGroupDTO.builder()
      .promptDetail(ManageConvertor.promptDetailDO2DTO(compareGroup.getPromptDetail()))
      .debugCore(debugCoreDO2DTO(compareGroup.getDebugCore()))
      .build();
  }
}
