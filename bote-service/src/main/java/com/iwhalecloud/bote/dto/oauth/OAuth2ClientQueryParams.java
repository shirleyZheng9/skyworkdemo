package com.iwhalecloud.bote.dto.oauth;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2客户端查询参数
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "OAuth2客户端查询参数")
public class OAuth2ClientQueryParams extends PagingQueryParams {

    @Schema(description = "客户端标识")
    private String clientCode;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "是否启用")
    private String enabled;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "搜索内容（客户端标识或名称）")
    private String searchContent;
}
