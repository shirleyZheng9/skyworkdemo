package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 支付凭证识别
 *
 * @author qian.sisheng
 * @since 2025-11-20
 */
@Component
public class PaymentVoucherIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_PAYMENT_VOUCHER;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("payee", "收款方", AttrDataType.STRING),
      ParameterSpec.newProperty("amount", "支付金额", AttrDataType.NUMBER),
      ParameterSpec.newProperty("payMethod", "支付方式", AttrDataType.STRING),
      ParameterSpec.newProperty("payTime", "支付时间", AttrDataType.DATETIME),
      ParameterSpec.newProperty("tradeNo", "交易单号", AttrDataType.STRING),
      ParameterSpec.newProperty("orderNo", "订单编号", AttrDataType.STRING),
      ParameterSpec.newProperty("merchantNo", "商户单号", AttrDataType.STRING),
      ParameterSpec.newProperty("receiverAddress", "收货地址", AttrDataType.STRING),
      ParameterSpec.newProperty("cardNo", "卡号", AttrDataType.STRING)));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是支付凭证相关信息：\n").append("```text\n").append(result).append("```\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("  - payee：收款方，接受款项的个人、商家或机构名称\n");
    prompt.append("  - amount：支付金额，请使用正数，即使原文显示为负数\n");
    prompt.append("  - payMethod：支付方式，支付所使用的渠道，如招商银行储蓄卡、工商银行信用卡、零钱等\n");
    prompt.append("  - payTime：支付时间，必须转换成 yyyy-MM-dd HH:mm:ss形式\n");
    prompt.append("  - orderNo：订单号，如“订单号”、“订单编号”、“订单-”等\n");
    prompt.append("  - tradeNo：账单单号，如”交易单号“、”转账单号“、”交易参考号“、”参考号“\n");
    prompt.append("  - merchantNo：商户单号，通常以P或M开头的编号，如商户单号、商户订单号\n");
    prompt.append("  - receiverAddress：收货地址，商品配送的具体地点，或者购买方的地址\n");
    prompt.append("  - cardNo：卡号，用于支付的银行卡、信用卡或者其他支付卡的号码，只有完整卡号才提取\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、订单编号、交易单号、merchantNo是不同的编号，不要混淆。");
    prompt.append("5、文字必须完整，不能漏字。");
    prompt.append("6、根据支付凭证相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("7、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
      {"payee":"北京科技有限公司","amount":"500.00","payMethod":"支付宝",\
      "payTime":"2025-11-20 14:30:00","tradeNo":"20251120143000001","orderNo":"201211302043334123",\
      "merchantNo":"P203332131233333322","receiverAddress":"北京市朝阳区xxx路xx号","cardNo":""}```
      """);
    return prompt.toString();
  }
}
