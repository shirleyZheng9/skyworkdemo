package com.iwhalecloud.bote.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 签名相关配置
 *
 * @author zhangJun
 * @since 2021/10/22
 **/
@Data
@Component
@ConfigurationProperties("bote.security.sign")
public class SignProperties {

    private boolean enable;
    /** 秘钥 */
    private String secretKey = "zrT5bi2escXilaH1fs653uZiH9RWfzCS";
    /** 免拦截的接口列表 */
    private List<String> whitelist;
    /** 拦截路径,多个用逗号分隔 */
    private List<String> matchPaths;
}
