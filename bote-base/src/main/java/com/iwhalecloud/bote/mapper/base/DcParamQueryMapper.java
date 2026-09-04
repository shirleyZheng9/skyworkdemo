package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.SimpleDcParamDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 系统参数查询 Mapper
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
public interface DcParamQueryMapper {
  /**
   * 根据参数编码查询系统参数
   *
   * @param paramCode 参数编码
   * @return 参数值
   */
  SimpleDcParamDTO findDcParamByCode(@Param("paramCode") String paramCode);

  /**
   * 查询所有系统参数列表
   *
   * @return 系统参数列表
   */
  List<SimpleDcParamDTO> selectDcParam();

}
