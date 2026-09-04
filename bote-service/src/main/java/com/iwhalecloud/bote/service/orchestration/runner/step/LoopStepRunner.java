package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.LoopStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 循环步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class LoopStepRunner extends AbstractStepRunner<LoopStep> {
  /** 最大循环次数 */
  private static final int MAX_LOOP_COUNT = 10000;

  @Override
  protected void doRun(SceneOrchestrationContext context, LoopStep step) {
    if (StringUtils.isEmpty(step.getChildStep())) {
      context.addStepLog("未配置子步骤，跳过循环");
      logger.warn("No child step found for loop: step={}", step.getName());
      return;
    }

    // 使用迭代器统一不同类型的循环
    Iterator<?> iterator;
    // 循环次数
    int loopCount;
    if (SceneConsts.LOOP_TYPE_RANGE.equals(step.getLoopType())) {
      Pair<RangeIterator, Integer> pair = parseLoopRange(context, step);
      iterator = pair.getLeft();
      loopCount = pair.getRight();
    }
    else if (SceneConsts.LOOP_TYPE_OBJECT.equals(step.getLoopType())) {
      Map<String, Object> object = parseLoopObject(context, step);
      iterator = new ObjectIterator(object);
      loopCount = object.size();
    }
    else {
      // 解析循环列表
      List<?> list = parseLoopList(context, step);
      iterator = list.iterator();
      loopCount = list.size();
    }

    if (loopCount == 0) {
      context.addStepLog("循环次数: 0");
      return;
    }

    try {
      executeLoop(context, step, iterator, loopCount);
    }
    finally {
      context.removeLoopVariable(step.getCode());
    }
  }

  /**
   * 解析循环列表
   */
  private List<?> parseLoopList(SceneOrchestrationContext context, LoopStep step) {
    Object value = SceneParamUtil.getParamValue(step.getList());
    if (value == null) {
      return Collections.emptyList();
    }

    // 检查类型
    if (!(value instanceof List)) {
      logger.warn("Loop list is not a list: step={}, type={}, value={}", step.getName(), value.getClass().getCanonicalName(), value);
      context.addStepLog("循环列表类型错误，期望 List, 实际为 %s, 值为 %s", value.getClass().getCanonicalName(), value);
      throw new BssException(String.format("步骤【%s】的循环列表不是列表类型，实际类型为 %s", step.getName(), value.getClass().getCanonicalName()));
    }

    List<?> list = (List<?>) value;
    // 检查列表长度
    int size = list.size();
    if (size > MAX_LOOP_COUNT) {
      logger.warn("Loop list size exceeds maximum limit: step={}, size={}", step.getName(), size);
      context.addStepLog("循环列表长度超出限制: size=%s", size);
      throw new BssException(String.format("步骤【%s】的循环次数 %s 超出最大限制 %s", step.getName(), size, MAX_LOOP_COUNT));
    }

    context.setStepInputLog(list);
    return list;
  }

  /**
   * 解析循环对象
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> parseLoopObject(SceneOrchestrationContext context, LoopStep step) {
    Object value = SceneParamUtil.getParamValue(step.getObject());
    if (value == null) {
      return Collections.emptyMap();
    }

    // 检查类型
    if (!(value instanceof Map)) {
      logger.warn("Loop object is not a map: step={}, type={}, value={}", step.getName(), value.getClass().getCanonicalName(), value);
      context.addStepLog("循环对象类型错误，期望 Map, 实际为 %s, 值为 %s", value.getClass().getCanonicalName(), value);
      throw new BssException(String.format("步骤【%s】的循环对象不是对象类型，实际类型为 %s", step.getName(), value.getClass().getCanonicalName()));
    }

    Map<String, Object> map = (Map<String, Object>) value;
    // 检查对象元素数量
    int size = map.size();
    if (size > MAX_LOOP_COUNT) {
      logger.warn("Loop object size exceeds maximum limit: step={}, size={}", step.getName(), size);
      context.addStepLog("循环对象属性数量超出限制: size=%s", size);
      throw new BssException(String.format("步骤【%s】的循环次数 %s 超出最大限制 %s", step.getName(), size, MAX_LOOP_COUNT));
    }

    context.setStepInputLog(map);
    return map;
  }

  /**
   * 解析循环范围
   */
  private Pair<RangeIterator, Integer> parseLoopRange(SceneOrchestrationContext context, LoopStep step) {
    Long start = (Long) AttrDataType.INTEGER.convert("start", SceneParamUtil.getParamValue(step.getStart()));
    Long end = (Long) AttrDataType.INTEGER.convert("end", SceneParamUtil.getParamValue(step.getEnd()));
    Long stepLength = (Long) AttrDataType.INTEGER.convert("step", SceneParamUtil.getParamValue(step.getStep()));
    Assert.notNull(start, "循环范围的起始值不能为 null");
    Assert.notNull(end, "循环范围的结束值不能为 null");
    Assert.notNull(stepLength, "循环范围的步长不能为 null");
    Assert.isTrue(stepLength != 0, "循环范围的步长不能为 0");
    Assert.isTrue(start.equals(end) || (start < end && stepLength > 0) || (start > end && stepLength < 0), "循环范围的步长与起始值、结束值不匹配");

    // 检查循环次数
    int loopCount = (int) ((end - start + stepLength) / stepLength);
    if (loopCount > MAX_LOOP_COUNT) {
      logger.warn("Loop range size exceeds maximum limit: step={}, size={}", step.getName(), loopCount);
      context.addStepLog("循环范围数量超出限制: size=%s", loopCount);
      throw new BssException(String.format("步骤【%s】的循环次数 %s 超出最大限制 %s", step.getName(), loopCount, MAX_LOOP_COUNT));
    }

    context.getLastStepRunLogOptional().ifPresent(s -> s.setInput(ImmutableMap.of("start", start, "end", end, "step", stepLength)));
    return Pair.of(new RangeIterator(start, end, stepLength), loopCount);
  }

  /**
   * 执行子步骤
   */
  private void executeLoop(SceneOrchestrationContext context, LoopStep loopStep, Iterator<?> iterator, int loopCount) {
    int i = 0;
    while (iterator.hasNext()) {
      // 每次循环都给循环步骤单独创建一条执行日志
      if (i > 0) {
        context.startStepLog(loopStep);
      }
      i++;
      // 校验循环次数，防止用户在循环内往循环列表添加元素导致死循环
      if (i > loopCount) {
        throw new BssException(String.format("步骤【%s】的循环次数超出预期次数 %s", loopStep.getName(), loopCount));
      }
      Object loopVariable = iterator.next();
      context.setLoopVariable(loopStep.getCode(), loopVariable);
      // 记录并结束循环步骤的日志
      context.addStepLog("循环次数: %d/%d", i, loopCount);
      context.addStepLog("循环变量: %s", loopVariable);
      context.succeedStepLog();
      try {
        // 从 childStep 开始执行步骤及每个步骤的下一步，直到没有下一步或中断循环
        executeSteps(context, loopStep.getChildStep());
        if (context.isReturned()) {
          break;
        }
      }
      // 忽略异常。虽然使用异常做流程控制不太好，但是不使用异常难以兼容在并行节点中退出循环的情况（可能多个并行分支中有循环，因此不能使用公共的上下文对象标记退出循环）
      catch (BreakLoopException e) {
        break;
      }
      catch (ContinueLoopException e) {
        // 继续循环
      }
      finally {
        // 每一轮循环结束时，删除循环内所有节点的出参，避免被下一轮循环误引用
        // 存量数据中可能没有 childrenCodes, 需要兼容
        context.removeStepOutput(loopStep.getChildrenCodes());
      }
    }
  }

  /**
   * 对象迭代器
   */
  private static class ObjectIterator implements Iterator<Map<String, Object>> {
    /** Map 元素迭代器 */
    private final Iterator<Entry<String, Object>> iterator;

    public ObjectIterator(Map<String, Object> map) {
      this.iterator = map.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Map<String, Object> next() {
      Entry<String, Object> entry = iterator.next();
      // 不能使用 ImmutableMap, value 的值可能为 null, 而 ImmutableMap 不支持 null
      Map<String, Object> item = new HashMap<>();
      item.put("key", entry.getKey());
      item.put("value", entry.getValue());
      return item;
    }
  }

  /**
   * 范围迭代器
   */
  private static class RangeIterator implements Iterator<Long> {
    /** 结束值 */
    private final long end;
    /** 步长 */
    private final long step;
    /** 是否结束 */
    private boolean isOver;
    /** 下一个值 */
    private long nextValue;

    public RangeIterator(long start, long end, long step) {
      this.nextValue = start;
      this.end = end;
      this.step = step;
      this.isOver = false;
    }

    @Override
    public boolean hasNext() {
      return !isOver;
    }

    @Override
    public Long next() {
      if (isOver) {
        throw new NoSuchElementException();
      }
      long value = nextValue;
      nextValue = nextValue + step;
      if ((step > 0 && nextValue > end) || (step < 0 && nextValue < end)) {
        isOver = true;
      }
      return value;
    }
  }
}
