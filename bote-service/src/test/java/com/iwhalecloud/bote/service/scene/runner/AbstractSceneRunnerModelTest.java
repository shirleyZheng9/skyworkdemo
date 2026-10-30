package com.iwhalecloud.bote.service.scene.runner;

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
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.cmsr.tiangong.base.exception.BssException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 智能体对话时大模型校验测试：绑定的大模型已删除时不允许继续对话
 *
 * @author zhengxueli
 * @since 2026-10-30
 */
class AbstractSceneRunnerModelTest {
  private static final Long TENANT_ID = 100L;
  private static final Long MODEL_ID = 200L;
  private static final Long DEFAULT_MODEL_ID = 300L;

  private LargeModelManageMapper largeModelManageMapper;
  private TenantSettingInfoCache tenantSettingInfoCache;
  private TestSceneRunner runner;

  @BeforeEach
  void setUp() {
    largeModelManageMapper = mock(LargeModelManageMapper.class);
    tenantSettingInfoCache = mock(TenantSettingInfoCache.class);
    when(tenantSettingInfoCache.getModelId(TENANT_ID)).thenReturn(DEFAULT_MODEL_ID);
    runner = new TestSceneRunner(largeModelManageMapper, tenantSettingInfoCache);
  }

  /**
   * 测试用执行器：通过子类方法调用 protected 的 resolveModelId，不依赖与被测类同包
   */
  private static final class TestSceneRunner extends AbstractSceneRunner {
    TestSceneRunner(LargeModelManageMapper largeModelManageMapper, TenantSettingInfoCache tenantSettingInfoCache) {
      this.largeModelManageMapper = largeModelManageMapper;
      this.tenantSettingInfoCache = tenantSettingInfoCache;
    }

    Long resolve(SceneChatParamsDTO sceneChatParams) {
      return resolveModelId(sceneChatParams);
    }

    @Override
    public OrchestrationEngineResponse run(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams) {
      return null;
    }
  }

  @Test
  void deletedModel_isRejected() {
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(null);

    BssException error = assertThrows(BssException.class, () -> runner.resolve(params(MODEL_ID)));

    assertEquals(ModelConsts.MODEL_DELETED_MSG, error.getMessage());
  }

  @Test
  void enabledModel_isAllowed() {
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(model(BaseConsts.TRUE));

    assertEquals(MODEL_ID, runner.resolve(params(MODEL_ID)));
  }

  @Test
  void disabledModel_isAllowed() {
    // 停用只表示不是 AI 助手的默认模型，不影响智能体使用
    when(largeModelManageMapper.getLargeModel(TENANT_ID, MODEL_ID)).thenReturn(model(BaseConsts.FALSE));

    assertEquals(MODEL_ID, runner.resolve(params(MODEL_ID)));
  }

  @Test
  void defaultModel_usesTenantModelWithoutCheck() {
    assertEquals(DEFAULT_MODEL_ID, runner.resolve(params(ModelConsts.DEFAULT_MODEL)));
    assertEquals(DEFAULT_MODEL_ID, runner.resolve(params(null)));

    verify(largeModelManageMapper, never()).getLargeModel(anyLong(), anyLong());
  }

  private static SceneChatParamsDTO params(Long modelId) {
    SceneChatParamsDTO params = new SceneChatParamsDTO();
    params.setTenantId(TENANT_ID);
    params.setModelId(modelId);
    return params;
  }

  private static LargeModelDTO model(String isEnabled) {
    LargeModelDTO model = new LargeModelDTO();
    model.setModelId(MODEL_ID);
    model.setIsEnabled(isEnabled);
    return model;
  }
}
