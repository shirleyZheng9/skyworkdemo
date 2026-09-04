package com.iwhalecloud.bote.mapper.a2a;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aPlatformQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * A2A 平台相关数据库操作
 *
 * @author bianjp
 * @since 2025-09-08
 */
public interface A2aPlatformMapper {
  /**
   * 校验平台编码是否已存在
   */
  boolean existsPlatformCode(@Param("tenantId") Long tenantId, @Param("platformCode") String platformCode);

  /**
   * 检查平台下是否存在 A2A 服务
   */
  boolean existsAgentByPlatformId(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId);

  /**
   * 根据主键查询 A2A 平台
   */
  @Nullable
  A2aPlatformDTO selectPlatform(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId);

  /**
   * 查询 A2A 平台的鉴权配置
   */
  @Nullable
  A2aPlatformDTO selectPlatformAuthConfig(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId);

  /**
   * 根据平台编码查询 A2A 平台
   */
  @Nullable
  A2aPlatformDTO selectPlatformByCode(@Param("tenantId") Long tenantId, @Param("platformCode") String platformCode);

  /**
   * 根据平台 ID 查询平台鉴权扩展功能 ID
   */
  @Nullable
  Long selectAuthExtFuncIdByPlatformId(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId);

  /**
   * 新增 A2A 平台
   */
  int insertPlatform(@Param("dto") A2aPlatformDTO platform);

  /**
   * 修改 A2A 平台
   */
  int updatePlatform(@Param("dto") A2aPlatformDTO platform);

  /**
   * 修改发布密钥
   */
  int updatePublishKey(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId,
                       @Param("publishKey") String publishKey, @Param("updatorId") Long updatorId);

  /**
   * 删除 A2A 平台
   */
  int deletePlatform(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId, @Param("updatorId") Long updatorId);

  /**
   * 查询 A2A 平台列表
   */
  List<A2aPlatformDTO> selectPlatformList(@Param("query") A2aPlatformQueryParams queryParams);

  /**
   * 分页查询 A2A 平台
   */
  Page<A2aPlatformDTO> selectPlatformPage(@Param("query") A2aPlatformQueryParams queryParams, RowBounds rowBounds);
}
