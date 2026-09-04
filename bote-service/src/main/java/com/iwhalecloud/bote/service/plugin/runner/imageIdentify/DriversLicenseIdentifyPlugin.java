package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 驾驶证识别
 *
 * @author qian.sisheng
 * @since 2025-11-19
 */
@Component
public class DriversLicenseIdentifyPlugin extends AbstractImageIdentifyPlugin {
  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DRIVERS_LICENSE;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("name", "姓名", AttrDataType.STRING),
      ParameterSpec.newProperty("sex", "性别", AttrDataType.STRING),
      ParameterSpec.newProperty("nation", "国籍", AttrDataType.STRING),
      ParameterSpec.newProperty("address", "地址", AttrDataType.STRING),
      ParameterSpec.newProperty("birthday", "出生日期", AttrDataType.DATE),
      ParameterSpec.newProperty("issueDate", "初次领证日期", AttrDataType.DATE),
      ParameterSpec.newProperty("class", "准驾车型", AttrDataType.STRING),
      ParameterSpec.newProperty("validity", "有效期", AttrDataType.STRING),
      ParameterSpec.newProperty("number", "证号", AttrDataType.STRING)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是驾驶证相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- name：姓名\n");
    prompt.append("- sex：性别\n");
    prompt.append("- nation：国籍\n");
    prompt.append("- address：地址\n");
    prompt.append("- birthday：出生日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- issueDate：初次领证日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- class：准驾车型，如C1、C2等\n");
    prompt.append("- validity：有效期，转换成YYYY-MM-DD形式\n");
    prompt.append("- number：证号\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据驾驶证相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
       {"name":"张三","sex":"男","nation":"中国","address":"上海",\
      "birthday":"1990-01-01","issueDate":"2020-01-01","class":"C1","validity":"2025-01-01","number":"1234567890"}```
      """);
    return prompt.toString();
  }

}
