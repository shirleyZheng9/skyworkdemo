package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织检索请求对象
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondOrgTreeRequest {
  /** 父组织标识，-1表示查询顶层组织 */
  private Long parentOrgId;
  /** 组织名称，用于模糊检索 */
  private String orgName;
}
