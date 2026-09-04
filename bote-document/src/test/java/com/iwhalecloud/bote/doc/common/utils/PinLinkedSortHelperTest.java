package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link PinLinkedSortHelper} 单元测试。
 *
 * <p>sortByLinkedList 为纯链表排序逻辑，用内部 TestNode 覆盖空/无头/正常链/断链分支；
 * enforcePinLimit 用 @Mock HomepageMapper 覆盖 ≤5 跳过、null 跳过、>5 裁剪与 prevId==0 置空分支。</p>
 */
@ExtendWith(MockitoExtension.class)
class PinLinkedSortHelperTest {

  /** 简单链表节点实现，用于测试 sortByLinkedList 泛型排序。 */
  static final class TestNode implements PinLinkedSortHelper.LinkedListNode<TestNode> {
    private final Long pinId;
    private final Long prevPinId;
    private final Date pinTime;

    TestNode(Long pinId, Long prevPinId, Date pinTime) {
      this.pinId = pinId;
      this.prevPinId = prevPinId;
      this.pinTime = pinTime;
    }

    @Override
    public Long getPinId() {
      return pinId;
    }

    @Override
    public Long getPrevPinId() {
      return prevPinId;
    }

    @Override
    public Date getPinTime() {
      return pinTime;
    }
  }

  // ==================== sortByLinkedList(List) ====================

  @Test
  void sortByLinkedList_null_returnsNull() {
    assertThat(PinLinkedSortHelper.sortByLinkedList(null)).isNull();
  }

  @Test
  void sortByLinkedList_empty_returnsEmpty() {
    assertThat(PinLinkedSortHelper.sortByLinkedList(new ArrayList<TestNode>())).isEmpty();
  }

  @Test
  void sortByLinkedList_noHead_sortsByPinTime() {
    // 两条记录都有 prevPinId（无头），按 pinTime 升序
    TestNode n2 = new TestNode(2L, 1L, new Date(2000));
    TestNode n3 = new TestNode(3L, 2L, new Date(1000));
    List<TestNode> result = PinLinkedSortHelper.sortByLinkedList(new ArrayList<>(List.of(n2, n3)));
    assertThat(result).extracting(TestNode::getPinId).containsExactly(3L, 2L);
  }

  @Test
  void sortByLinkedList_normalChain_rebuildsOrder() {
    // A(prev=null)->B(prev=A)->C(prev=B)，打乱输入
    TestNode a = new TestNode(1L, null, new Date(1000));
    TestNode b = new TestNode(2L, 1L, new Date(2000));
    TestNode c = new TestNode(3L, 2L, new Date(3000));
    List<TestNode> result = PinLinkedSortHelper.sortByLinkedList(new ArrayList<>(List.of(c, a, b)));
    assertThat(result).extracting(TestNode::getPinId).containsExactly(1L, 2L, 3L);
  }

  @Test
  void sortByLinkedList_brokenChain_appendsUnsortedByPinTime() {
    // A->B 正常链，O 为断链孤儿
    TestNode a = new TestNode(1L, null, new Date(1000));
    TestNode b = new TestNode(2L, 1L, new Date(2000));
    TestNode orphan = new TestNode(9L, 99L, new Date(500));
    List<TestNode> result = PinLinkedSortHelper.sortByLinkedList(new ArrayList<>(List.of(orphan, a, b)));
    // 链内 [A,B]，孤儿按 pinTime 追加到末尾
    assertThat(result).extracting(TestNode::getPinId).containsExactly(1L, 2L, 9L);
  }

  // ==================== sortByLinkedList(List, Function) ====================

  @Test
  void sortByLinkedListWithComparator_null_returnsNull() {
    Function<List<TestNode>, List<TestNode>> cmp = Function.identity();
    assertThat(PinLinkedSortHelper.sortByLinkedList(null, cmp)).isNull();
  }

