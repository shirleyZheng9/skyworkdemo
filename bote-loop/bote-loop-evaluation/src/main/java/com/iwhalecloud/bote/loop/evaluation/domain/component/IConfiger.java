package com.iwhalecloud.bote.loop.evaluation.domain.component;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BmqProducerCfg;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CKDBConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptConsumerConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptErrCtrl;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RetryConf;

/**
 * 配置器接口
 * 对应Go版本的IConfiger接口
 */
public interface IConfiger {

  /**
   * 获取实验消费者配置
   *
   * @return 实验消费者配置
   */
  ExptConsumerConf getConsumerConf();

  /**
   * 获取实验错误控制配置
   *
   * @return 实验错误控制配置
   */
  ExptErrCtrl getErrCtrl();

  /**
   * 获取实验执行配置
   *
   * @param spaceId 空间ID
   * @return 实验执行配置
   */
  ExptExecConf getExptExecConf(Long spaceId);

  /**
   * 获取错误重试配置
   *
   * @param spaceId 空间ID
   * @param err 错误对象
   * @return 重试配置
   */
  RetryConf getErrRetryConf(Long spaceId, Exception err);

  /**
   * 获取实验轮次结果过滤BMQ生产者配置
   *
   * @return BMQ生产者配置
   */
  BmqProducerCfg getExptTurnResultFilterBmqProducerCfg();

  /**
   * 获取ClickHouse数据库配置
   *
   * @return ClickHouse数据库配置
   */
  CKDBConfig getCKDBName();
}
