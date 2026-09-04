package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CompleteExptOptionFn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunCheckOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.GetExptTupleOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.InvokeExptReq;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExptManagerImpl implements IExptManager {

  private final ExptConfigManagerImpl exptConfigManager;
  private final ExptManagerExecutionImpl exptManagerExecution;

  @Override
  public Boolean checkName(String name, Long spaceId, Session session) {
    return exptConfigManager.checkName(name, spaceId, session);
  }

  @Override
  public Experiment createExpt(CreateExptParam req, Session session) {
    return exptConfigManager.createExpt(req, session);
  }

  @Override
  public void update(Experiment expt, Session session) {
    exptConfigManager.update(expt, session);
  }

  @Override
  public void delete(Long exptId, Long spaceId, Session session) {
    exptConfigManager.delete(exptId, spaceId, session);
  }

  @Override
  public void mDelete(List<Long> exptIds, Long spaceId, Session session) {
    exptConfigManager.mDelete(exptIds, spaceId, session);
  }

  @Override
  public PageInfo<Experiment> list(ListExptParam param) {
    return exptConfigManager.list(param);
  }

  @Override
  public PageInfo<Experiment> listExptRaw(Integer page, Integer pageSize, Long spaceId, ExptListFilter filter) {
    return exptConfigManager.listExptRaw(page, pageSize, spaceId, filter);
  }

  @Override
  public Experiment getDetail(Long exptId, Long spaceId, Session session, GetExptTupleOption... opts) {
    return exptConfigManager.getDetail(exptId, spaceId, session, opts);
  }

  @Override
  public List<Experiment> mGetDetail(List<Long> exptIds, Long spaceId, Session session) {
    return exptConfigManager.mGetDetail(exptIds, spaceId, session);
  }

  @Override
  public Experiment get(Long exptId, Long spaceId, Session session) {
    return exptConfigManager.get(exptId, spaceId, session);
  }

  @Override
  public List<Experiment> mGet(List<Long> exptIds, Long spaceId) {
    return exptConfigManager.mGet(exptIds, spaceId);
  }

  @Override
  public Experiment clone(Long exptId, Long spaceId) {
    return exptConfigManager.clone(exptId, spaceId);
  }

  @Override
  public void checkRun(Experiment expt, Long spaceId, Session session, ExptRunCheckOption... opts) {
    exptManagerExecution.checkRun(expt, spaceId, session, opts);
  }

  @Override
  public void run(Long exptId, Long runId, Long spaceId, Session session, ExptRunMode runMode, Map<String, String> ext) {
    exptManagerExecution.run(exptId, runId, spaceId, session, runMode, ext);
  }

  @Override
  public void retryUnSuccess(Long exptId, Long runId, Long spaceId, Session session, Map<String, String> ext) {
    exptManagerExecution.retryUnSuccess(exptId, runId, spaceId, session, ext);
  }

  @Override
  public void invoke(InvokeExptReq invokeExptReq) {
    exptManagerExecution.invoke(invokeExptReq);
  }

  @Override
  public void finish(Experiment exptId, Long exptRunId, Session session) {
    exptManagerExecution.finish(exptId, exptRunId, session);
  }

  @Override
  public void pendRun(Long exptId, Long exptRunId, Long spaceId, Session session) {
    exptManagerExecution.pendRun(exptId, exptRunId, spaceId, session);
  }

  @Override
  public void pendExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    exptManagerExecution.pendExpt(exptId, spaceId, session, opts);
  }

  @Override
  public void completeRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    exptManagerExecution.completeRun(exptId, exptRunId, mode, spaceId, session, opts);
  }

  @Override
  public void completeExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    exptManagerExecution.completeExpt(exptId, spaceId, session, opts);
  }

  @Override
  public void logRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session) {
    exptManagerExecution.logRun(exptId, exptRunId, mode, spaceId, session);
  }

  @Override
  public ExptRunLog getRunLog(Long exptId, Long exptRunId, Long spaceId, Session session) {
    return exptManagerExecution.getRunLog(exptId, exptRunId, spaceId, session);
  }
}