  @Test
  void sortByLinkedListWithComparator_noHead_usesComparator() {
    TestNode n2 = new TestNode(2L, 1L, new Date(2000));
    TestNode n3 = new TestNode(3L, 2L, new Date(1000));
    Function<List<TestNode>, List<TestNode>> cmp = list -> list.stream()
      .sorted((x, y) -> y.getPinId().compareTo(x.getPinId())).collect(Collectors.toList());
    List<TestNode> result = PinLinkedSortHelper.sortByLinkedList(new ArrayList<>(List.of(n2, n3)), cmp);
    // 无头时使用自定义比较器（按 pinId 倒序）
    assertThat(result).extracting(TestNode::getPinId).containsExactly(3L, 2L);
  }

  @Test
  void sortByLinkedListWithComparator_normalChain_rebuildsOrder() {
    TestNode a = new TestNode(1L, null, new Date(1000));
    TestNode b = new TestNode(2L, 1L, new Date(2000));
    TestNode c = new TestNode(3L, 2L, new Date(3000));
    Function<List<TestNode>, List<TestNode>> cmp = Function.identity();
    List<TestNode> result = PinLinkedSortHelper.sortByLinkedList(new ArrayList<>(List.of(b, c, a)), cmp);
    assertThat(result).extracting(TestNode::getPinId).containsExactly(1L, 2L, 3L);
  }

  // ==================== enforcePinLimit ====================

  @Mock
  private HomepageMapper homepageMapper;

  private UserHomepagePinEntity pin(long pinId, Long prevPinId) {
    UserHomepagePinEntity entity = new UserHomepagePinEntity();
    entity.setPinId(pinId);
    entity.setPrevPinId(prevPinId);
    return entity;
  }

  @Test
  void enforcePinLimit_underLimit_skipsDelete() {
    when(homepageMapper.selectActivePinsByUserAndType(1L, "DOCUMENT", 10L, 100L))
      .thenReturn(List.of(pin(1L, null), pin(2L, 1L), pin(3L, 2L)));

    PinLinkedSortHelper.enforcePinLimit(1L, "DOCUMENT", 10L, 100L, homepageMapper);

    verify(homepageMapper, never()).deletePinRecord(anyLong());
    verify(homepageMapper, never()).updatePrevPinIdForMovedNode(anyLong(), anyLong(), anyLong());
  }

  @Test
  void enforcePinLimit_nullPins_skipsDelete() {
    when(homepageMapper.selectActivePinsByUserAndType(1L, "DOCUMENT", 10L, 100L)).thenReturn(null);

    PinLinkedSortHelper.enforcePinLimit(1L, "DOCUMENT", 10L, 100L, homepageMapper);

    verify(homepageMapper, never()).deletePinRecord(anyLong());
  }

  @Test
  void enforcePinLimit_overLimit_deletesExcessAndFixesLinks() {
    // 7 条链：A->B->C->D->E->F->G，保留前 5，删除 F(6)、G(7)
    List<UserHomepagePinEntity> pins = new ArrayList<>();
    pins.add(pin(1L, null));
    pins.add(pin(2L, 1L));
    pins.add(pin(3L, 2L));
    pins.add(pin(4L, 3L));
    pins.add(pin(5L, 4L));
    pins.add(pin(6L, 5L));
    pins.add(pin(7L, 6L));
    when(homepageMapper.selectActivePinsByUserAndType(1L, "DOCUMENT", 10L, 100L)).thenReturn(pins);
    when(homepageMapper.getCurrentPrevPinId(6L, 1L)).thenReturn(5L);
    when(homepageMapper.getCurrentPrevPinId(7L, 1L)).thenReturn(0L); // 触发 newPrev=null 分支

    PinLinkedSortHelper.enforcePinLimit(1L, "DOCUMENT", 10L, 100L, homepageMapper);

    // F 的前驱为 5，newPrev=5；G 的前驱为 0，newPrev=null
    verify(homepageMapper).updatePrevPinIdForMovedNode(6L, 5L, 1L);
    verify(homepageMapper).updatePrevPinIdForMovedNode(7L, null, 1L);
    verify(homepageMapper).deletePinRecord(6L);
    verify(homepageMapper).deletePinRecord(7L);
    // 保留的 1-5 不应被删除
    verify(homepageMapper, never()).deletePinRecord(1L);
    verify(homepageMapper, never()).deletePinRecord(5L);
  }
}
