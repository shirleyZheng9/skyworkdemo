package com.iwhalecloud.bote.doc.module.collaboration.workbook.controller;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.ro.NewChangesRO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.service.WorkbookFacade;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.SheetBlockDataVO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.UniverResultVO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.WorkbookDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 在线表格接口
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/univer-api/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：在线表格接口")
public class UniverWorkbookController {

  private final WorkbookFacade workbookFacade;

  @Operation(summary = "查询在线文档的快照数据")
  @GetMapping("/snapshot/2/unit/{documentId}/rev/0")
  public WorkbookDetailVO queryWorkbookSnapshot(@PathVariable String documentId) {
    return workbookFacade.queryWorkbookSnapshot(documentId);
  }

  @Operation(summary = "查询sheet数据")
  @GetMapping("/snapshot/block/2/unit/{documentId}/block/{blockId}")
  public SheetBlockDataVO querySheetBlockData(@PathVariable Long blockId, @PathVariable String documentId) {
    return workbookFacade.querySheetBlockData(documentId, blockId);
  }

  @Operation(summary = "表格数据更新")
  @PostMapping("/comb/2/unit/{documentId}/new_changes")
  public UniverResultVO newChanges(@RequestBody @Validated NewChangesRO changesRO, @PathVariable String documentId) {
    Assert.isTrue(Objects.equals(changesRO.getUnitID(), documentId), "入参不一致");
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    // 此处需要获取租户
    ThreadPools.getCommon().execute(() -> {
      workbookFacade.newChanges(documentId, changesRO, currentLoginUserId);
    });
    UniverResultVO resultVO = new UniverResultVO();
    resultVO.setError(resultVO.successResult());
    return resultVO;
  }
}
