package com.iwhalecloud.bote.generator.flow.converter.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.dto.generator.flow.node.IfNodeData;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep.BranchSpec;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.lang.Nullable;

/**
 * 条件节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class IfNodeConverter extends AbstractNodeConverter<IfNodeData> {
  public IfNodeConverter() {
    super(IfNodeData.class);
  }

  @Override
  protected void simplifyNodeData(IfNodeData data) {
    for (BranchSpec branch : ListUtils.emptyIfNull(data.getBranches())) {
      branch.setCondition(simplifyIfCondition(branch.getCondition()));
    }
  }

  /**
   * 简化条件，减少不必要的层级
   */
  @Nullable
  private IfCondition simplifyIfCondition(IfCondition condition) {
    if ("and".equals(condition.getOperator()) || "or".equals(condition.getOperator())) {
      if (CollectionUtils.isEmpty(condition.getChildren())) {
        return null;
      }
      if (condition.getChildren().size() == 1) {
        return simplifyIfCondition(condition.getChildren().get(0));
      }
      condition.setChildren(condition.getChildren().stream().map(this::simplifyIfCondition).filter(Objects::nonNull).collect(Collectors.toList()));
      return condition;
    }
    return condition;
  }

  @Override
  protected Map<String, Object> revertNodeData(FlowConverterContext context, IfNodeData data) {
    // 补充条件的层级、key，避免前端报错
    List<BranchSpec> branches = ListUtils.emptyIfNull(data.getBranches());
    List<Map<String, Object>> branchMapList = new ArrayList<>();
    for (BranchSpec branch : branches) {
      IfCondition condition = branch.getCondition();
      if (condition == null) {
        condition = IfCondition.or(IfCondition.and());
      }
      else if (condition.isAnd()) {
        condition = IfCondition.or(condition);
      }
      else if (condition.isOr()) {
        condition.setChildren(condition.getChildren().stream().map(IfCondition::and).collect(Collectors.toList()));
      }
      else if (!condition.isOr()) {
        condition = IfCondition.or(IfCondition.and(condition));
      }
      ObjectNode node = JsonUtil.convert(condition, ObjectNode.class);
      node.put("key", "0");
      fillConditionKey(node);

      Map<String, Object> map = new LinkedHashMap<>();
      map.put("branchCode", branch.getBranchCode());
      map.put("branchName", branch.getBranchName());
      map.put("condition", node);
      branchMapList.add(map);
    }

    return ImmutableMap.of("branches", branchMapList);
  }

  /**
   * 补充条件的 key
   */
  private void fillConditionKey(ObjectNode condition) {
    JsonNode children = condition.get("children");
    if (children instanceof ArrayNode) {
      for (int i = 0; i < children.size(); i++) {
        ObjectNode child = (ObjectNode) children.get(i);
        child.put("key", UUID.randomUUID().toString());
        fillConditionKey(child);
      }
    }
  }
}
