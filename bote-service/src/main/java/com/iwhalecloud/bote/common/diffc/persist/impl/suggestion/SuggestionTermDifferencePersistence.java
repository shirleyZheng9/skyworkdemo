package com.iwhalecloud.bote.common.diffc.persist.impl.suggestion;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.entity.suggestion.SuggestionTermEntity;
import com.iwhalecloud.bote.mapper.suggestion.SuggestionTermManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：联想术语
 *
 * @author lizuyin
 * @since 2025-07-30
 */
@Component
public final class SuggestionTermDifferencePersistence extends BaseRootPersistence<SuggestionTermEntity> {

  public SuggestionTermDifferencePersistence(SuggestionTermManageMapper suggestionTermManageMapper) {
    setAddConsumer(suggestionTermManageMapper::insertSuggestionTerm);
    setModifyConsumer(suggestionTermManageMapper::updateSuggestionTerm);
  }
}
