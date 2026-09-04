package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link DataSyncCodeEnum} 单元测试
 *
 * <p>覆盖静态查找方法 getReverseCodes/getTableCode/getName/getCodeEnum 及
 * ignoreForCopy/ignoreForSceneCopy 的正常与未命中分支。纯逻辑，无需 Mock。</p>
 */
class DataSyncCodeEnumTest {

  @Test
  void getReverseCodes_returnsCodesInReverseOrder() {
    List<String> codes = DataSyncCodeEnum.getReverseCodes();

    assertThat(codes).isNotEmpty();
    // 末尾常量 JOB 的 code 位于倒序列表首位
    assertThat(codes.get(0)).isEqualTo(DataSyncCodeEnum.JOB.getCode());
    // 首个常量 BOT 的 code 位于倒序列表末尾
    assertThat(codes.get(codes.size() - 1)).isEqualTo(DataSyncCodeEnum.BOT.getCode());
    assertThat(codes).hasSize(DataSyncCodeEnum.values().length);
  }

  @Test
  void getTableCode_knownCode_returnsLowercasedTableCode() {
    assertThat(DataSyncCodeEnum.getTableCode("bot_base")).isEqualTo("bt_bot");
    assertThat(DataSyncCodeEnum.getTableCode("catalog")).isEqualTo("bt_catalog");
  }

  @Test
  void getTableCode_emptyTableCode_returnsEmptyString() {
    // JOB 的 tableCode 为空字符串
    assertThat(DataSyncCodeEnum.getTableCode("job")).isEmpty();
  }

  @Test
  void getTableCode_unknownCode_returnsNull() {
    assertThat(DataSyncCodeEnum.getTableCode("no-such-code")).isNull();
  }

  @Test
  void getName_knownCode_returnsName() {
    assertThat(DataSyncCodeEnum.getName("bot_base")).isEqualTo("智能应用");
    assertThat(DataSyncCodeEnum.getName("knowledge_base")).isEqualTo("知识库");
  }

  @Test
  void getName_unknownCode_returnsNull() {
    assertThat(DataSyncCodeEnum.getName("no-such-code")).isNull();
  }

  @Test
  void getCodeEnum_knownCode_returnsEnum() {
    assertThat(DataSyncCodeEnum.getCodeEnum("bot_base")).isSameAs(DataSyncCodeEnum.BOT);
    assertThat(DataSyncCodeEnum.getCodeEnum("a2a_agent")).isSameAs(DataSyncCodeEnum.A2A_AGENT);
  }

  @Test
  void getCodeEnum_unknownCode_returnsNull() {
    assertThat(DataSyncCodeEnum.getCodeEnum("no-such-code")).isNull();
  }

  @Test
  void ignoreForCopy_containsTenantRelatedCodes() {
    assertThat(DataSyncCodeEnum.ignoreForCopy())
        .containsExactly(
            DataSyncCodeEnum.TENANT.getCode(),
            DataSyncCodeEnum.TENANT_SETTING_INFO.getCode(),
            DataSyncCodeEnum.API_AUTH.getCode(),
            DataSyncCodeEnum.APP_PUBLISH.getCode());
  }

  @Test
  void ignoreForSceneCopy_containsKnowledgeAndLibrary() {
    assertThat(DataSyncCodeEnum.ignoreForSceneCopy())
        .containsExactly(DataSyncCodeEnum.KNOWLEDGE.getCode(), DataSyncCodeEnum.LIBRARY.getCode());
  }

  @Test
  void getters_returnExpectedValues() {
    DataSyncCodeEnum bot = DataSyncCodeEnum.BOT;
    assertThat(bot.getCode()).isEqualTo("bot_base");
    assertThat(bot.getTableCode()).isEqualTo("bt_bot");
    assertThat(bot.getName()).isEqualTo("智能应用");
  }
}
