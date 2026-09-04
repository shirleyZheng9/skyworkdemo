package com.iwhalecloud.bote.license.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iwhalecloud.common.license.core.model.LicenseInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * license 扩展信息
 *
 * @author zhangJun
 * @since 2022/4/7
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class LicenseExtInfoDTO extends LicenseInfo {

  /** 版本类型(trial, basic, ultimate) */
  private String version;
  /** 版本名称 */
  private String versionName;
  /** 最大用户数，为空或非正数表示不限制 */
  private Long maxUserNum;
  /** 最大应用数，为空或非正数表示不限制 */
  private Long maxAppNum;
  /** 能使用的数据库的ip列表，为空表示不限制 */
  private List<String> databaseIps;
  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
  private Date createDate;
  /** 失效时间，为空表示不限制 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
  private Date expireDate;

  public LicenseExtInfoDTO() {
  }

  public LicenseExtInfoDTO(LicenseInfo licenseInfo) {
    setSolutionId(licenseInfo.getSolutionId());
    setSolutionName(licenseInfo.getSolutionName());
    setProductId(licenseInfo.getProductId());
    setProductName(licenseInfo.getProductName());
    setProductVersionId(licenseInfo.getProductVersionId());
    setProductVersionCode(licenseInfo.getProductVersionCode());
    setTargetProjectId(licenseInfo.getTargetProjectId());
    setTargetProjectType(licenseInfo.getTargetProjectType());
    setTargetProjectName(licenseInfo.getTargetProjectName());
    setTargetProjectOrgId(licenseInfo.getTargetProjectOrgId());
    setTargetProjectOrgName(licenseInfo.getTargetProjectOrgName());
    setTargetProjectLeaderNo(licenseInfo.getTargetProjectLeaderNo());
    setTargetProjectLeaderName(licenseInfo.getTargetProjectLeaderName());
    setApplyUserNo(licenseInfo.getApplyUserNo());
    setApplyUserName(licenseInfo.getApplyUserName());
    setApplyTime(licenseInfo.getApplyTime());
    setLevel(licenseInfo.getLevel());
    setCreateTime(licenseInfo.getCreateTime());
    setExtendInfo(licenseInfo.getExtendInfo());
    setExpireTime(licenseInfo.getExpireTime());

    // 转为日期对象以方便使用
    if (StringUtils.isNumeric(licenseInfo.getCreateTime())) {
      this.createDate = new Date(Long.parseLong(licenseInfo.getCreateTime()));
    }
    if (StringUtils.isNumeric(licenseInfo.getExpireTime())) {
      this.expireDate = new Date(Long.parseLong(licenseInfo.getExpireTime()));
    }
  }
}
