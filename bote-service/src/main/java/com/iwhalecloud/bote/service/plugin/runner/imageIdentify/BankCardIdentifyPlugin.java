package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 银行卡识别插件
 *
 * @author qian.sisheng
 * @since 2025-11-18
 */
@Component
public class BankCardIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BANK_CARD;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("bankName", "银行名称", AttrDataType.STRING),
      ParameterSpec.newProperty("bankCardNumber", "银行卡号", AttrDataType.STRING),
      ParameterSpec.newProperty("bankCardType", "银行卡类型", AttrDataType.STRING),
      ParameterSpec.newProperty("bankCardValidity", "银行卡有效期", AttrDataType.STRING)));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是银行卡的相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- bankName：银行名称\n");
    prompt.append("- bankCardNumber：银行卡号，卡号是由13至19位数字组成的唯一编码，不包含字母或符号。请提取完整的卡号信息，"
      + "如遇字符'b'请修正为'6'后再进行提取，确保数据完整性\n");
    prompt.append("- bankCardType：银行卡类型，使用借记卡、信用卡、VISA、Mastercard等\n");
    prompt.append("- bankCardValidity：银行卡有效期，请保持原样式\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据银行卡的相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
      {"bankName":"中国银行","bankCardNumber":"621668600000066","bankCardType":"借记卡","bankCardValidity":"10/2022"}
      ```
      """);
    return prompt.toString();
  }
}
