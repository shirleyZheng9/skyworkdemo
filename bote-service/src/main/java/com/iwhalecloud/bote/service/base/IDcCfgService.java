package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.DcCfgSafeDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSaveRequest;
import java.util.List;

public interface IDcCfgService {
  /**
   * 查询开关类型字典列表
   *
   * @return 系统参数配置列表
   */
  List<DcCfgDTO> querySwitchList();

  /**
   * 查询参数列表
   *
   * @return 系统参数配置列表
   */
  List<DcCfgDTO> queryParamList(String searchContent, String type);

  /**
   * 修改参数数据
   *
   * @param dcCfgSaveRequest 请求参数
   * @return 结果
   */
  ResultVO<Void> modParamData(DcCfgSaveRequest dcCfgSaveRequest);

  /**
   * 查询安全相关参数列表
   *
   * @return 安全相关参数列表
   */
  List<DcCfgSafeDTO> getSafeParamList();

  /**
   * 修改安全相关参数值
   *
   * @param request 请求参数
   * @return 修改结果
   */
  ResultVO<Void> modSafeParamData(DcCfgSaveRequest request);

}
