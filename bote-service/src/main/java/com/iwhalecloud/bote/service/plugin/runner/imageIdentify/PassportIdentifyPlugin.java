package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 国内护照识别
 *
 * @author qian.sisheng
 * @since 2025-11-19
 */
@Component
public class PassportIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_PASSPORT;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("type", "护照类型", AttrDataType.STRING),
      ParameterSpec.newProperty("countryCode", "国家码", AttrDataType.STRING),
      ParameterSpec.newProperty("passportNo", "护照号码", AttrDataType.STRING),
      ParameterSpec.newProperty("name", "中文名", AttrDataType.STRING),
      ParameterSpec.newProperty("englishName", "英文名", AttrDataType.STRING),
      ParameterSpec.newProperty("sex", "性别", AttrDataType.STRING),
      ParameterSpec.newProperty("nationality", "国籍", AttrDataType.STRING),
      ParameterSpec.newProperty("birthday", "出生日期", AttrDataType.DATE),
      ParameterSpec.newProperty("birthPlace", "出生地", AttrDataType.STRING),
      ParameterSpec.newProperty("issueDate", "签发日期", AttrDataType.DATE),
      ParameterSpec.newProperty("authorityPlace", "签发地", AttrDataType.STRING),
      ParameterSpec.newProperty("expiryDate", "到期日期", AttrDataType.DATE),
      ParameterSpec.newProperty("authority", "签发机关", AttrDataType.STRING),
      ParameterSpec.newProperty("signature", "持照人签名", AttrDataType.STRING),
      ParameterSpec.newProperty("mrz", "机读码（MRZ码）", AttrDataType.STRING)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是护照的相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- type：护照类型，有P、W、G\n");
    prompt.append("- countryCode：国家码\n");
    prompt.append("- passportNo：护照号码\n");
    prompt.append("- name：中文名\n");
    prompt.append("- englishName：英文名, 可以是中文拼音\n");
    prompt.append("- sex：性别\n");
    prompt.append("- nationality：国籍\n");
    prompt.append("- birthday：出生日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- birthPlace：出生地\n");
    prompt.append("- issueDate：签发日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- authorityPlace：签发地\n");
    prompt.append("- expiryDate：到期日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- authority：签发机关\n");
    prompt.append("- signature：持照人签名\n");
    prompt.append("- mrz：机读码（MRZ码）\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据护照的相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
      {"type":"P","countryCode":"CHN","passportNo":"G12345678",\
      "name":"张三","englishName":"Zhang San","sex":"男","nationality":"中国","birthday":"1990-01-01",\
      "birthPlace":"北京市","issueDate":"2020-01-01","authorityPlace":"北京市公安局出入境管理局",\
      "expiryDate":"2030-01-01","authority":"中华人民共和国国家移民管理局","signature":"张三",\
      "mrz":"P<CHNZHANG<<SAN<<<<<<<<<<<<<<<<<<<<<<<G12345678<CHN900101M20200101<<<<<<<<<<<<<<04"}```
      """);
    return prompt.toString();
  }
}
