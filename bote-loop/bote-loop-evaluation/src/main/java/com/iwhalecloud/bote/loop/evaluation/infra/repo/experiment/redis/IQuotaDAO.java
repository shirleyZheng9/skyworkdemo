package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.redis;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;

public interface IQuotaDAO {

  QuotaSpaceExpt getQuotaSpaceExpt(Long spaceId);

  void setQuotaSpaceExpt(Long spaceId, QuotaSpaceExpt qse);

}
