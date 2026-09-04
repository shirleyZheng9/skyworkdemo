package com.iwhalecloud.bote.license.check;

import com.iwhalecloud.bote.license.cache.LicenseCache;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * license 应用启动检查器
 *
 * <p>应用启动时检查 license, 如果无效则不允许启动应用。</p>
 * <p>应用启动时检查数据库部署ip，如果和license不匹配则不允许启动应用。</p>
 *
 * @author cheng.xu
 */
@RequiredArgsConstructor
@SuppressFBWarnings("REDOS")
@SuppressWarnings("java:S5998")
public class LicenseBootChecker implements CommandLineRunner {

  private static final String JDBC_URL = "jdbc.url";

  /** 匹配ipv4地址的正则表达式 */
  private static final String IPV4_REGEX = "(?:\\d{1,3}\\.){3}\\d{1,3}";

  /** 匹配ipv6地址的正则表达式 */
  private static final String IPV6_REGEX = "\\[([0-9a-fA-F:]*?)\\]";

  /** 匹配域名的正则表达式 */
  private static final String DOMAIN_REGEX = "[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+";

  private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

  private static final Pattern IPV6_PATTERN = Pattern.compile(IPV6_REGEX);

  private static final Pattern DOMAIN_PATTERN = Pattern.compile(DOMAIN_REGEX);

  private final LicenseChecker licenseChecker;

  private final LicenseCache licenseCache;

  @Override
  public void run(String... args) {
    CheckResult checkResult = licenseChecker.checkLicense(true);
    if (!checkResult.isSuccess()) {
      throw new IllegalStateException("license 校验不通过，请配置有效的 license: " + checkResult.getErrorMsg());
    }
    List<String> databaseIps = licenseCache.getLicense().getDatabaseIps();
    // 如果 license 中没有约束数据库 IP，则直接通过
    if (CollectionUtils.isEmpty(databaseIps)) {
      return;
    }
    String jdbcUrl = SpringUtil.getEnvironment().getRequiredProperty(JDBC_URL);
    String databaseIp = obtainIpOrDomain(jdbcUrl);
    if (!databaseIps.contains(databaseIp)) {
      throw new IllegalStateException("license 校验不通过，部署的数据库地址与申请的不一致");
    }
  }

  String obtainIpOrDomain(String jdbcUrl) {
    Matcher matcher = IPV4_PATTERN.matcher(jdbcUrl);
    if (matcher.find()) {
      return matcher.group();
    }
    matcher = DOMAIN_PATTERN.matcher(jdbcUrl);
    if (matcher.find()) {
      return matcher.group();
    }
    matcher = IPV6_PATTERN.matcher(jdbcUrl);
    if (matcher.find()) {
      return matcher.group(1);
    }
    throw new IllegalStateException("从 '" + jdbcUrl + "' 中匹配不到对应的数据库地址信息");
  }
}
