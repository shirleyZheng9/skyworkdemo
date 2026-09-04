package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.BaiduTranslatePluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang3.RandomStringUtils;

import com.iwhalecloud.bote.common.util.HttpUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.apache.commons.codec.digest.DigestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 百度翻译插件
 *
 * @author zyt
 * @since 2025-07-18
 */
@Component
public class BaiduTranslatePlugin extends AbstractPlugin<BaiduTranslatePluginParams> {

    public BaiduTranslatePlugin() {
        super(BaiduTranslatePluginParams.class);
    }

    @Override
    public String getPluginCode() {
        return PluginConsts.PLUGIN_CODE_BAIDU_TRANSLATE;
    }

    @Override
    public ParameterSpec createRequestParameter() {
        List<ParameterSpec> children = new ArrayList<>();
        children.add(ParameterSpec.newProperty("inputText", "要翻译的文本", AttrDataType.STRING));
        children.add(ParameterSpec.newProperty("from", "源语言，支持auto自动检测", AttrDataType.STRING));
        children.add(ParameterSpec.newProperty("to", "目标语言，不支持auto", AttrDataType.STRING));
        children.add(ParameterSpec.newProperty("appId", "APP ID", AttrDataType.STRING));
        children.add(ParameterSpec.newProperty("appKey", "密钥", AttrDataType.STRING));
        return ParameterSpec.newRoot(children);
    }


    @Override
    public ParameterSpec createResponseParameter() {
        return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("text", "翻译后的文本", AttrDataType.STRING)));
    }

    @Override
    public void validateParams(BaiduTranslatePluginParams params) {
        Assert.hasText(params.getInputText(), "翻译文本不能为空");
        Assert.hasText(params.getFrom(), "源语言不能为空");
        Assert.hasText(params.getTo(), "目标语言不能为空");
        Assert.hasText(params.getAppId(), "appId不能为空");
        Assert.hasText(params.getAppKey(), "密钥不能为空");
    }

    @Override
    @SuppressFBWarnings(value = "WEAK_MESSAGE_DIGEST_MD5", justification = "百度翻译API签名规范要求使用MD5，仅用于第三方接口签名，非安全用途")
    public Object doRun(BaiduTranslatePluginParams params) {
        String q = params.getInputText();
        // q去掉所有换行符
        q = q.replaceAll("\\r\\n|\\r|\\n", "");
        String salt = RandomStringUtils.secure().nextNumeric(5);
        String sign = DigestUtils.md5Hex(params.getAppId() + q + salt + params.getAppKey());
        String url = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("q", q);
        paramMap.add("from", params.getFrom());
        paramMap.add("to", params.getTo());
        paramMap.add("appid", params.getAppId());
        paramMap.add("salt", salt);
        paramMap.add("sign", sign);
        try {
            BaiduTranslateResponse response = HttpUtil.get(url, paramMap, new ParameterizedTypeReference<BaiduTranslateResponse>() { });
            if (response == null) {
                throw new BssException("百度翻译API错误：响应为空");
            }
            if (response.getErrorCode() != null) {
                throw new BssException("百度翻译API错误：" + response.getErrorCode() + " - " + response.getErrorMsg());
            }
            return extractTranslation(response);
        } catch (Exception e) {
            throw new BssException("百度翻译插件执行异常: " + e.getMessage(), e);
        }
    }

    /**
     * 提取翻译结果
     */
    private Map<String, Object> extractTranslation(BaiduTranslateResponse response) {
        if (response.getTransResult() != null && !response.getTransResult().isEmpty()) {
            TransResult first = response.getTransResult().get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("text", first.getDst() != null ? first.getDst() : "");
            return result;
        }
        throw new BssException("百度翻译API返回格式异常: " + response);
    }

    /**
     * 百度翻译API响应DTO，内部静态类
     */
    @Setter
    @Getter
    private static final class BaiduTranslateResponse {
        /** 翻译结果 */
        @JsonProperty("trans_result")
        private List<TransResult> transResult;
        /** 错误码 */
        @JsonProperty("error_code")
        private String errorCode;
        /** 错误信息 */
        @JsonProperty("error_msg")
        private String errorMsg;
    }

    /**
     * 翻译结果
     */
    @Setter
    @Getter
    private static final class TransResult {
        /** 源语言 */
        private String src;
        /** 目标语言 */
        private String dst;
    }
}
