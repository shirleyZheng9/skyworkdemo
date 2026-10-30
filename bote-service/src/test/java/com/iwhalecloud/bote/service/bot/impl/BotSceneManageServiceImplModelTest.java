package com.iwhalecloud.bote.service.bot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.cmsr.tiangong.base.exception.BssException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 保存智能体时大模型校验测试：不允许绑定已删除的大模型
 *
 * @author zhengxueli
 * @since 2026-10-30
 */
class BotSceneManageServiceImplModelTest {
  private static final Long TENANT_ID = 100L;
  private static final Long MODEL_ID = 200L;
  private static final Long DEFAULT_MODEL_ID = 300L;

  private LargeModelManageMapper largeModelManageMapper;
  private TenantSettingInfoCache tenantSettingInfoCache;
  private BotSceneManageServiceImpl service;

  @BeforeEach
  void setUp() {
    largeModelManageMapper = mock(LargeModelManageMapper.class);
    tenantSettingInfoCache = mock(TenantSettingInfoCache.class);
    // 构造器依赖较多，这里跳过构造器，只注入 validateModel 用到的依赖
    service = new ObjenesisStd().newInstance(BotSceneManageServiceImpl.class);
    ReflectionTestUtils.setField(service, "largeModelManageMapper", largeModelManageMapper);
    ReflectionTestUtils.setField(service, "tenantSettingInfoCache", tenantSettingInfoCache);
  }

  @Test
  void deletedModel_isRejected() {
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(null);

    BssException error = assertThrows(BssException.class, () -> validateModel(scene(MODEL_ID)));

    assertEquals(ModelConsts.MODEL_DELETED_MSG, error.getMessage());
  }

  @Test
  void enabledModel_isAllowed() {
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(model(BaseConsts.TRUE));

    validateModel(scene(MODEL_ID));
  }

  @Test
  void disabledModel_isAllowed() {
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(model(BaseConsts.FALSE));

    validateModel(scene(MODEL_ID));
  }

  @Test
  void noModel_isSkipped() {
    validateModel(scene(null));

    verify(largeModelManageMapper, never()).getLargeModel(anyLong(), anyLong());
  }

  @Test
  void defaultModelMissing_isRejectedWithOriginalMessage() {
    when(tenantSettingInfoCache.getModelIdOrNull(TENANT_ID)).thenReturn(DEFAULT_MODEL_ID);
    when(largeModelManageMapper.getLargeModel(TENANT_ID, DEFAULT_MODEL_ID)).thenReturn(null);

    BssException error = assertThrows(BssException.class, () -> validateModel(scene(ModelConsts.DEFAULT_MODEL)));

    assertEquals("大模型不存在，请切换大模型", error.getMessage());
  }

  @Test
  void defaultModelNotConfigured_isSkipped() {
    when(tenantSettingInfoCache.getModelIdOrNull(TENANT_ID)).thenReturn(null);

    validateModel(scene(ModelConsts.DEFAULT_MODEL));

    verify(largeModelManageMapper, never()).getLargeModel(anyLong(), anyLong());
  }

  private void validateModel(BotSceneDTO scene) {
    ReflectionTestUtils.invokeMethod(service, "validateModel", scene);
  }

  private static BotSceneDTO scene(Long modelId) {
    BotSceneDTO scene = new BotSceneDTO();
    scene.setTenantId(TENANT_ID);
    scene.setModelId(modelId);
    return scene;
  }

  private static LargeModelDTO model(String isEnabled) {
    LargeModelDTO model = new LargeModelDTO();
    model.setModelId(MODEL_ID);
    model.setIsEnabled(isEnabled);
    return model;
  }
}
