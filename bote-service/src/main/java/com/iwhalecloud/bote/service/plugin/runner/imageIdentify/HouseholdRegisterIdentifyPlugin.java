package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * 户口本识别
 *
 * @author qian.sisheng
 * @since 2025-11-18
 */
@Component
public class HouseholdRegisterIdentifyPlugin extends AbstractImageIdentifyPlugin {

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_HOUSEHOLD_REGISTER;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    ParameterSpec user = ParameterSpec.newObject("user", "常住人口登记卡信息",
      Arrays.asList(ParameterSpec.newProperty("name", "姓名", AttrDataType.STRING),
        ParameterSpec.newProperty("usedName", "曾用名", AttrDataType.STRING),
        ParameterSpec.newProperty("relationship", "户主或与户主关系", AttrDataType.STRING),
        ParameterSpec.newProperty("sex", "性别", AttrDataType.STRING),
        ParameterSpec.newProperty("birthPlace", "出生地", AttrDataType.STRING),
        ParameterSpec.newProperty("nation", "民族", AttrDataType.STRING),
        ParameterSpec.newProperty("nativePlace", "籍贯", AttrDataType.STRING),
        ParameterSpec.newProperty("birthday", "出生日期", AttrDataType.DATE),
        ParameterSpec.newProperty("address", "本市(县)其他住址", AttrDataType.STRING),
        ParameterSpec.newProperty("religion", "宗教信仰", AttrDataType.STRING),
        ParameterSpec.newProperty("idCardNo", "公民身份证件编号", AttrDataType.STRING),
        ParameterSpec.newProperty("height", "身高", AttrDataType.STRING),
        ParameterSpec.newProperty("bloodType", "血型", AttrDataType.STRING),
        ParameterSpec.newProperty("education", "文化程度", AttrDataType.STRING),
        ParameterSpec.newProperty("maritalStatus", "婚姻状况", AttrDataType.STRING),
        ParameterSpec.newProperty("militaryStatus", "兵役状况", AttrDataType.STRING),
        ParameterSpec.newProperty("servicePlace", "服务处所", AttrDataType.STRING),
        ParameterSpec.newProperty("occupation", "职业", AttrDataType.STRING),
        ParameterSpec.newProperty("moveInTimeAndAddress", "何时由何地迁来本市(县)", AttrDataType.STRING),
        ParameterSpec.newProperty("moveOutTimeAndAddress", "何时由何地迁来本地址", AttrDataType.STRING),
        ParameterSpec.newProperty("signature", "承办人签章", AttrDataType.STRING),
        ParameterSpec.newProperty("date", "登记日期", AttrDataType.DATE)));

    ParameterSpec householdHomePage = ParameterSpec.newObject("householdHomePage", "户口本首页",
      Arrays.asList(ParameterSpec.newProperty("householdType", "户别", AttrDataType.STRING),
        ParameterSpec.newProperty("householdNumber", "户号", AttrDataType.STRING),
        ParameterSpec.newProperty("householdOwnerName", "户主姓名", AttrDataType.STRING),
        ParameterSpec.newProperty("householdAddress", "住址", AttrDataType.STRING),
        ParameterSpec.newProperty("householdRegisterDate", "登记日期", AttrDataType.DATE),
        ParameterSpec.newProperty("householdSignature", "承办人签章", AttrDataType.STRING)));
    return ParameterSpec.newRoot(Arrays.asList(user, householdHomePage));
  }

  @Override
  protected String getPrompt(String result) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("以下是户口本的相关信息：").append(result).append("\n");
    prompt.append("要求：\n");
    prompt.append("1、输出结果必须为JSON格式。\n");
    prompt.append("2、输出结果请使用以下字段：\n");
    prompt.append("- name：姓名\n");
    prompt.append("- usedName：曾用名\n");
    prompt.append("- relationship：户主或与户主关系\n");
    prompt.append("- sex：性别\n");
    prompt.append("- birthPlace：出生地\n");
    prompt.append("- nation：民族\n");
    prompt.append("- nativePlace：籍贯\n");
    prompt.append("- birthday：出生日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- address：本市(县)其他住址\n");
    prompt.append("- religion：宗教信仰\n");
    prompt.append("- idCardNo：公民身份证件编号\n");
    prompt.append("- height：身高\n");
    prompt.append("- bloodType：血型\n");
    prompt.append("- education：文化程度\n");
    prompt.append("- maritalStatus：婚姻状况\n");
    prompt.append("- militaryStatus：兵役状况\n");
    prompt.append("- servicePlace：服务处所\n");
    prompt.append("- occupation：职业\n");
    prompt.append("- moveInTimeAndAddress：何时由何地迁来本市(县)\n");
    prompt.append("- moveOutTimeAndAddress：何时由何地迁来本地址\n");
    prompt.append("- signature：承办人签章\n");
    prompt.append("- date：登记日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- householdType：户别，如居民集体户口等\n");
    prompt.append("- householdNumber：户号\n");
    prompt.append("- householdOwnerName：户主姓名\n");
    prompt.append("- householdAddress：住址\n");
    prompt.append("- householdRegisterDate：登记日期，转换成YYYY-MM-DD形式\n");
    prompt.append("- householdSignature：承办人签章\n");
    prompt.append("3、householdHomePage(户口本首页)householdType、householdNumber、householdOwnerName、householdAddress，其余内容为user(常住人口登记卡信息)。\n");
    prompt.append("4、如果字段信息数据为空时，请将数据值设置为空字符串\n");
    prompt.append("5、文字必须完整，不能漏字。");
    prompt.append("6、根据户口本的相关信息和以上要求提取，不做主观判断或外推，严禁编造内容。\n");
    prompt.append("7、请将上述信息提取出来，并返回一个JSON格式的字符串。示例如下：\n");
    prompt.append("""
      ```json
      {"user":{"name":"张三","usedName":"李四","relationship":"户主","sex":"男","birthPlace":"北京市",\
      "nation":"汉族","nativePlace":"江苏省南京市","birthday":"1990-01-01","address":"北京市朝阳区某某街道","religion":"",\
      "idCardNo":"110101199001011234","height":"175cm","bloodType":"O型","education":"本科","maritalStatus":"已婚",\
      "militaryStatus":"已服役","servicePlace":"某某公司","occupation":"工程师","moveInTimeAndAddress":"2020年从上海市浦东新区迁入",\
      "moveOutTimeAndAddress":"2020年从上海市浦东新区某某路迁至此址","signature":"王五","date":"2025-11-18"},\
      "householdHomePage":{"householdType":"非农业家庭户","householdNumber":""\
      ,"householdOwnerName":"张三","householdAddress":"北京市朝阳区某某街道", "householdRegisterDate": "系统管理员"}\
      ,"householdRegisterDate": "2012-5-28"}```
      """);
    return prompt.toString();
  }
}
