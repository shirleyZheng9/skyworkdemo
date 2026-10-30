package com.iwhalecloud.bote.controller.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.service.model.ILargeModelManageService;
import com.iwhalecloud.bote.service.model.IModelTestService;
import com.iwhalecloud.bote.service.workspace.helper.WorkspaceHelper;
import com.cmsr.tiangong.base.vo.ResultVO;
import com.cmsr.tiangong.cache.refresh.IRefreshCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 删除大模型后刷新模型客户端缓存测试
 *
 * @author zhengxueli
 * @since 2026-10-30
 */
class LargeModelManageControllerDeleteTest {
  private static final Long TENANT_ID = 100L;
  private static final Long MODEL_ID = 200L;

  private ILargeModelManageService modelManageService;
  private IRefreshCacheService refreshCacheService;
  private LargeModelManageController controller;

  @BeforeEach
  void setUp() {
    modelManageService = mock(ILargeModelManageService.class);
    refreshCacheService = mock(IRefreshCacheService.class);
    controller = new LargeModelManageController(modelManageService, mock(IModelTestService.class), refreshCacheService,
      mock(WorkspaceHelper.class), mock(TenantSettingInfoCache.class));
  }

  @Test
  void deleteSuccess_refreshesModelClientCache() {
    when(modelManageService.deleteLargeModel(TENANT_ID, MODEL_ID)).thenReturn(ResultVO.success());

    controller.deleteLargeModel(MODEL_ID, TENANT_ID, null);

    verify(refreshCacheService).refresh(CacheConsts.CACHE_NAME_MODEL_CLIENT, TENANT_ID + CacheConsts.COLON + MODEL_ID);
  }

  @Test
  void deleteFailed_doesNotRefreshCache() {
    when(modelManageService.deleteLargeModel(TENANT_ID, MODEL_ID)).thenReturn(ResultVO.fail("大模型已存在关联配置数据，不允许删除"));

    controller.deleteLargeModel(MODEL_ID, TENANT_ID, null);

    verify(refreshCacheService, never()).refresh(anyString(), any(String[].class));
  }

  @Test
  void deletePersonalSuccess_refreshesModelClientCache() {
    when(modelManageService.deleteLargeModel(TENANT_ID, MODEL_ID)).thenReturn(ResultVO.success());

    controller.deletePersonalLargeModel(MODEL_ID, TENANT_ID);

    verify(refreshCacheService).refresh(CacheConsts.CACHE_NAME_MODEL_CLIENT, TENANT_ID + CacheConsts.COLON + MODEL_ID);
  }

  @Test
  void deletePersonalFailed_doesNotRefreshCache() {
    when(modelManageService.deleteLargeModel(TENANT_ID, MODEL_ID)).thenReturn(ResultVO.fail("大模型已存在关联配置数据，不允许删除"));

    controller.deletePersonalLargeModel(MODEL_ID, TENANT_ID);

    verify(refreshCacheService, never()).refresh(anyString(), any(String[].class));
  }
}
