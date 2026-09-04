package com.iwhalecloud.bote.service.element;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 资源关联计算工厂类
 *
 * @author chen.linfa
 * @since 2025-04-10
 */
public abstract class ResourceElementFactory {
  /** 执行器合集。key 为操作符 */
  private static final Map<String, String> executorsMap;

  static {
    executorsMap = new HashMap<>();
    executorsMap.put(OperClassEnum.BOT.name(), ResourceElementConsts.BOT);
    executorsMap.put(OperClassEnum.SCENE.name(), ResourceElementConsts.SCENE);
    executorsMap.put(OperClassEnum.KNOWLEDGE.name(), ResourceElementConsts.KNOWLEDGE);
    executorsMap.put(OperClassEnum.LIBRARY.name(), ResourceElementConsts.LIBRARY);
    executorsMap.put(OperClassEnum.CORPUS.name(), ResourceElementConsts.CORPUS);

    executorsMap.put(OperClassEnum.SKILL_API.name(), ResourceElementConsts.SKILL_SERVICE);
    executorsMap.put(OperClassEnum.SKILL_FLOW.name(), ResourceElementConsts.SKILL_FLOW);
    executorsMap.put(OperClassEnum.SKILL_PAGE.name(), ResourceElementConsts.SKILL_PAGE);
    executorsMap.put(OperClassEnum.SKILL_FUNCTION.name(), ResourceElementConsts.SKILL_FUNCTION);
    executorsMap.put(OperClassEnum.SKILL_ATTR.name(), ResourceElementConsts.SKILL_ATTR);
    executorsMap.put(OperClassEnum.SKILL_PLUGIN.name(), ResourceElementConsts.SKILL_PLUGIN);
    executorsMap.put(OperClassEnum.SKILL_PAGE_FUNC.name(), ResourceElementConsts.SKILL_PAGE_FUNC);
    executorsMap.put(OperClassEnum.SKILL_SQL.name(), ResourceElementConsts.SKILL_SQL);
    executorsMap.put(OperClassEnum.A2A_AGENT.name(), ResourceElementConsts.A2A_AGENT);
    executorsMap.put(OperClassEnum.A2A_PLATFORM.name(), ResourceElementConsts.A2A_PLATFORM);
    executorsMap.put(OperClassEnum.AGENT_SKILL.name(), ResourceElementConsts.AGENT_SKILL);

    executorsMap.put(OperClassEnum.PROMPT.name(), ResourceElementConsts.PROMPT);
    executorsMap.put(OperClassEnum.COPILOT_POINT.name(), ResourceElementConsts.COPILOT_POINT);
    executorsMap.put(OperClassEnum.DATA_TABLE.name(), ResourceElementConsts.DATA_TABLE);

    executorsMap.put(OperClassEnum.JOB.name(), ResourceElementConsts.JOB);
  }

  /**
   * 获取操作符执行器
   *
   * @param operator 操作符
   * @return 执行器
   */
  public static IResourceElementCustomizer get(String operator) {
    String beanName = executorsMap.get(operator);
    Assert.notNull(beanName, () -> "不支持的资源类型: " + operator);
    return SpringUtil.getBean(beanName, IResourceElementCustomizer.class);
  }

  /**
   * 判断是否支持操作符
   *
   * @param operator 操作符
   * @return 是否支持
   */
  public static boolean supports(String operator) {
    return executorsMap.containsKey(operator);
  }
}
