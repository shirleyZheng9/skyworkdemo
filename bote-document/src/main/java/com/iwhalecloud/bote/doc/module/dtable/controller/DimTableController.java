package com.iwhalecloud.bote.doc.module.dtable.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.dtable.dto.DocumentDimTableRelaDTO;
import com.iwhalecloud.bote.doc.module.dtable.entity.DocumentDimTableRelaEntity;
import com.iwhalecloud.bote.doc.module.dtable.service.DimTableClientService;
import com.iwhalecloud.bote.doc.module.dtable.service.IDocumentDimTableRelaService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Aiqing
 * @since 2026/1/4
 */
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/dtable")
@Tag(name = "文档中心-多维表格")
@RequiredArgsConstructor
public class DimTableController {

  private static final Logger logger = LoggerFactory.getLogger(DimTableController.class);

  private final IDocumentDimTableRelaService documentDimTableRelaService;
  private final DimTableClientService dimtableClientService;

  @Operation(summary = "查询文档关联多维表格信息")
  @GetMapping("queryDimTableRela")
  public ResultVO<DocumentDimTableRelaDTO> queryDimTableRela(@RequestParam String documentId) {

    DocumentDimTableRelaEntity dimTableRelaEntity = documentDimTableRelaService.findByDocumentId(documentId);
    if (dimTableRelaEntity == null) {
      logger.warn("文档与多维表格关联关系不存在, documentId:{}", documentId);
      throw new BssException("查询文档信息失败");
    }

    String tableSpaceId = dimTableRelaEntity.getTableSpaceId();
    String nodeId = dimTableRelaEntity.getRootNodeId();
    Assert.isTrue(StringUtils.isNotBlank(tableSpaceId), "文档关联参数异常");

    String firstNodeId = dimtableClientService.queryDocumentSpaceFirstNodeId(tableSpaceId);
    if (StringUtils.isNotBlank(firstNodeId)) {
      nodeId = firstNodeId;
    }
    DocumentDimTableRelaDTO relaDTO = new DocumentDimTableRelaDTO();
    relaDTO.setDocumentId(documentId);
    relaDTO.setSpaceId(tableSpaceId);
    relaDTO.setNodeId(nodeId);
    return ResultVO.success(relaDTO);
  }

}
