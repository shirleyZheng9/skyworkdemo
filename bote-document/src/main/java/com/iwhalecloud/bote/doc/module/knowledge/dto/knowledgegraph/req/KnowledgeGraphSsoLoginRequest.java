package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST /api/auth/sso/login 请求体（仅必填 ticket）
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphSsoLoginRequest {

  private String ticket;
}
