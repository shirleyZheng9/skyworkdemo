package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugContextEntity;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CompareConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugContext;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugCore;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugMessage;
import com.iwhalecloud.bote.loop.prompt.domain.entity.MockTool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调试上下文转换器
 * 迁移对应关系: Go语言modules/prompt/infra/repo/mysql/convertor.DebugContextConvertor
 * - 功能: 将调试上下文领域对象(DO)与持久化对象(PO)之间进行转换
 * - 主要方法:
 * * debugContextDO2PO - DebugContext DO转PO
 * * debugContextPO2DO - DebugContext PO转DO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 使用Jackson进行JSON序列化/反序列化
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go json.MarshalString -> Jackson ObjectMapper
 * - Go json.Unmarshal -> Jackson ObjectMapper
 * - Go ptr.Of -> Java直接赋值
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class DebugContextConvertor {

  private DebugContextConvertor() {
    // 工具类，禁止实例化
  }

  private static final Logger logger = LoggerFactory.getLogger(DebugContextConvertor.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 转换DebugContext DO到PO
   * 迁移对应关系: Go语言convertor.DebugContextDO2PO
   * - 功能: 将调试上下文DO转换为PO
   * - 参数: 调试上下文DO
   * - 返回: 调试上下文PO
   */
  public static PromptDebugContextEntity debugContextDO2PO(DebugContext debugContext) {
    if (debugContext == null) {
      return null;
    }

    String mockContexts = serializeDebugCore(debugContext);
    String mockVariables = serializeDebugCoreVariables(debugContext);
    String mockTools = serializeDebugCoreTools(debugContext);
    String debugConfig = serializeDebugConfig(debugContext);
    String compareConfig = serializeCompareConfig(debugContext);

    return buildPromptDebugContextEntity(debugContext, mockContexts, mockVariables, mockTools, debugConfig, compareConfig);
  }

  private static String serializeDebugCore(DebugContext debugContext) {
    if (debugContext.getDebugCore() != null && debugContext.getDebugCore().getMockContexts() != null) {
      return serializeToJson(debugContext.getDebugCore().getMockContexts(), "debug core mock contexts");
    }
    return null;
  }

  private static String serializeDebugCoreVariables(DebugContext debugContext) {
    if (debugContext.getDebugCore() != null && debugContext.getDebugCore().getMockVariables() != null) {
      return serializeToJson(debugContext.getDebugCore().getMockVariables(), "debug core mock variables");
    }
    return null;
  }

  private static String serializeDebugCoreTools(DebugContext debugContext) {
    if (debugContext.getDebugCore() != null && debugContext.getDebugCore().getMockTools() != null) {
      return serializeToJson(debugContext.getDebugCore().getMockTools(), "debug core mock tools");
    }
    return null;
  }

  private static String serializeDebugConfig(DebugContext debugContext) {
    if (debugContext.getDebugConfig() != null) {
      return serializeToJson(debugContext.getDebugConfig(), "debug config");
    }
    return null;
  }

  private static String serializeCompareConfig(DebugContext debugContext) {
    if (debugContext.getCompareConfig() != null) {
      return serializeToJson(debugContext.getCompareConfig(), "compare config");
    }
    return null;
  }

  private static String serializeToJson(Object obj, String fieldName) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialize {}: {}", fieldName, e.getMessage(), e);
      return null;
    }
  }

  private static PromptDebugContextEntity buildPromptDebugContextEntity(DebugContext debugContext,
      String mockContexts, String mockVariables, String mockTools, String debugConfig, String compareConfig) {
    return PromptDebugContextEntity.builder()
      .promptId(debugContext.getPromptId())
      .userId(debugContext.getUserId())
      .mockContexts(mockContexts)
      .mockVariables(mockVariables)
      .mockTools(mockTools)
      .debugConfig(debugConfig)
      .compareConfig(compareConfig)
      .build();
  }

  /**
   * 转换DebugContext PO到DO
   * 迁移对应关系: Go语言convertor.DebugContextPO2DO
   * - 功能: 将调试上下文PO转换为DO
   * - 参数: 调试上下文PO
   * - 返回: 调试上下文DO
   */
  public static DebugContext debugContextPO2DO(PromptDebugContextEntity po) {
    if (po == null) {
      return null;
    }

    List<DebugMessage> mockContexts = null;
    List<VariableVal> mockVariables = null;
    List<MockTool> mockTools = null;
    DebugConfig debugConfig = null;
    CompareConfig compareConfig = null;

    try {
      if (po.getMockContexts() != null) {
        mockContexts = objectMapper.readValue(po.getMockContexts(),
          new TypeReference<List<DebugMessage>>() {
          });
      }
      if (po.getMockVariables() != null) {
        mockVariables = objectMapper.readValue(po.getMockVariables(),
          new TypeReference<List<VariableVal>>() {
          });
      }
      if (po.getMockTools() != null) {
        mockTools = objectMapper.readValue(po.getMockTools(),
          new TypeReference<List<MockTool>>() {
          });
      }
      if (po.getDebugConfig() != null) {
        debugConfig = objectMapper.readValue(po.getDebugConfig(), DebugConfig.class);
      }
      if (po.getCompareConfig() != null) {
        compareConfig = objectMapper.readValue(po.getCompareConfig(), CompareConfig.class);
      }
    }
    catch (JsonProcessingException e) {
      // 记录日志但不抛出异常
      logger.error("Failed to deserialize debug context: {}", e.getMessage(), e);
    }

    DebugCore debugCore = DebugCore.builder()
      .mockContexts(mockContexts)
      .mockVariables(mockVariables)
      .mockTools(mockTools)
      .build();

    return DebugContext.builder()
      .promptId(po.getPromptId())
      .userId(po.getUserId())
      .debugCore(debugCore)
      .debugConfig(debugConfig)
      .compareConfig(compareConfig)
      .build();
  }
}
