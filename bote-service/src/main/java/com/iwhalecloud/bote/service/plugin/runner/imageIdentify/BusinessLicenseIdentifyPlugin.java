package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 营业执照识别
 *
 * @author qian.sisheng
 * @since 2025-11-19
 */
@Component
public class BusinessLicenseIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BUSINESS_LICENSE;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("creditCode", "社会信用代码/注册号", AttrDataType.STRING),
      ParameterSpec.newProperty("company", "单位名称", AttrDataType.STRING),
      ParameterSpec.newProperty("legalPerson", "法定代表人", AttrDataType.STRING),
      ParameterSpec.newProperty("registeredCapital", "注册资本", AttrDataType.STRING),
      ParameterSpec.newProperty("validDate", "营业期限", AttrDataType.STRING),
      ParameterSpec.newProperty("establishDate", "成立时间", AttrDataType.STRING),
      ParameterSpec.newProperty("businessScope", "经营范围", AttrDataType.STRING),
      ParameterSpec.newProperty("registeredAddress", "注册地址", AttrDataType.STRING),
      ParameterSpec.newProperty("companyType", "公司类型", AttrDataType.STRING),
      ParameterSpec.newProperty("type", "类型", AttrDataType.STRING)
    ));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是营业执照相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- creditCode：社会信用代码/注册号\n");
    prompt.append("- company：单位名称\n");
    prompt.append("- legalPerson：法定代表人\n");
    prompt.append("- registeredCapital：注册资本\n");
    prompt.append("- validDate：营业期限\n");
    prompt.append("- establishDate：成立时间\n");
    prompt.append("- businessScope：经营范围\n");
    prompt.append("- registeredAddress：注册地址\n");
    prompt.append("- companyType：公司类型，如有限责任公司\n");
    prompt.append("- type：类型，如个体商户等\n");
    prompt.append("3、如果字段信息数据为空时，请将数据值设置为空字符串。\n");
    prompt.append("4、文字必须完整，不能漏字。");
    prompt.append("5、根据营业执照相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("6、请将上述信息按照json格式返回，并确保返回的json格式正确，请勿返回其他内容。示例如下：");
    prompt.append("""
      ```json
       {"creditCode":"91310115MA1K3QJL7Y","company":"上海某某科技有限公司","legalPerson":"张三",\
      "registeredCapital":"人民币壹仟万元整","validDate":"2020-01-01至长期","establishDate":"2020-01-01",\
      "businessScope":"计算机软件技术开发、技术转让、技术咨询、技术服务，计算机系统集成，网络工程，电子商务（不得从事金融业务）。"\
      ,"registeredAddress":"上海市浦东新区某某路1234号","companyType":"有限责任公司","type":"个体商户"}```
      """);
    return prompt.toString();
  }
}
