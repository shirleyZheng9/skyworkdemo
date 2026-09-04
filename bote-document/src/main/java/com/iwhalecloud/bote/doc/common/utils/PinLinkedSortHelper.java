package com.iwhalecloud.bote.doc.common.utils;

import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;

/**
 * 链表排序工具类 用于对具有prevPinId和pinId字段的实体进行链表结构排序
 *
 * @author lizuyin
 * @since 2025-08-18
 */
public final class PinLinkedSortHelper {

  private PinLinkedSortHelper() {
  }

  /**
   * 根据链表结构对实体列表进行排序 使用前驱指针重建完整的排序链表
   *
   * @param <T> 实体类型，必须实现LinkedListNode接口
   * @param entities 待排序的实体列表
   * @return 排序后的实体列表
   */
  public static <T extends LinkedListNode<T>> List<T> sortByLinkedList(List<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return entities;
    }

    // 找到链表头（prevPinId为null的记录）
    T head = findLinkedListHead(entities);

    // 如果找不到链表头，按置顶时间排序
    if (head == null) {
      return sortByPinTime(entities);
    }

    // 从链表头开始，按前驱指针重建排序
    List<T> sortedList = rebuildLinkedList(head, entities);

    // 处理异常情况，确保所有实体都被排序
    return handleSortingExceptions(sortedList, entities);
  }

  /**
   * 查找链表头（prevPinId为null的记录）
   *
   * @param <T> 实体类型
   * @param entities 实体列表
   * @return 链表头实体，如果找不到则返回null
   */
  @Nullable
  private static <T extends LinkedListNode<T>> T findLinkedListHead(List<T> entities) {
    return entities.stream().filter(entity -> entity.getPrevPinId() == null).findFirst().orElse(null);
  }

  /**
   * 按置顶时间对实体排序
   *
   * @param <T> 实体类型
   * @param entities 实体列表
   * @return 按置顶时间排序后的列表
   */
  private static <T extends LinkedListNode<T>> List<T> sortByPinTime(List<T> entities) {
    return entities.stream().sorted((a, b) -> {
      if (a.getPinTime() == null && b.getPinTime() == null) {
        return 0;
      }
      if (a.getPinTime() == null) {
        return 1;
      }
      if (b.getPinTime() == null) {
        return -1;
      }
      return a.getPinTime().compareTo(b.getPinTime());
    }).collect(Collectors.toList());
  }

  /**
   * 重建链表结构
   *
   * @param <T> 实体类型
   * @param head 链表头
   * @param entities 所有实体列表
   * @return 重建后的有序链表
   */
  private static <T extends LinkedListNode<T>> List<T> rebuildLinkedList(T head, List<T> entities) {
    List<T> sortedList = new ArrayList<>();
    T current = head;

    while (current != null) {
      sortedList.add(current);
      current = findNextNode(current, entities);
    }

    return sortedList;
  }

  /**
   * 查找下一个节点 通过prevPinId字段找到当前节点的下一个节点
   *
   * @param <T> 实体类型
   * @param current 当前节点
   * @param entities 所有实体列表
   * @return 下一个节点，如果找不到则返回null
   */
  private static <T extends LinkedListNode<T>> T findNextNode(T current, List<T> entities) {
    return entities.stream()
      .filter(entity -> entity.getPrevPinId() != null && entity.getPrevPinId().equals(current.getPinId())).findFirst()
      .orElse(null);
  }

  /**
   * 处理排序异常情况 确保所有实体都被正确排序，处理链表断链等异常情况
   *
   * @param <T> 实体类型
   * @param sortedList 已排序的列表
   * @param entities 原始实体列表
   * @return 完整的排序列表
   */
  private static <T extends LinkedListNode<T>> List<T> handleSortingExceptions(List<T> sortedList, List<T> entities) {
    // 如果排序后的数量与原始数量一致，说明链表结构完整
    if (sortedList.size() == entities.size()) {
      return sortedList;
    }

    // 将未排序的项按置顶时间添加到末尾
    List<T> unsorted = findUnsortedEntities(sortedList, entities);
    List<T> sortedUnsorted = sortByPinTime(unsorted);
    sortedList.addAll(sortedUnsorted);

    return sortedList;
  }

  /**
   * 查找未排序的实体
   *
   * @param <T> 实体类型
   * @param sortedList 已排序的列表
   * @param entities 原始实体列表
   * @return 未排序的实体列表
   */
  private static <T extends LinkedListNode<T>> List<T> findUnsortedEntities(List<T> sortedList, List<T> entities) {
    return entities.stream().filter(entity -> !sortedList.contains(entity)).collect(Collectors.toList());
  }

  /**
   * 根据链表结构对实体列表进行排序（使用自定义比较器） 适用于需要自定义排序逻辑的场景
   *
   * @param <T> 实体类型，必须实现LinkedListNode接口
   * @param entities 待排序的实体列表
   * @param timeComparator 时间比较器，用于在链表结构不完整时进行备选排序
   * @return 排序后的实体列表
   */
  public static <T extends LinkedListNode<T>> List<T> sortByLinkedList(List<T> entities,
                                                                       Function<List<T>, List<T>> timeComparator) {
    if (entities == null || entities.isEmpty()) {
      return entities;
    }

    // 找到链表头（prevPinId为null的记录）
    T head = findLinkedListHead(entities);

    // 如果找不到链表头，使用自定义比较器排序
    if (head == null) {
      return timeComparator.apply(entities);
    }

    // 从链表头开始，按前驱指针重建排序
    List<T> sortedList = rebuildLinkedList(head, entities);

    // 处理异常情况，确保所有实体都被排序
    return handleSortingExceptionsWithCustomComparator(sortedList, entities, timeComparator);
  }

  /**
   * 置顶链表长度限制：最多5条（从头向后保留前5条，多余的按链表规则逻辑删除）。
   * <p>注意：本方法会在必要时执行数据库更新并在最后刷新缓存。</p>
   *
   * @param userId 用户ID
   * @param targetType 目标类型（DOCUMENT/LIBRARY/KNOWLEDGE）
   * @param tenantId 租户ID
   * @param spaceId 空间ID
   * @param homepageMapper 数据访问Mapper
   */
  public static void enforcePinLimit(Long userId, String targetType, Long tenantId, Long spaceId, HomepageMapper homepageMapper) {
    List<UserHomepagePinEntity> allPins = homepageMapper.selectActivePinsByUserAndType(userId, targetType, tenantId, spaceId);
    if (shouldSkipTrimming(allPins)) {
      return;
    }
    Map<Long, UserHomepagePinEntity> prevMap = buildPrevMap(allPins);
    UserHomepagePinEntity head = findHeadNode(allPins);
    if (head == null) {
      return;
    }
    List<Long> toDelete = collectExcessPinIds(head, prevMap, 5);
    if (toDelete.isEmpty()) {
      return;
    }
    deletePinsAndFixLinks(userId, toDelete, homepageMapper);
  }

  /**
   * 是否无需裁剪（为空或不超过限制）。
   *
   * @param allPins 所有有效置顶记录
   * @return 是否跳过裁剪
   * @since 2025-08-19
   */
  private static boolean shouldSkipTrimming(List<UserHomepagePinEntity> allPins) {
    return allPins == null || allPins.size() <= 5;
  }

  /**
   * 基于 prev_pin_id 构建前驱到节点的映射。
   *
   * @param allPins 所有有效置顶记录
   * @return 前驱ID到节点的映射
   */
  private static Map<Long, UserHomepagePinEntity> buildPrevMap(List<UserHomepagePinEntity> allPins) {
    Map<Long, UserHomepagePinEntity> prevMap = new HashMap<Long, UserHomepagePinEntity>();
    for (UserHomepagePinEntity pin : allPins) {
      Long prevId = pin.getPrevPinId();
      if (prevId != null) {
        prevMap.put(prevId, pin);
      }
    }
    return prevMap;
  }

  /**
   * 查找链表头结点（prev_pin_id 为 null）。
   *
   * @param allPins 所有有效置顶记录
   * @return 头结点，未找到返回 null
   * @since 2025-08-19
   */
  private static UserHomepagePinEntity findHeadNode(List<UserHomepagePinEntity> allPins) {
    for (UserHomepagePinEntity pin : allPins) {
      if (pin.getPrevPinId() == null) {
        return pin;
      }
    }
    return null;
  }

  /**
   * 收集超出限制的需要删除的 pinId（从头向后，仅保留 limit 个）。
   *
   * @param head 头结点
   * @param prevMap 前驱映射
   * @param limit 保留数量
   * @return 待删除的 pinId 列表
   * @since 2025-08-19
   */
  private static List<Long> collectExcessPinIds(UserHomepagePinEntity head, Map<Long, UserHomepagePinEntity> prevMap,
                                                int limit) {
    List<Long> toKeep = new ArrayList<>(limit);
    List<Long> toDelete = new ArrayList<>();
    UserHomepagePinEntity current = head;
    while (current != null) {
      if (toKeep.size() < limit) {
        toKeep.add(current.getPinId());
      }
      else {
        toDelete.add(current.getPinId());
      }
      current = prevMap.get(current.getPinId());
    }
    return toDelete;
  }

  /**
   * 删除多余节点并修复链表（将后继的 prev 指向被删节点的前驱）。
   *
   * @param userId 用户ID
   * @param toDelete 待删除 pinId 列表
   * @param homepageMapper 数据访问Mapper
   * @since 2025-08-19
   */
  private static void deletePinsAndFixLinks(Long userId, List<Long> toDelete, HomepageMapper homepageMapper) {
    for (Long pinId : toDelete) {
      Long prevId = homepageMapper.getCurrentPrevPinId(pinId, userId);
      Long newPrev = (prevId != null && prevId == 0L) ? null : prevId;
      homepageMapper.updatePrevPinIdForMovedNode(pinId, newPrev, userId);
      homepageMapper.deletePinRecord(pinId);
    }
  }

  /**
   * 使用自定义比较器处理排序异常情况
   *
   * @param <T> 实体类型
   * @param sortedList 已排序的列表
   * @param entities 原始实体列表
   * @param timeComparator 自定义比较器
   * @return 完整的排序列表
   */
  private static <T extends LinkedListNode<T>> List<T> handleSortingExceptionsWithCustomComparator(List<T> sortedList,
                                                                                                   List<T> entities, Function<List<T>, List<T>> timeComparator) {
    // 如果排序后的数量与原始数量一致，说明链表结构完整
    if (sortedList.size() == entities.size()) {
      return sortedList;
    }
    // 将未排序的项使用自定义比较器排序后添加到末尾
    List<T> unsorted = findUnsortedEntities(sortedList, entities);
    List<T> sortedUnsorted = timeComparator.apply(unsorted);
    sortedList.addAll(sortedUnsorted);
    return sortedList;
  }

  /**
   * 链表节点接口 定义了链表排序所需的基本字段
   *
   * @param <T> 实体类型
   */
  public interface LinkedListNode<T> {
    /**
     * 获取置顶记录ID
     *
     * @return 置顶记录ID
     */
    Long getPinId();

    /**
     * 获取前一个置顶记录ID
     *
     * @return 前一个置顶记录ID，如果为null则表示是链表头
     */
    @Nullable
    Long getPrevPinId();

    /**
     * 获取置顶时间
     *
     * @return 置顶时间
     */
    @Nullable
    Date getPinTime();
  }
}
