package com.iwhalecloud.bote.common.thread;

import org.springframework.core.task.TaskDecorator;

/**
 * 博特任务装饰器
 *
 * <p>由 bote-service 模块实现，用于传递线程本地变量</p>
 *
 * @author bianjp
 * @since 2025-05-22
 */
public interface BoteTaskDecorator extends TaskDecorator {
}
