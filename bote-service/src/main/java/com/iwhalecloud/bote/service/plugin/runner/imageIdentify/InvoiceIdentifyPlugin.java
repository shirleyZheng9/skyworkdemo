package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 增值税发票识别
 *
 * @author qian.sisheng
 * @since 2025-11-20
 */
@Component
public class InvoiceIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_INVOICE;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("type", "发票类型", AttrDataType.STRING),
      ParameterSpec.newProperty("number", "发票号码", AttrDataType.STRING),
      ParameterSpec.newProperty("code", "发票代码", AttrDataType.STRING),
      ParameterSpec.newProperty("date", "开票日期", AttrDataType.DATE),
      ParameterSpec.newProperty("amount", "发票金额", AttrDataType.NUMBER),
      ParameterSpec.newProperty("taxAmount", "不含税金额", AttrDataType.STRING),
      ParameterSpec.newProperty("tax", "税额", AttrDataType.STRING),
      ParameterSpec.newProperty("buyer", "购买方名称", AttrDataType.STRING),
      ParameterSpec.newProperty("buyerIdNumber", "购买方纳税人识别号", AttrDataType.STRING),
      ParameterSpec.newProperty("seller", "销售方名称", AttrDataType.STRING),
      ParameterSpec.newProperty("sellerIdNumber", "销售方纳税人识别号", AttrDataType.STRING),
      ParameterSpec.newProperty("place", "地点，统一到省级行政区", AttrDataType.STRING),
      ParameterSpec.newProperty("departureAddress", "出发地址", AttrDataType.STRING),
      ParameterSpec.newProperty("arrivalAddress", "到达地址", AttrDataType.STRING),
      ParameterSpec.newProperty("seatClass", "座位等级", AttrDataType.STRING)));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是发票相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("  - type：发票类型，如增值税专用发票”, “增值税专用发票（电子）”, “增值税普通发票”, “增值税电子普通发票”, "
      + "“电子发票（普通发票）”, “电子发票（增值税专用发票）”, “火车票”, “电子发票（铁路电子客票）”, “航空运输电子客票行程单”, "
      + "“电子发票（航空运输电子客票行程单）”, “出租车发票”等\n");
    prompt.append("  - number：发票号码, 如电子客票号码\n");
    prompt.append("  - code：发票代码\n");
    prompt.append("  - date：开票日期, 转换成YYYY-MM-DD形式\n");
    prompt.append("  - amount：发票金额,统一保留两位小数\n");
    prompt.append("  - taxAmount：不含税金额,统一保留两位小数\n");
    prompt.append("  - tax：税额, 统一保留两位小数\n");
    prompt.append("  - buyer：购买方名称\n");
    prompt.append("  - buyerIdNumber：购买方纳税人识别号\n");
    prompt.append("  - seller：销售方名称\n");
    prompt.append("  - sellerIdNumber：销售方纳税人识别号\n");
    prompt.append("  - place：地点，统一到省级行政区，车票、机票等不需要\n");
    prompt.append("  - departureAddress：出发地址，车票、机票等才需要提取，否则为空\n");
    prompt.append("  - arrivalAddress：到达地址，车票、机票等才需要提取，否则为空\n");
    prompt.append("  - seatClass：座位等级，车票、机票等才需要提取，否则为空\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据发票相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
      {"type":"增值税普通发票","number":"12345678","code":"87654321901234567890",\
      "date":"2025-11-20","amount":"500.00","taxAmount":"442.48","tax":"57.52","buyer":"北京科技有限公司",\
      "buyerIdNumber":"911101087654321098","seller":"上海信息技术有限公司","sellerIdNumber":"913101121234567890",\
      "place":"上海市","departureAddress":"上海市浦东新区陆家嘴金融中心","arrivalAddress":"上海市徐汇区漕河泾开发区",\
      "seatClass":"二等座"}```
      """);
    return prompt.toString();
  }

}
