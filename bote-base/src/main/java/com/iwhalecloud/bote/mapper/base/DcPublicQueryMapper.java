package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 配置词典管理
 *
 * @author auto
 * @since 2024-09-14
 */
public interface DcPublicQueryMapper {
  /**
   * 获取配置词典列表
   *
   * @param stype 配置字典编码
   * @return 配置词典列表
   */
  List<DcPublicEntity> selectDcPublicListByStype(@Param("stype") Long stype);

  /**
   * 查询所有的配置字典列表
   *
   * @return 配置字典列表
   */
  List<DcPublicEntity> selectDcPublicList();
}
