package com.iwhalecloud.bote.controller.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.service.portal.tiangong.ITiangongProjectUserSyncService;
import com.iwhalecloud.bote.service.portal.tiangong.TiangongProjectSyncException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.junit.jupiter.api.Test;

/** 天工项目成员内部接口异常契约测试。 */
class TiangongProjectUserSyncControllerTest {

  @Test
  void upsertPreservesStableBusinessErrorCode() {
    ITiangongProjectUserSyncService service = mock(ITiangongProjectUserSyncService.class);
    when(service.upsert(any())).thenThrow(new TiangongProjectSyncException("TG_CREATOR_CONFLICT", "creator conflict"));

    ResultVO<?> result = new TiangongProjectUserSyncController(service).upsert(new TiangongProjectUserUpsertRequest());

    assertEquals("TG_CREATOR_CONFLICT", result.getResultCode());
    assertEquals("creator conflict", result.getResultMsg());
  }

  @Test
  void removePreservesStableBusinessErrorCode() {
    ITiangongProjectUserSyncService service = mock(ITiangongProjectUserSyncService.class);
    when(service.remove(any())).thenThrow(new TiangongProjectSyncException("TG_CREATOR_REMOVE_FORBIDDEN", "creator"));

    ResultVO<?> result = new TiangongProjectUserSyncController(service).remove(new TiangongProjectUserRemoveRequest());

    assertEquals("TG_CREATOR_REMOVE_FORBIDDEN", result.getResultCode());
    assertEquals("creator", result.getResultMsg());
  }
}
