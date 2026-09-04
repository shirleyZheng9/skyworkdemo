package com.iwhalecloud.bote.doc.common.support.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link DefaultTreeBuildFactory} 单元测试。
 *
 * <p>覆盖多级树构建、空列表、单根无子、自定义根 ID、多根、孤儿节点丢弃、默认根 ID 常量。
 * 经 TestTreeNode（实现 {@link Tree}）构造扁平节点列表后构建为嵌套树。</p>
 */
class DefaultTreeBuildFactoryTest {

  @Test
  void doTreeBuild_multiLevel_buildsNestedTree() {
    TestTreeNode root = new TestTreeNode("root", "0");
    TestTreeNode child = new TestTreeNode("child", "root");
    TestTreeNode grand = new TestTreeNode("grand", "child");
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>();

    List<TestTreeNode> tree = factory.doTreeBuild(new ArrayList<>(List.of(grand, root, child)));

    assertThat(tree).hasSize(1);
    assertThat(tree.get(0).getNodeId()).isEqualTo("root");
    assertThat(tree.get(0).getChildrenNodes()).extracting(TestTreeNode::getNodeId).containsExactly("child");
    assertThat(tree.get(0).getChildrenNodes().get(0).getChildrenNodes())
      .extracting(TestTreeNode::getNodeId).containsExactly("grand");
  }

  @Test
  void doTreeBuild_emptyList_returnsEmpty() {
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>();
    assertThat(factory.doTreeBuild(new ArrayList<>())).isEmpty();
  }

  @Test
  void doTreeBuild_singleRoot_noChildren() {
    TestTreeNode root = new TestTreeNode("r", "0");
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>();

    List<TestTreeNode> tree = factory.doTreeBuild(new ArrayList<>(List.of(root)));

    assertThat(tree).extracting(TestTreeNode::getNodeId).containsExactly("r");
    assertThat(tree.get(0).getChildrenNodes()).isEmpty();
  }

  @Test
  void doTreeBuild_customRootId_buildsFromCustomRoot() {
    TestTreeNode root = new TestTreeNode("r", "ROOT");
    TestTreeNode child = new TestTreeNode("c", "r");
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>("ROOT");

    List<TestTreeNode> tree = factory.doTreeBuild(new ArrayList<>(List.of(child, root)));

    assertThat(tree).extracting(TestTreeNode::getNodeId).containsExactly("r");
    assertThat(tree.get(0).getChildrenNodes()).extracting(TestTreeNode::getNodeId).containsExactly("c");
  }

  @Test
  void doTreeBuild_multipleRoots_allReturned() {
    TestTreeNode r1 = new TestTreeNode("r1", "0");
    TestTreeNode r2 = new TestTreeNode("r2", "0");
    TestTreeNode c1 = new TestTreeNode("c1", "r1");
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>();

    List<TestTreeNode> tree = factory.doTreeBuild(new ArrayList<>(List.of(r1, r2, c1)));

    assertThat(tree).extracting(TestTreeNode::getNodeId).containsExactlyInAnyOrder("r1", "r2");
    TestTreeNode r1Node = tree.stream().filter(n -> "r1".equals(n.getNodeId())).findFirst().orElseThrow();
    assertThat(r1Node.getChildrenNodes()).extracting(TestTreeNode::getNodeId).containsExactly("c1");
  }

  @Test
  void doTreeBuild_orphanNode_dropped() {
    TestTreeNode root = new TestTreeNode("root", "0");
    // parentId "missing" 既非根，也非任何节点 ID，属孤儿节点
    TestTreeNode orphan = new TestTreeNode("orphan", "missing");
    DefaultTreeBuildFactory<TestTreeNode> factory = new DefaultTreeBuildFactory<>();

    List<TestTreeNode> tree = factory.doTreeBuild(new ArrayList<>(List.of(root, orphan)));

    assertThat(tree).extracting(TestTreeNode::getNodeId).containsExactly("root");
  }

  @Test
  void defaultRootParentId_isZero() {
    assertThat(DefaultTreeBuildFactory.ROOT_PARENT_ID).isEqualTo("0");
  }

  /** 测试用树节点。 */
  static class TestTreeNode implements Tree<TestTreeNode> {

    private final String id;
    private final String parentId;
    private List<TestTreeNode> children = new ArrayList<>();

    TestTreeNode(String id, String parentId) {
      this.id = id;
      this.parentId = parentId;
    }

    @Override
    public String getNodeId() {
      return id;
    }

    @Override
    public String getNodeParentId() {
      return parentId;
    }

    @Override
    public List<TestTreeNode> getChildrenNodes() {
      return children;
    }

    @Override
    public void setChildrenNodes(List<TestTreeNode> childrenNodes) {
      this.children = childrenNodes;
    }
  }
}
