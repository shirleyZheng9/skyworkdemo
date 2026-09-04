package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeGraphManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * knowledgeGraph 管理 controller
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
@RestController
@ConditionalOnBooleanProperty(name = "knowledge.knowledgeGraph.enabled")
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/knowledgeGraph", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "knowledgeGraph管理类")
@SuppressWarnings("PMD.GuardLogStatement")
public class KnowledgeGraphManageController {

  private final IKnowledgeGraphManageService knowledgeGraphManageService;

  @Operation(summary = "获取knowledgeGraph的知识库列表")
  @GetMapping("queryKnowledgeGraphList")
  public ResultVO<List<KnowledgeGraphDTO>> queryKnowledgeGraphList(@RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(knowledgeGraphManageService.queryKnowledgeGraphList(tenantId));
  }

  @Operation(summary = "获取 knowledgeGraph Token")
  @GetMapping("getKnowledgeGraphToken")
  public ResultVO<KnowledgeGraphTokenDTO> getKnowledgeGraphToken(@RequestParam("tenantId") Long tenantId) {
    return knowledgeGraphManageService.getKnowledgeGraphToken(tenantId);
  }
}
