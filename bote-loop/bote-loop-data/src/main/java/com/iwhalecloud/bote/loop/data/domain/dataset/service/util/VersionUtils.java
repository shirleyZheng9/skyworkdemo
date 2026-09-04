package com.iwhalecloud.bote.loop.data.domain.dataset.service.util;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * 版本工具类
 * 迁移对应关系: Go语言version_utils.go
 * - 功能: 提供版本相关的工具方法
 * - 方法定义: 各种版本处理方法
 * <p>
 * Java实现说明:
 * - 对应Go的version_utils.go文件
 * - 使用Java静态方法实现工具功能
 * - 提供版本验证和数据处理功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go字符串处理 -> Java字符串处理
 * - Go错误处理 -> Java异常处理
 */
public final class VersionUtils {

  private VersionUtils() {
    // 工具类，禁止实例化
  }

  private static final Pattern VERSION_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

  /**
   * 验证版本号
   * 迁移对应关系: Go语言validateVersion
   * - 功能: 验证新版本号是否有效且大于前一版本
   * - 参数: preVersion - 前一版本, newVersion - 新版本
   * - 返回: 无异常表示验证通过
   * - 用途: 版本号格式和顺序验证
   */
  public static void validateVersion(String preVersion, String newVersion) {
    if (!StringUtils.hasText(newVersion)) {
      throw new BssException("version is empty");
    }

    if (!VERSION_PATTERN.matcher(newVersion).matches()) {
      throw new BssException("version '" + newVersion + "' not a valid semantic version");
    }

    if (!StringUtils.hasText(preVersion)) {
      return; // 无历史版本，直接返回
    }

    if (!VERSION_PATTERN.matcher(preVersion).matches()) {
      throw new BssException("previous version '" + preVersion + "' not a valid semantic version");
    }

    if (!isVersionGreater(newVersion, preVersion)) {
      throw new BssException("new version '" + newVersion + "' should be greater than '" + preVersion + "'");
    }
  }

  /**
   * 比较版本号大小
   * 迁移对应关系: Go语言semver比较逻辑
   * - 功能: 比较两个版本号的大小
   * - 参数: version1 - 版本1, version2 - 版本2
   * - 返回: true表示version1大于version2
   * - 用途: 版本号大小比较
   */
  private static boolean isVersionGreater(String version1, String version2) {
    String[] v1Parts = version1.split("\\.");
    String[] v2Parts = version2.split("\\.");

    int maxLength = Math.max(v1Parts.length, v2Parts.length);
    for (int i = 0; i < maxLength; i++) {
      int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
      int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

      if (v1Part > v2Part) {
        return true;
      }
      else if (v1Part < v2Part) {
        return false;
      }
    }
    return false;
  }

  /**
   * 用数据集信息补全版本信息
   * 迁移对应关系: Go语言patchVersionWithDataset
   * - 功能: 用数据集信息补全版本对象的字段
   * - 参数: dataset - 数据集, version - 版本对象
   * - 返回: 补全后的版本对象
   * - 用途: 版本对象字段补全
   */
  public static DatasetVersion patchVersionWithDataset(Dataset dataset, DatasetVersion version) {
    if (dataset == null || version == null) {
      return version;
    }

    version.setAppId(dataset.getAppId());
    version.setSpaceId(dataset.getSpaceId());
    version.setDatasetId(dataset.getId());
    version.setSchemaId(dataset.getSchemaId());
    version.setVersionNum(dataset.getNextVersionNum());
    version.setDatasetBrief(dataset);
    return version;
  }
}
