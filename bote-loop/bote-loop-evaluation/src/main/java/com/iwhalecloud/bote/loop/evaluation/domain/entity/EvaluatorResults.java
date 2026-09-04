package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器结果实体
 * 迁移对应关系: Go语言EvaluatorResults
 * - 功能: 评估器结果数据结构
 * - 字段: evalVerIDToResID
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorResults结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现Serialize方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go map[int64]int64 -> Java Map<Long, Long>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorResults {
  @JsonProperty("eval_ver_id_to_res_id")
  private Map<Long, Long> evalVerIdToResId;
}
