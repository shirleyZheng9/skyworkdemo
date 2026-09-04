package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 身份证识别插件
 *
 * @author qian.sisheng
 * @since 2025-11-17
 */
@Component
public class IDCardIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_ID_CARD_IDENTIFY;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    ParameterSpec emblemSide = ParameterSpec.newObject("emblemSide", "人像面", Arrays.asList(
      ParameterSpec.newProperty("name", "姓名", AttrDataType.STRING),
      ParameterSpec.newProperty("sex", "性别", AttrDataType.STRING),
      ParameterSpec.newProperty("nation", "民族", AttrDataType.STRING),
      ParameterSpec.newProperty("birth", "出生年月", AttrDataType.DATE),
      ParameterSpec.newProperty("address", "地址", AttrDataType.STRING),
      ParameterSpec.newProperty("id", "身份证号", AttrDataType.STRING)
    ));

    ParameterSpec portraitSide = ParameterSpec.newObject("portraitSide", "人像面", Arrays.asList(
      ParameterSpec.newProperty("validDate", "有效期", AttrDataType.STRING),
      ParameterSpec.newProperty("effectiveDate", "有效期开始时间", AttrDataType.DATE),
      ParameterSpec.newProperty("expireDate", "有效期结束时间", AttrDataType.DATE),
      ParameterSpec.newProperty("effectiveYear", "有效期开始年份", AttrDataType.STRING),
      ParameterSpec.newProperty("effectiveMonth", "有效期开始月份", AttrDataType.STRING),
      ParameterSpec.newProperty("effectiveDay", "有效期开始日期", AttrDataType.STRING),
      ParameterSpec.newProperty("expireYear", "有效期结束年份", AttrDataType.STRING),
      ParameterSpec.newProperty("expireMonth", "有效期结束月份", AttrDataType.STRING),
      ParameterSpec.newProperty("expireDay", "有效期结束日期", AttrDataType.STRING),
      ParameterSpec.newProperty("authority", "签发机关", AttrDataType.STRING)
    ));
    return ParameterSpec.newRoot(Arrays.asList(emblemSide, portraitSide));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是居民身份证的相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- name：姓名\n");
    prompt.append("- sex：性别\n");
    prompt.append("- nation：民族\n");
    prompt.append("- birth：出生年月，转换成YYYY-MM-DD形式\n");
    prompt.append("- address：地址\n");
    prompt.append("- id：身份证号\n");
    prompt.append("- validDate：有效期\n");
    prompt.append("- effectiveDate：有效期开始时间，转换成YYYY-MM-DD形式\n");
    prompt.append("- expireDate：有效期结束时间，转换成YYYY-MM-DD形式\n");
    prompt.append("- effectiveYear：有效期开始年份\n");
    prompt.append("- effectiveMonth：有效期开始月份\n");
    prompt.append("- effectiveDay：有效期开始日期\n");
    prompt.append("- expireYear：有效期结束年份\n");
    prompt.append("- expireMonth：有效期结束月份\n");
    prompt.append("- expireDay：有效期结束日期\n");
    prompt.append("- authority：签发机关\n");
    prompt.append("3、emblemSide(人像面)包含name、sex、nation、birth、address、id，其余内容为portraitSide(国徽面)。\n");
    prompt.append("4、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("5、文字必须完整，不能漏字。");
    prompt.append("6、根据居民身份证的相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("7、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
       {"emblemSide":{"name":"张三","sex":"男","nation":"汉","birth":"1990-01-01",\
      "address":"北京市东城区","id":"110101199001011234"},"portraitSide":{"validDate":"2005-10-09-2006-01-09",\
      "effectiveDate":"2005-10-09","expireDate":"2006-01-09","effectiveYear":"2005","effectiveMonth":"10",\
      "effectiveDay":"09","expireYear":"2006","expireMonth":"01","expireDay":"09",\
      "authority":"北京市公安局西城分局"}} ```
      """);
    return prompt.toString();
  }

}
