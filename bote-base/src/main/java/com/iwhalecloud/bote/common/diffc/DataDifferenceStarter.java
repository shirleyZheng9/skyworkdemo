package com.iwhalecloud.bote.common.diffc;

import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.diffc.ComputeArgument;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.diffc.support.log.DataDifferenceLogSupport;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import com.iwhalecloud.bss.litchi.diffc.vo.log.OperLogMessage;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 计算差异辅助类
 *
 * @author chen.linfa
 * @since 2024-10-25
 */
public final class DataDifferenceStarter {
  private DataDifferenceStarter() {
  }

  /**
   * 计算差异,并默认保存
   *
   * @param oldObject 老数据
   * @param newObject 新数据
   * @param computeWithChildren 是否需要计算孩子节点
   * @param <T> 数据类型
   * @return 数据差异结果
   */
  @Nullable
  public static <T> DataDifference<T> computeSave(@Nullable T oldObject, T newObject, boolean computeWithChildren, @Nullable Long tenantId) {
    // 回填更新时间
    final Consumer<DataDifference<T>> updateTimeConsumer = difference -> {
      // 如果为虚拟对象或差异数据为空，则不回填更新时间
      if (difference == null || isVirtualObject(difference)) {
        return;
      }
      Class<?> clazz = getClazz(difference);
      Date date = getUpdatedTime(clazz, difference, tenantId);
      setUpdatedTime(clazz, difference, date);
    };
    return com.iwhalecloud.bss.litchi.diffc.DataDifferenceStarter.computeSave(oldObject, newObject, false,
      ComputeArgument.builder().computeWithChildren(computeWithChildren).build(), null, Collections.singletonList(updateTimeConsumer));
  }

  /**
   * 计算差异,并默认保存,保存操作日志
   *
   * @param oldObject 老数据
   * @param newObject 新数据
   * @param computeWithChildren 是否需要计算孩子节点
   * @param tenantId 租户 ID
   * @param operClass 操作类型
   * @param <T> 数据类型
   * @return 数据差异结果
   */
  @Nullable
  public static <T> DataDifference<T> computeSaveAndLog(@Nullable T oldObject, T newObject, boolean computeWithChildren, Long tenantId,
    OperClassEnum operClass) {

    Map<String, Object> logExtInfo = new HashMap<>(2);
    // 数据落到表字段 bt_oper_log.app_id
    logExtInfo.put("appId", tenantId);
    // 数据落到表字段 bt_oper_log.obj_desc
    logExtInfo.put("dataJson", JsonUtil.toJsonStringCompact(newObject));
    // 记录日志
    final Consumer<DataDifference<T>> logConsumer = difference -> {
      OperLogMessage operLog = DataDifferenceLogSupport.newLog(difference, operClass.name());
      // 推送异步内存队列
      if (operLog != null) {
        operLog.setExtInfo(logExtInfo);
        DisruptorUtil.getInstance().produce(operLog);
      }
    };

    // 回填更新时间
    final Consumer<DataDifference<T>> updateTimeConsumer = difference -> {
      // 如果为虚拟对象或差异数据为空，则不回填更新时间
      if (difference == null || isVirtualObject(difference)) {
        return;
      }
      Class<?> clazz = getClazz(difference);
      Date date = getUpdatedTime(clazz, difference, tenantId);
      setUpdatedTime(clazz, difference, date);
    };

    DataDifference<T> difference = com.iwhalecloud.bss.litchi.diffc.DataDifferenceStarter.computeSave(oldObject, newObject, false,
      ComputeArgument.builder().computeWithChildren(computeWithChildren).build(), null, Arrays.asList(logConsumer, updateTimeConsumer));
    // 记录实体关系
    if (difference != null) {
      if (ResourceElementFactory.supports(operClass.name())) {
        ResourceElementFactory.get(operClass.name()).submit(tenantId, Long.valueOf(difference.getId()));
      }
    }
    return difference;
  }

  /**
   * 获取VO类
   *
   * @param difference 数据差异对象
   * @return 类对象
   */
  private static Class<?> getClazz(DataDifference<?> difference) {
    Class<?> clazz = difference.getToSaveData().getClass().getSuperclass();
    // 如果DTO类直接继承BaseVO,则返回DTO类，否则返回Entity类
    if (ClassUtils.getPackageName(BaseEntity.class).equals(ClassUtils.getPackageName(clazz))) {
      return difference.getToSaveData().getClass();
    }
    else {
      return clazz;
    }
  }


  /**
   * 判断是否为虚拟对象
   *
   * @param difference 数据差异对象
   * @return 结果
   */
  private static boolean isVirtualObject(DataDifference<?> difference) {
    Class<?> clazz = difference.getToSaveData().getClass();
    return clazz.getAnnotation(DiffNode.class) == null;
  }

  /**
   * 获取状态时间
   *
   * @param clazz 类对象
   * @param difference 数据差异对象
   * @return 状态时间
   */
  @SuppressFBWarnings("SECSQLISPRJDBC")
  @Nullable
  private static Date getUpdatedTime(Class<?> clazz, DataDifference<?> difference, @Nullable Long tenantId) {
    Date date;
    String tableCode = difference.getName();
    String primaryKey = difference.getSpec().getIdSpec().getName();
    String primaryValue = difference.getId();
    boolean uuidFlag = difference.getSpec().getIdSpec().getField().getType() != Long.class;
    String sql = "select updated_time from " + tableCode + " where " + primaryKey + "=?";
    List<Object> args = new ArrayList<>();
    args.add(uuidFlag ? primaryValue : Long.valueOf(primaryValue));
    try {
      // 判断 entity 是否含有 tenantId 字段
      if (tenantId != null) {
        boolean hasTenantId = FieldUtils.getAllFieldsList(clazz).stream().anyMatch(p -> "tenantId".equals(p.getName()));
        if (hasTenantId && !"bt_tenant".equals(tableCode)) {
          sql = sql + " and tenant_id in (?, -1)";
          args.add(tenantId);
        }
      }
      List<Date> result = SpringUtil.getBean(JdbcTemplate.class).queryForList(sql, Date.class, args.toArray());
      if (CollectionUtils.isEmpty(result)) {
        // 处理无结果的情况，如返回 null 或抛出自定义异常
        return null;
      }
      date = result.get(0);
    }
    catch (Exception e) {
      throw new BssException("差异化计算, 查询更新时间异常", e);
    }
    return date;
  }

  /**
   * 回填更新时间
   *
   * @param difference 数据差异对象
   * @param date 时间
   */
  private static void setUpdatedTime(Class<?> clazz, DataDifference<?> difference, @Nullable Date date) {
    if (date == null) {
      return;
    }
    try {
      Field field = ReflectionUtils.findField(clazz.getSuperclass(), "updatedTime");
      if (field != null) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, difference.getToSaveData(), date);
      }
    }
    catch (Exception e) {
      throw new BssException("差异化计算, 回填更新时间异常", e);
    }

  }

}
