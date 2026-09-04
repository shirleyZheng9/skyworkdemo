package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.QuotaUpdateResult;
import java.util.function.Function;

/**
 * 配额仓库接口
 * 对应Go: QuotaRepo
 */
public interface QuotaRepo {

  /**
   * 创建或更新配额
   * 对应Go: CreateOrUpdate(ctx context.Context, spaceID int64, updater func(*entity.QuotaSpaceExpt) (*entity.QuotaSpaceExpt, bool, error), session *entity.Session) error
   */
  void createOrUpdate(Long spaceId, Function<QuotaSpaceExpt, QuotaUpdateResult> updater, Session session);
}
