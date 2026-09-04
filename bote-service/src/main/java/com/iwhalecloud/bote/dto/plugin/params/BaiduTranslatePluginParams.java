package com.iwhalecloud.bote.dto.plugin.params;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百度翻译插件参数
 *
 * @author zyt
 * @since 2025-07-18
 */
@Getter
@Setter
@ToString
public class BaiduTranslatePluginParams {
    /** 待翻译文本 */
    private String inputText;
    /** 源语言，支持auto自动检测 */
    private String from;
    /** 目标语言，不支持auto */
    private String to;
    /** APP ID */
    private String appId;
    /** 密钥 */
    private String appKey;
}
