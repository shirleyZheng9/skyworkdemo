package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;

/**
 * 配额服务接口
 * 对应Go: QuotaService
 */
public interface QuotaService {

  /**
   * 允许实验运行
   * 对应Go: AllowExptRun
   */
  void allowExptRun(Long exptId, Long spaceId, Session session);

  /**
   * 释放实验运行
   * 对应Go: ReleaseExptRun
   */
  void releaseExptRun(Long exptId, Long spaceId, Session session);
}
