package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.WeKnoraTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraKnowledgeBaseRespDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.IWeknoraManageService;
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
 * weknora controller
 *
 * @author auto
 * @since 2024-09-20
 */
@RestController
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/weknora", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "weknora管理类")
@SuppressWarnings("PMD.GuardLogStatement")
public class WeknoraManageController {

  // @formatter:off
  private final IWeknoraManageService weknoraManageService;
  // @formatter:on


  @Operation(summary = "获取weknora账号的知识库列表")
  @GetMapping("queryKnowledgeBases")
  public ResultVO<List<WeKnoraKnowledgeBaseRespDTO>> queryKnowledgeBases(@RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(weknoraManageService.queryKnowledgeBases(tenantId));
  }


  @Operation(summary = "获取 WeKnora Token")
  @GetMapping("getWeKnoraToken")
  public ResultVO<WeKnoraTokenDTO> getWeKnoraToken(Long tenantId) {
    return weknoraManageService.getWeKnoraToken(tenantId);
  }
}
