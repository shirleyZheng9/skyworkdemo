package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取Prompt基础信息选项
 * 迁移对应关系: Go语言repo.GetPromptBasicOption
 * - 功能: 定义获取Prompt基础信息的选项
 * - 继承: CacheOption
 * <p>
 * Java实现说明:
 * - 对应Go的repo.GetPromptBasicOption结构体
 * - 继承CacheOption类
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体嵌入 -> Java继承
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class GetPromptBasicOption extends CacheOption {

}
