package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSafeDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSimpleDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author auto
 * @since 2024-09-14
 */
public interface DcCfgMapper {

  /**
   * 查询所有开关状态
   */
  List<DcCfgDTO> selectAllSwitches(@Param("type") String type);

  /**
   * 根据所有配置参数
   */
  List<DcCfgDTO> selectAllParams(String type);

  /**
   * 更新参数值
   */
  int updateParamValue(@Param("paramCode") String paramCode, @Param("paramVal") String paramVal, @Param("updatorId") Long updatorId);

  /**
   * 根据参数编码查询参数值
   */
  List<DcCfgSimpleDTO> selectParamValueByParamCodes(@Param("paramCodes") List<String> paramCodes);

  /**
   * 查询所有安全策略相关的配置参数
   */
  List<DcCfgSafeDTO> selectAllSafeParams();

}
