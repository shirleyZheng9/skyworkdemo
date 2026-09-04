package com.iwhalecloud.bote.controller.cutover;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep.QuestionClassification;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEndpointDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowWithParamDTO;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bote.service.orchestration.converter.SceneDslConverter;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 迁移问题分类节点
 *
 * @author bianjp
 * @since 2025-03-03
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
@SuppressWarnings("PMD.GuardLogStatement")
public class MigrateQuestionClassifierController {
  private static final Logger logger = LoggerFactory.getLogger(MigrateQuestionClassifierController.class);
  private final CutOverMapper cutOverMapper;

  @GetMapping(path = "migrateQuestionClassifier", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateQuestionClassifier(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始迁移问题分类节点\n");
    long total = PageHelper.count(cutOverMapper::selectFlowsWithQuestionClassifierNode);
    if (total == 0) {
      addLog(writer, "没有包含问题分类节点的工作流\n");
      return;
    }
    addLog(writer, "工作流总数: %d\n", total);

    // 处理失败的工作流
    List<SkillFlowWithParamDTO> failedFlows = new ArrayList<>();
    int successCount = 0;
    int skipCount = 0;

    // 分批处理工作流，避免占用过大内存
    int limit = 50;
    try {
      for (int offset = 0; offset < total; offset += limit) {
        //noinspection resource
        PageHelper.offsetPage(offset, limit, false);
        List<SkillFlowWithParamDTO> flows = cutOverMapper.selectFlowsWithQuestionClassifierNode();
        for (SkillFlowWithParamDTO flow : flows) {
          addLog(writer, "迁移租户【%s】的【%s】 ...\n", flow.getTenantNameOrId(), flow.getFlowName());
          SceneGraphDTO graph = JsonUtil.parseJson(flow.getFlowGraphJson(), SceneGraphDTO.class);
          if (graph == null) {
            failedFlows.add(flow);
            addLog(writer, "\t流程图解析失败，跳过\n");
            continue;
          }
          FlowProcessStatus status = migrateFlow(writer, flow, graph);
          switch (status) {
            case SUCCESS:
              successCount++;
              break;
            case FAIL:
              failedFlows.add(flow);
              break;
            default:
              skipCount++;
              break;
          }
        }
      }
    }
    finally {
      PageHelper.clearPage();
    }

    addLog(writer, "迁移完成。耗时: %sms, 总数: %s, 更新: %s, 跳过: %s\n", System.currentTimeMillis() - startTime, total, successCount, skipCount);
    if (!failedFlows.isEmpty()) {
      addLog(writer, "以下工作流迁移失败，请手动处理：\n");
      for (SkillFlowWithParamDTO flow : failedFlows) {
        addLog(writer, "\ttenantId: %s, flowId: %s\n", flow.getTenantId(), flow.getFlowId());
      }
    }
  }

  /**
   * 迁移工作流
   *
   * @return 是否迁移成功
   */
  private FlowProcessStatus migrateFlow(PrintWriter writer, SkillFlowWithParamDTO flow, SceneGraphDTO graph) {
    List<SceneGraphNodeDTO> questionClassifierNodes = graph.getNodes().stream()
      .filter(node -> StepType.QUESTION_CLASSIFIER.equals(node.getNodeType()))
      .collect(Collectors.toList());
    if (questionClassifierNodes.isEmpty()) {
      addLog(writer, "\t流程图没有问题分类节点，跳过\n");
      return FlowProcessStatus.SKIP;
    }

    // 新的线条列表
    List<SceneGraphEdgeDTO> edges = new LinkedList<>(graph.getEdges());
    // 是否需要更新。有可能问题分类节点未连接其它节点，不需要更新
    boolean needUpdate = false;
    for (SceneGraphNodeDTO node : questionClassifierNodes) {
      List<SceneGraphEdgeDTO> targetEdges = edges.stream().filter(e -> node.getNodeCode().equals(e.getSource().getCell())).collect(Collectors.toList());
      if (targetEdges.isEmpty()) {
        addLog(writer, "\t问题分类节点【%s】没有出边，跳过\n", node.getNodeName());
      }
      else if ((targetEdges.size() == 1 && "else".equals(targetEdges.get(0).getSource().getPort())) || targetEdges.size() > 1) {
        addLog(writer, "\t问题分类节点【%s】已迁移，跳过\n", node.getNodeName());
      }
      else {
        addLog(writer, "\t迁移问题分类节点【%s】\n", node.getNodeName());
        needUpdate = true;
        // 删除原来的线条，然后给每个分类都创建一条线条
        edges.remove(targetEdges.get(0));
        SceneGraphEndpointDTO target = targetEdges.get(0).getTarget();
        List<QuestionClassification> classifications = JsonUtil.convert(node.getNodeData().get("classifications"), new TypeReference<List<QuestionClassification>>() {
        });
        for (QuestionClassification classification : ListUtils.emptyIfNull(classifications)) {
          edges.add(new SceneGraphEdgeDTO(new SceneGraphEndpointDTO(node.getNodeCode(), classification.getId().toString()), target));
        }
        edges.add(new SceneGraphEdgeDTO(new SceneGraphEndpointDTO(node.getNodeCode(), "else"), target));
      }
    }

    if (!needUpdate) {
      return FlowProcessStatus.SKIP;
    }

    try {
      graph.setEdges(edges);
      flow.setGraph(graph);
      updateFlow(flow);
      return FlowProcessStatus.SUCCESS;
    }
    catch (Exception e) {
      logger.error("Failed to update flow: tenantId={}, flowId={}", flow.getTenantId(), flow.getFlowId(), e);
      addLog(writer, "\t更新工作流失败: %s\n", ExpUtil.getMsg(e));
      return FlowProcessStatus.FAIL;
    }
  }

  /**
   * 更新工作流
   */
  private void updateFlow(SkillFlowWithParamDTO flow) {
    flow.setFlowGraphJson(JsonUtil.toJsonStringCompact(flow.getGraph()));
    if (StringUtils.isNotEmpty(flow.getVariableJson())) {
      flow.setVariables(JsonUtil.parseJsonRequired(flow.getVariableJson(), new TypeReference<List<ParameterSpec>>() {
      }));
    }
    if (StringUtils.isNotEmpty(flow.getRequestJson())) {
      flow.setRequest(JsonUtil.parseJsonRequired(flow.getRequestJson(), ParameterSpec.class));
    }
    flow.setFlowDsl(SceneDslUtil.toJson(SceneDslConverter.convert(flow)));
    TransactionUtil.executeNew(() -> cutOverMapper.updateFlowGraphAndDsl(flow));
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }

  /**
   * 工作流处理状态
   */
  private enum FlowProcessStatus {
    SUCCESS,
    FAIL,
    SKIP
  }
}
