package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.cache.DcPublicCache;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.util.Assert;

/**
 * 配置词典辅助工具
 *
 * @author chen.linfa
 * @since 2024-11-23
 */
public final class DcPublicUtil {
  private DcPublicUtil() {
  }

  /** 数据库类型匹配机制 */
  public static final String DATABASE_ID_MATCH = "7500";
  /** 公共 SQL 片段 */
  public static final String SQL_FRAGMENT = "8000";
  /** 联网搜索策略参数 */
  public static final String WEB_SEARCH_PARAM = "8100";
  /** 不同类型的数据库创建语句  */
  public static final String DATABASE_CREATE_SQL = "7501";

  /** 单点登录 - groovy 脚本说明 */
  public static final String SSO_GROOVY_TIP = "8200";

  private static final DcPublicCache cache = SpringUtil.getBean(DcPublicCache.class);

  /**
   * 根据 stype 获取首条数据的 codea，值不为空
   *
   * @param stype 类型
   * @return codea 值
   */
  public static String getRequiredCodea(String stype) {
    String text = Optional.ofNullable(cache.getDcPublic(stype)).map(DcPublicEntity::getCodea).orElse("");
    Assert.hasText(text, "查询不到预置的配置词典[stype=" + stype + "]，请联系管理员");
    return text;
  }

  public static List<DcPublicEntity> getList(String stype) {
    List<DcPublicEntity> list = cache.getDcPublicList(stype);
    Assert.notEmpty(list, "查询不到预置的配置词典[stype=" + stype + "]，请联系管理员");
    return list;
  }

  /**
   * 获取配置词典
   */
  public static DcPublicEntity get(String stype) {
    return getList(stype).get(0);
  }
}
