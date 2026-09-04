package com.iwhalecloud.bote.doc.common.support.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link NodeSortHelper} 单元测试。
 *
 * <p>覆盖 null/空、>1000 节点短路原序、单向链表按前置节点排序、多首节点、filter 谓词（过滤节点但
 * 保留其后代）、断链孤儿追加。经 TestSortable（实现 {@link SortableNode}）构造节点列表。</p>
 */
class NodeSortHelperTest {

  @Test
  void sort_null_returnsEmpty() {
    assertThat(NodeSortHelper.sortNodeAtSameLevel(null)).isEmpty();
  }

  @Test
  void sort_empty_returnsEmpty() {
    assertThat(NodeSortHelper.sortNodeAtSameLevel(List.of())).isEmpty();
  }

  @Test
  void sort_overThousand_shortCircuitsOriginalOrder() {
    List<TestSortable> nodes = new ArrayList<>();
    for (int i = 0; i < 1001; i++) {
      nodes.add(new TestSortable("n" + i, i == 0 ? null : "n" + (i - 1)));
    }

    List<String> sorted = NodeSortHelper.sortNodeAtSameLevel(nodes);

    assertThat(sorted).hasSize(1001);
    assertThat(sorted.get(0)).isEqualTo("n0");
    assertThat(sorted.get(1000)).isEqualTo("n1000");
  }

  @Test
  void sort_singleChain_ordersByPreNode() {
    // 乱序输入
    List<TestSortable> nodes = List.of(
      new TestSortable("C", "B"),
      new TestSortable("A", null),
      new TestSortable("B", "A"));

    assertThat(NodeSortHelper.sortNodeAtSameLevel(nodes)).containsExactly("A", "B", "C");
  }

  @Test
  void sort_multipleFirstNodes_emitsSiblingsThenDescendants() {
    List<TestSortable> nodes = List.of(
      new TestSortable("A", null),
      new TestSortable("B", null),
      new TestSortable("C", "A"));

    // 先输出所有首节点，再递归其子节点：A、B、C
    assertThat(NodeSortHelper.sortNodeAtSameLevel(nodes)).containsExactly("A", "B", "C");
  }

  @Test
  void sort_filter_excludesFilteredButKeepsDescendants() {
    List<TestSortable> nodes = List.of(
      new TestSortable("A", null),
      new TestSortable("B", "A"),
      new TestSortable("C", "B"));
    // 仅接受 A 与 C，过滤掉 B，但 B 的后代 C 仍被递归输出
    List<String> sorted = NodeSortHelper.sortNodeAtSameLevel(nodes,
      n -> "A".equals(n.getNodeId()) || "C".equals(n.getNodeId()));

    assertThat(sorted).containsExactly("A", "C");
  }

  @Test
  void sort_brokenChain_appendsOrphansAtEnd() {
    List<TestSortable> nodes = List.of(
      new TestSortable("A", null),
      new TestSortable("B", "A"),
      new TestSortable("X", "missing")); // preNodeId 不在列表中

    // A->B 正常链先输出，X 因断链在第二轮补刷末尾
    assertThat(NodeSortHelper.sortNodeAtSameLevel(nodes)).containsExactly("A", "B", "X");
  }

  /** 测试用可排序节点。 */
  static class TestSortable implements SortableNode {

    private final String id;
    private final String preId;

    TestSortable(String id, String preId) {
      this.id = id;
      this.preId = preId;
    }

    @Override
    public String getNodeId() {
      return id;
    }

    @Override
    public String getPreNodeId() {
      return preId;
    }
  }
}
