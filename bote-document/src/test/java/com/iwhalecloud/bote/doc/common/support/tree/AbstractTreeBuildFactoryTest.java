package com.iwhalecloud.bote.doc.common.support.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractTreeBuildFactory} 模板方法单元测试。
 *
 * <p>以记录调用顺序与入参的子类验证 doTreeBuild 按 beforeBuild -> executeBuilding -> afterBuild
 * 顺序串联，且各阶段输入为上一阶段输出。</p>
 */
class AbstractTreeBuildFactoryTest {

  @Test
  void doTreeBuild_invokesStagesInOrder_andChainsOutputs() {
    RecordingFactory factory = new RecordingFactory();

    List<String> result = factory.doTreeBuild(new ArrayList<>(List.of("a")));

    assertThat(result).containsExactly("a", "before", "exec", "after");
    assertThat(factory.callLog).containsExactly("before", "execute", "after");
    assertThat(factory.beforeInput).containsExactly("a");
    assertThat(factory.executeInput).containsExactly("a", "before");
    assertThat(factory.afterInput).containsExactly("a", "before", "exec");
  }

  /** 记录三阶段调用顺序与入参，并在每阶段向列表追加一个标记。 */
  static class RecordingFactory extends AbstractTreeBuildFactory<String> {

    final List<String> callLog = new ArrayList<>();
    List<String> beforeInput;
    List<String> executeInput;
    List<String> afterInput;

    @Override
    protected List<String> beforeBuild(List<String> nodes) {
      callLog.add("before");
      beforeInput = new ArrayList<>(nodes);
      nodes.add("before");
      return nodes;
    }

    @Override
    protected List<String> executeBuilding(List<String> nodes) {
      callLog.add("execute");
      executeInput = new ArrayList<>(nodes);
      nodes.add("exec");
      return nodes;
    }

    @Override
    protected List<String> afterBuild(List<String> nodes) {
      callLog.add("after");
      afterInput = new ArrayList<>(nodes);
      nodes.add("after");
      return nodes;
    }
  }
}
