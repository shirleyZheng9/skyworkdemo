package com.iwhalecloud.bote.portal.support.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class OapiV2UserGetResponse {

  /**
   * 是否激活
   */
  @JsonProperty("active")
  private Boolean active;
  /**
   * 是否管理员
   */
  @JsonProperty("admin")
  private Boolean admin;
  /**
   * 头像
   */
  @JsonProperty("avatar")
  private String avatar;
  /**
   * 是否老板
   */
  @JsonProperty("boss")
  private Boolean boss;
  /**
   * 员工邮箱
   */
  @JsonProperty("email")
  private String email;
  /**
   * 是否专属帐号
   */
  @JsonProperty("exclusive_account")
  private Boolean exclusiveAccount;
  /**
   * 专属帐号类型：{sso: 企业自定义idp;dingtalk: 钉钉idp}
   */
  @JsonProperty("exclusive_account_type")
  private String exclusiveAccountType;
  /**
   * 扩展属性，长度最大2000个字符。可以设置多种属性（手机上最多显示10个扩展属性，具体显示哪些属性，请到OA管理后台->设置->通讯录信息设置和OA管理后台->设置->手机端显示信息设置）。 该字段的值支持链接类型填写，同时链接支持变量通配符自动替换，目前支持通配符有：userid，corpid。示例： [工位地址](http:www.dingtalk.com?userid=#userid#&corpid=#corpid#)
   */
  @JsonProperty("extension")
  private String extension;
  /**
   * 是否号码隐藏。隐藏手机号后，手机号在个人资料页隐藏，但仍可对其发DING、发起钉钉免费商务电话。
   */
  @JsonProperty("hide_mobile")
  private Boolean hideMobile;
  /**
   * 入职时间，Unix时间戳，单位ms。
   */
  @JsonProperty("hired_date")
  private Long hiredDate;
  /**
   * 员工工号
   */
  @JsonProperty("job_number")
  private String jobNumber;
  /**
   * 专属帐号登录名
   */
  @JsonProperty("login_id")
  private String loginId;
  /**
   * 主管的ID，仅限企业内部开发调用
   */
  @JsonProperty("manager_userid")
  private String managerUserid;
  /**
   * 手机号码
   */
  @JsonProperty("mobile")
  private String mobile;
  /**
   * 员工名称
   */
  @JsonProperty("name")
  private String name;
  /**
   * 员工的企业邮箱
   */
  @JsonProperty("org_email")
  private String orgEmail;
  /**
   * 企业邮箱类型（profession：标准版，base：基础版）
   */
  @JsonProperty("org_email_type")
  private String orgEmailType;
  /**
   * 是否实名认证
   */
  @JsonProperty("real_authed")
  private Boolean realAuthed;
  /**
   * 备注
   */
  @JsonProperty("remark")
  private String remark;
  /**
   * 是否高管
   */
  @JsonProperty("senior")
  private Boolean senior;
  /**
   * 国际电话区号
   */
  @JsonProperty("state_code")
  private String stateCode;
  /**
   * 分机号
   */
  @JsonProperty("telephone")
  private String telephone;
  /**
   * 职位
   */
  @JsonProperty("title")
  private String title;
  /**
   * 员工在当前开发者企业账号范围内的唯一标识
   */
  @JsonProperty("unionid")
  private String unionid;
  /**
   * 用户id
   */
  @JsonProperty("userid")
  private String userid;
  /**
   * 办公地点
   */
  @JsonProperty("work_place")
  private String workPlace;
}
