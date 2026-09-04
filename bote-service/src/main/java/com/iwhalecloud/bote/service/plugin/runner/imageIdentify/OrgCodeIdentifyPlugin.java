package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 组织机构代码证识别插件
 *
 * @author qian.sisheng
 * @since 2025-11-22
 */
@Component
public class OrgCodeIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_ORG_CODE;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("code", "代码", AttrDataType.STRING),
      ParameterSpec.newProperty("name", "机构名称", AttrDataType.STRING),
      ParameterSpec.newProperty("type", "机构类型", AttrDataType.STRING),
      ParameterSpec.newProperty("legalPerson", "法定代表人", AttrDataType.STRING),
      ParameterSpec.newProperty("address", "地址", AttrDataType.STRING),
      ParameterSpec.newProperty("validity", "有效期", AttrDataType.STRING),
      ParameterSpec.newProperty("startDate", "开始日期", AttrDataType.DATE),
      ParameterSpec.newProperty("endDate", "结束日期", AttrDataType.DATE),
      ParameterSpec.newProperty("issue", "颁发单位", AttrDataType.STRING),
      ParameterSpec.newProperty("registerNumber", "登记号", AttrDataType.STRING)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是组织机构代码证相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- code：代码\n");
    prompt.append("- name：机构名称\n");
    prompt.append("- type：机构类型\n");
    prompt.append("- legalPerson：法定代表人\n");
    prompt.append("- address：地址\n");
    prompt.append("- validity：有效期\n");
    prompt.append("- startDate：开始日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- endDate：结束日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- issue：颁发单位\n");
    prompt.append("- registerNumber：登记号\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据组织机构代码证相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
        ```json
        {"code":"12345678-9","name":"某某科技有限公司","address":"广州番禺","type":"企业法人", "legalPerson": "张三",
        "validity":"自2020年11月22日至2024年11月22日", "startDate": "2020-11-22", "endDate": "2024-11-22",
        "issue":"北京市市场监督管理局","registerNumber":"911101051234567890"}
        ```""");
    return prompt.toString();
  }
}
