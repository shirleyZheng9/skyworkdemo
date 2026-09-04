package com.iwhalecloud.bote.dto.knowledge.access.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识中台令牌信息DTO
 * @author bote
 */
@Data
@Schema(description = "知识中台令牌信息")
public class KnowledgeTokenDTO {

    @Schema(description = "accessToken名称")
    private String tokenName;

    @Schema(description = "accessToken值")
    private String token;

    @Schema(description = "accessToken剩余有效期（单位: 秒）")
    private Long tokenTimeout;

    @Schema(description = "refreshToken值")
    private String refreshToken;

    @Schema(description = "refreshToken剩余有效期（单位: 秒）")
    private Long refreshTokenTimeout;
}
