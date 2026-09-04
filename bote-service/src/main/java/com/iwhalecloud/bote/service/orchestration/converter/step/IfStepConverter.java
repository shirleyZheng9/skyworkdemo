package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep.BranchSpec;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 如果步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class IfStepConverter extends AbstractStepConverter<IfStep> {
  public IfStepConverter() {
    super(IfStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, IfStep step, ConverterContext context) {
    List<BranchSpec> branches = parseRequiredJsonAttr(node, "branches", "条件分支", new TypeReference<ArrayList<BranchSpec>>() { //NOPMD - suppressed LooseCoupling - 确保生成的列表可修改
    });
    List<Pair<String, SceneGraphNodeDTO>> targetEdges = context.getTargetEdges(node);
    // 设置每个分支的下一个节点
    for (BranchSpec branch : branches) {
      Pair<String, SceneGraphNodeDTO> edge = IterableUtils.find(targetEdges, e -> branch.getBranchCode().equals(e.getLeft()));
      if (edge != null && !StepType.PARALLEL_END.equals(edge.getRight().getNodeType())) {
        branch.setNext(edge.getRight().getNodeCode());
      }
    }
    // 找出 else 分支
    Pair<String, SceneGraphNodeDTO> elseEdge = IterableUtils.find(targetEdges, e -> "branchElse".equals(e.getLeft()));
    if (elseEdge != null && !StepType.PARALLEL_END.equals(elseEdge.getRight().getNodeType())) {
      BranchSpec elseBranch = new BranchSpec();
      elseBranch.setBranchName("否则");
      elseBranch.setBranchCode("else");
      elseBranch.setNext(elseEdge.getRight().getNodeCode());
      branches.add(elseBranch);
    }
    step.setBranches(branches);
  }
}
