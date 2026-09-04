package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：页面服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillPageManageService {

  /**
   * 保存页面基本信息
   *
   * @param page 页面
   * @return 结果
   */
  ResultVO<SkillPageDTO> saveSkillPageInfo(SkillPageDTO page);

  /**
   * 保存页面
   *
   * @param page 页面
   * @return 结果
   */
  ResultVO<SkillPageDTO> saveSkillPage(SkillPageDTO page);

  /**
   * 查询单个页面
   *
   * @param tenantId 租户 ID
   * @param pageId 页面主键
   * @return 页面
   */
  @Nullable
  SkillPageDTO findSkillPage(Long tenantId, Long pageId);

  /**
   * 查询页面列表
   *
   * @param queryParams 查询条件
   * @return 页面列表
   */
  List<SimpleSkillPageDTO> querySkillPageList(SkillQueryParams queryParams);

  /**
   * 查询页面列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面分页列表
   */
  PageInfo<SkillPageDTO> querySkillPagePage(SkillQueryParams queryParams);

  /**
   * 查询页面列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面分页列表
   */
  PageInfo<SimpleSkillPageDTO> querySimpleSkillPagePage(SkillQueryParams queryParams);

  /**
   * 删除页面
   *
   * @param tenantId 租户 ID
   * @param pageId 页面主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillPage(Long tenantId, Long pageId);

  /**
   * 根据编码获取远程组件
   *
   * @param pageCode 页面编码
   * @param fileInfoId  文件信息iID
   * @param tenantId 租户ID
   */
  String getRemoteComponent(String pageCode, @Nullable Long fileInfoId, Long tenantId);
}
