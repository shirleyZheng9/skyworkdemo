package com.iwhalecloud.bote.loop.evaluation.domain.component;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BmqProducerCfg;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CKDBConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptConsumerConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptErrCtrl;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RetryConf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Configer implements IConfiger, InitializingBean {
  private final ExptConsumerConf exptConsumerConf;
  private final ExptErrCtrl exptErrCtrl;
  private ExptExecConf defaultExptExecConf;

  @Override
  public ExptConsumerConf getConsumerConf() {
    if (exptConsumerConf == null) {
      return new ExptConsumerConf();
    }
    return exptConsumerConf;
  }

  @Override
  public ExptErrCtrl getErrCtrl() {
    return exptErrCtrl;
  }

  @Override
  public ExptExecConf getExptExecConf(Long spaceId) {
    ExptExecConf exptExecConfBySpaceId = getConsumerConf().getExptExecConfBySpaceId(spaceId);
    return exptExecConfBySpaceId == null ? defaultExptExecConf : exptExecConfBySpaceId;
  }

  @Override
  public RetryConf getErrRetryConf(Long spaceId, Exception err) {
    return getErrCtrl().getErrRetryCtrlBySpaceId(spaceId).getRetryConf(err);
  }

  @Override
  public BmqProducerCfg getExptTurnResultFilterBmqProducerCfg() {
    return null;
  }

  @Override
  public CKDBConfig getCKDBName() {
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    ExptExecConf exptExecConf = new ExptExecConf();
    exptExecConf.setZombieIntervalSecond(60 * 60 * 24);
    exptExecConf.setDaemonIntervalSecond(20);
    exptExecConf.setSpaceExptConcurLimit(200);
    defaultExptExecConf = exptExecConf;
  }
}
