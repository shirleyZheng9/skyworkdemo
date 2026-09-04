package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 目录管理服务
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
public interface ICatalogManageService {

  /**
   * 保存目录
   *
   * @param catalog 目录
   * @return 结果
   */
  ResultVO<CatalogDTO> saveCatalog(CatalogDTO catalog);

  /**
   * 删除目录
   *
   * @param tenantId 租户 ID
   * @param catalogId 目录 ID
   * @return 结果
   */
  ResultVO<Void> deleteCatalog(Long tenantId, Long catalogId);

  /**
   * 按照分类查询目录树
   *
   * @param params 查询参数
   * @return 目录树
   */
  List<CatalogDTO> queryCatalogTree(CatalogQueryParams params);

  /**
   * 查询目录下所有子目录 ID 列表
   *
   * @param tenantId 租户 ID
   * @param catalogId 目录 ID
   * @param catalogType 目录类型
   * @return 目录 ID 列表
   */
  List<Long> queryChildrenCatalogIds(Long tenantId, @Nullable Long catalogId, String catalogType);

  /**
   * 查询目录下所有子目录 ID 列表
   *
   * @param tenantId 租户 ID
   * @param catalogId 目录 ID
   * @param catalogType 目录类型
   * @return 目录 ID 列表
   */
  List<Long> queryChildrenCatalogIds(Long tenantId, Long spaceId, @Nullable Long catalogId, String catalogType);


  /**
   * 根据主键获取目录
   *
   * @param tenantId 租户ID
   * @param catalogId 目录ID
   * @return 目录信息，如果不存在则返回 null
   */
  @Nullable
  CatalogDTO getCatalog(Long tenantId, Long catalogId);
}
