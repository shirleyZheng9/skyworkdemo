package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.entity.agent.AiEnvVariableEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户级的环境变量 DTO
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AiEnvVariableDTO extends AiEnvVariableEntity {
}
