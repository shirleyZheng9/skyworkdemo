package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.AdminImportRequest;
import com.iwhalecloud.bote.dto.skill.ImportAllResponse;
import com.iwhalecloud.bote.dto.skill.InstallRequest;
import com.iwhalecloud.bote.dto.skill.InstallResponse;
import com.iwhalecloud.bote.dto.skill.SkillInstallLogVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminItemVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportJobStatusVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareItemVO;
import com.iwhalecloud.bote.dto.skill.query.SkillInstallLogQueryParams;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareAdminQueryParams;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * SKILL广场服务
 *
 * @author skill-square
 * @since 2026-03-18
 */
public interface ISkillSquareService {

  /**
   * 分页查询技能广场列表（仅上架且有效技能）
   */
  PageInfo<SkillSquareItemVO> queryPage(SkillSquareQueryParams params);

  /**
   * 获取技能详情（含 readmeContent）
   *
   * @param skillId 技能ID
   * @return 详情，不存在或已下架时返回 null
   */
  @Nullable
  SkillSquareDetailVO getDetail(Long skillId);

  /**
   * 安装技能到智能体
   */
  ResultVO<InstallResponse> install(InstallRequest request);

  /**
   * 根据广场技能编码安装（从数据库解析 skillId，需上架且有效）
   *
   * @param skillCode 广场技能编码，与 {@code bt_agent_skill_square.skill_code} 一致
   * @param tenantId 租户 ID
   * @param botId 智能体 ID
   * @param installSource 安装来源，空则默认 dialogue
   */
  ResultVO<InstallResponse> installBySkillCode(String skillCode, Long tenantId, Long botId, String installSource);

  /**
   * 管理端分页查询（不过滤 online_status，支持 onlineStatus 筛选，仅返回 status_cd='00A'）
   */
  PageInfo<SkillSquareAdminItemVO> adminQueryPage(SkillSquareAdminQueryParams params);

  /**
   * 管理端查询技能详情（不过滤上架状态，status_cd='00A' 的技能均可查询）
   *
   * @param skillId 技能ID
   * @return 详情，技能不存在时返回 null
   */
  @Nullable
  SkillSquareAdminDetailVO adminGetDetail(Long skillId);

  /**
   * 管理端单技能导入（ZIP 包上传）
   *
   * @param request 导入请求参数
   */
  ResultVO<Long> adminImport(AdminImportRequest request);

  /**
   * 管理端单技能编辑（skillName、skillDesc、packageFile）
   */
  ResultVO<Void> adminUpdate(Long skillId, String skillName, String skillDesc,
                             MultipartFile packageFile);

  /**
   * 管理端技能状态更新（enable/disable/delete）
   */
  ResultVO<Void> adminUpdateStatus(Long skillId, String action);

  /**
   * 大批量导出：分包压缩并上传文件系统，立即返回任务 ID，通过 {@link #getBulkExportJobStatus} 轮询
   *
   * @param top 可选，仅导出安装量最高的前 N 条，不传则导出全部
   */
  ResultVO<Long> startBulkExportAsync(Integer top);

  /**
   * 查询异步导出任务状态（含进度与各分包下载路径）
   */
  ResultVO<SkillSquareBulkExportJobStatusVO> getBulkExportJobStatus(Long jobId);

  /**
   * 查询当前登录用户最近一次发起的导出任务状态（关闭弹窗后再次打开可恢复进度；无任务或已过期时 resultObject 为 null）
   */
  ResultVO<SkillSquareBulkExportJobStatusVO> getBulkExportCurrentJobStatus();

  /**
   * 清除当前用户与最近一次导出任务的绑定（用于「新任务」后不再自动恢复已结束任务的展示）
   */
  ResultVO<Void> clearBulkExportCurrentJob();

  /**
   * 全量导入（同步处理）
   *
   * @param zipFile 上传的 zip 文件
   * @return created/updated/failed 统计
   */
  ResultVO<ImportAllResponse> importAll(MultipartFile zipFile);

  /**
   * 查询技能安装记录
   *
   * @param params 查询条件
   * @return 技能安装记录
   */
  PageInfo<SkillInstallLogVO> adminQueryInstallLogs(SkillInstallLogQueryParams params);

  /**
   * 按照广场技能类型统计数量
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @return 各类型技能的数量统计
   */
  Map<String, Integer> getSkillTypeStatistics(Long spaceId, Long tenantId);
}
