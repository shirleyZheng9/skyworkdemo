package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 通用护照识别
 *
 * @author qian.sisheng
 * @since 2025-11-20
 */
@Component
public class CommonPassportIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_COMMON_PASSPORT;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("number", "护照号码", AttrDataType.STRING),
      ParameterSpec.newProperty("countryCode", "国家或地区代码", AttrDataType.STRING),
      ParameterSpec.newProperty("surname", "姓，大写英文字母", AttrDataType.STRING),
      ParameterSpec.newProperty("givenName", "名，大写英文字母", AttrDataType.STRING),
      ParameterSpec.newProperty("sex", "M：男性 F：女性", AttrDataType.STRING),
      ParameterSpec.newProperty("birthday", "出生日期", AttrDataType.DATE),
      ParameterSpec.newProperty("expiration", "证件有效期", AttrDataType.DATE),
      ParameterSpec.newProperty("mrz1", "MRZ码", AttrDataType.STRING),
      ParameterSpec.newProperty("mrz2", "MRZ码", AttrDataType.STRING)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是护照相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- number：护照号码\n");
    prompt.append("- countryCode：国家或地区代码\n");
    prompt.append("- surname：姓，大写英文字母\n");
    prompt.append("- givenName：名，大写英文字母\n");
    prompt.append("- sex：M：男性 F：女性\n");
    prompt.append("- birthday：出生日期, 转换成YYYY-MM-DD形式\n");
    prompt.append("- expiration：证件有效期, 转换成YYYY-MM-DD形式\n");
    prompt.append("- mrz1：MRZ码,第一行\n");
    prompt.append("- mrz2：MRZ码，第二行\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据护照相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息按照json格式返回，并确保返回的json格式正确，请勿返回其他内容。示例如下：");
    prompt.append("""
      ```json
      { "number": "789586745", "countryCode": "CHN", "surname": \
      "ZHANG", "givenName": "SAN", "sex": "M", \
      "birthday": "1998-01-01", "expiration": "2008-10-31", \
      "mrz1": "P<GBRUNITED<KINGDOM<FIVE<<JODIE<PIPPA<<<<<<<", "mrz2": "1071857032GBR8501178F1601312<<<<<<<<<<<<<<02"}```
      """);
    return prompt.toString();
  }
}
