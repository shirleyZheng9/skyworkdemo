package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.base.SimpleAttrValueRelDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 技能：属性管理
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
public interface AttrQueryMapper {

  /**
   * 根据静态编码查询静态值列表
   */
  List<SimpleAttrDTO> selectAttrValueByAttrCode(@Param("tenantId") Long tenantId, @Param("attrCode") String attrCode);

  /**
   * 查询所有静态值列表
   */
  List<SimpleAttrDTO> selectAllAttrValue(@Param("tenantId") Long tenantId);

  /**
   * 批量查询关联属性列表
   */
  List<SimpleAttrValueRelDTO> selectAttrValueRelList(@Param("tenantId") Long tenantId, @Param("attrValueIds") List<Long> attrValueIds);
}
