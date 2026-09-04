package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.EditLockInfoDTO;
import com.iwhalecloud.bote.dto.base.LockResultDTO;
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 编辑锁控制层
 *
 * @author qian.sisheng
 * @since 2025-05-09
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "editLock", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "编辑锁")
public class EditLockController {
  private final IEditLockService editLockService;

  @Operation(summary = "获取编辑锁")
  @GetMapping("acquireLock")
  public ResultVO<EditLockInfoDTO> acquireLock(@RequestParam("id") Long id, @RequestParam("type") String type,
    @RequestParam(value = "entityName", required = false) String entityName,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    return editLockService.acquireLock(id, type, entityName, tenantId);
  }

  @Operation(summary = "续期编辑锁")
  @GetMapping("renewLock")
  public ResultVO<LockResultDTO> renewLock(@RequestParam("id") Long id, @RequestParam("type") String type,
    @RequestParam("entityName") String entityName,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    return editLockService.renewLock(id, type, entityName, tenantId);
  }

  @Operation(summary = "释放编辑锁")
  @PostMapping("release")
  public ResultVO<Void> release(@RequestParam("id") Long id, @RequestParam("type") String type, @RequestParam("tenantId") Long tenantId) {
    editLockService.release(id, type, tenantId);
    return ResultVO.success();
  }

  @Operation(summary = "解锁")
  @GetMapping("unLock")
  public ResultVO<Void> unLock(@RequestParam("id") Long id, @RequestParam("type") String type,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    editLockService.unLock(id, type, tenantId);
    return ResultVO.success();
  }
}
