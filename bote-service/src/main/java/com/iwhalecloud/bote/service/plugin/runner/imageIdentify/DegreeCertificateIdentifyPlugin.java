package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 学位证书识别
 *
 * @author qian.sisheng
 * @since 2025-11-19
 */
@Component
public class DegreeCertificateIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DEGREE_CERTIFICATE;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("name", "姓名", AttrDataType.STRING),
      ParameterSpec.newProperty("degree", "学位", AttrDataType.STRING),
      ParameterSpec.newProperty("major", "专业", AttrDataType.STRING),
      ParameterSpec.newProperty("school", "学校", AttrDataType.STRING),
      ParameterSpec.newProperty("date", "日期", AttrDataType.DATE)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是学位证书相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- name：姓名\n");
    prompt.append("- degree：学位, 使用学士学位、硕士学位、博士学位\n");
    prompt.append("- major：专业\n");
    prompt.append("- school：学校\n");
    prompt.append("- date：日期, 转换成YYYY-MM-DD形式\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据学位证书相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的数组。示例如下：\n");
    prompt.append("""
      ```json
      {"name":"张三","degree":"Master","major":"计算机科学与技术","school":"清华大学","date":"2020-06-30"}```
      """);
    return prompt.toString();
  }
}
