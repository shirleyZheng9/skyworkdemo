package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.dto.skill.SimplePromptDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 提示词管理服务
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
public interface IPromptManageService {

  /**
   * 查询单个提示词
   *
   * @param tenantId 租户 ID
   * @param promptId 提示词主键
   * @return 提示词
   */
  @Nullable
  PromptDTO findPrompt(Long tenantId, Long promptId);

  /**
   * 保存提示词
   *
   * @param prompt 提示词
   * @return 结果
   */
  ResultVO<PromptDTO> savePrompt(PromptDTO prompt);

  /**
   * 删除提示词
   *
   * @param tenantId 租户 ID
   * @param promptId 提示词主键
   * @return 结果
   */
  ResultVO<Void> deletePrompt(Long tenantId, Long promptId);

  /**
   * 同步提示词
   *
   * @param prompt 提示词
   * @return 结果
   */
  ResultVO<Void> syncPrompt(SimplePromptDTO prompt);

  /**
   * 校验提示词
   *
   * @param prompt 提示词
   * @return 结果
   */
  String checkPrompt(SimplePromptDTO prompt);

  /**
   * 查询提示词列表
   *
   * @param queryParams 查询条件
   * @return 提示词列表
   */
  List<PromptDTO> queryPromptList(SkillQueryParams queryParams);

  /**
   * 查询提示词列表（分页）
   *
   * @param queryParams 查询条件
   * @return 提示词分页列表
   */
  PageInfo<PromptDTO> queryPromptPage(SkillQueryParams queryParams);

  /**
   * 根据模型 ID、标题、目录名称查询提示词内容
   *
   * @param tenantId 租户 ID
   * @param modelId 指定模型 ID
   * @param title 标题前缀
   * @param catalogName 目录名称
   * @return 提示词内容
   */
  String findPromptContent(Long tenantId, @Nullable Long modelId, String title, String catalogName);
}
