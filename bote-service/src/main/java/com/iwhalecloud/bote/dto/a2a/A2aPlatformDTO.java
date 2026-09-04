package com.iwhalecloud.bote.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.a2a.A2aPlatformEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * A2A 平台传输对象
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@Schema(description = "A2A 平台传输对象")
public class A2aPlatformDTO extends A2aPlatformEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "鉴权配置")
  private A2aAuthConfig authConfig;
  @Schema(description = "鉴权扩展服务函数名称")
  private String authExtFuncName;


  /**
   * 解析 JSON 配置属性
   */
  public void parseJsonConfig() {
    if (StringUtils.isNotEmpty(authConfigJson)) {
      this.authConfig = JsonUtil.parseJsonRequired(authConfigJson, A2aAuthConfig.class);
    }
    this.authConfigJson = null;
  }

  /**
   * 保存 JSON 配置属性
   */
  public void saveJsonConfig() {
    if (authConfig != null) {
      authConfig.validate();
      this.authConfigJson = JsonUtil.toJsonString(authConfig);
      Assert.isTrue(this.authConfigJson.getBytes(StandardCharsets.UTF_8).length < 4000, "鉴权请求头内容长度超出限制");
    }
    else {
      this.authConfigJson = null;
    }
  }

}
