package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPageManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillPageManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 技能：页面服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
public class SkillPageManageServiceImpl implements ISkillPageManageService {

  private final SkillPageManageMapper pageManageMapper;
  private final QuerySkillMapper querySkillMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final IFileStoreService fileStoreService;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillPageDTO> saveSkillPageInfo(SkillPageDTO page) {
    if (pageManageMapper.existsSkillPageCode(page)) {
      return BaseErrorConstant.CHECK_CODE.toResult(page.getPageCode());
    }
    SkillPageDTO old = page.getPageId() == null ? null : findSkillPage(page.getTenantId(), page.getPageId());
    if (old != null) {
      // 基本信息保存场景，部分参数前端不会传递，需要回填，避免丢失
      page.setPageContent(old.getPageContent());
      page.setPageCssJson(old.getPageCssJson());
      page.setPageTemplateJson(old.getPageTemplateJson());
      page.setFileInfoId(old.getFileInfoId());
    }
    page.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<SkillPageDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, page, false, page.getTenantId(), OperClassEnum.SKILL_PAGE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<SkillPageDTO> saveSkillPage(SkillPageDTO page) {
    if (page.getCopyPageId() != null) {
      return copySkillPage(page);
    }
    if (pageManageMapper.existsSkillPageCode(page)) {
      return BaseErrorConstant.CHECK_CODE.toResult(page.getPageCode());
    }
    page.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillPageDTO old = page.getPageId() == null ? null : findSkillPage(page.getTenantId(), page.getPageId());
    DataDifference<SkillPageDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, page, false, page.getTenantId(), OperClassEnum.SKILL_PAGE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillPageDTO> copySkillPage(SkillPageDTO page) {
    SkillPageDTO oldPage = pageManageMapper.getSkillPage(page.getTenantId(), page.getCopyPageId());
    if (oldPage == null) {
      return BaseErrorConstant.BOT_SKILL_PAGE_NOT_EXIST.toResult(page.getCopyPageId());
    }
    if (pageManageMapper.existsSkillPageCode(page)) {
      return BaseErrorConstant.CHECK_CODE.toResult(page.getPageCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, page, false, page.getTenantId(), OperClassEnum.SKILL_PAGE);
    return ResultVO.success(page);
  }

  @Override
  @Nullable
  public SkillPageDTO findSkillPage(Long tenantId, Long pageId) {
    SkillPageDTO skillPage = pageManageMapper.getSkillPage(tenantId, pageId);
    if (Objects.isNull(skillPage)) {
      return null;
    }
    Long fileId = skillPage.getFileId();
    if (Objects.nonNull(fileId)) {
      FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, skillPage.getFileInfoId());
      skillPage.setFileInfo(fileInfo);
    }
    return skillPage;
  }

  @Override
  public List<SimpleSkillPageDTO> querySkillPageList(SkillQueryParams params) {
    return querySkillMapper.selectSkillPageList(params);
  }

  @Override
  public PageInfo<SkillPageDTO> querySkillPagePage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return pageManageMapper.selectSkillPagePage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillPageDTO> querySimpleSkillPagePage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillPagePage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillPage(Long tenantId, Long pageId) {
    if (resourceElementService.existsRelatedResource(tenantId, pageId, DataSyncCodeEnum.SKILL_PAGE.getCode())) {
      return ResultVO.fail("页面已存在关联配置数据，不允许删除");
    }
    pageManageMapper.deleteSkillPage(tenantId, pageId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_PAGE.name()).clear(tenantId, pageId);
    return ResultVO.success();
  }

  @Override
  public String getRemoteComponent(String pageCode, @Nullable Long fileInfoId, Long tenantId) {
    Long fileId = null;
    if (StringUtils.isNotEmpty(pageCode)) {
      fileId = pageManageMapper.getFileIdByPageCode(pageCode, tenantId);
    }
    if (fileInfoId != null) {
      FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
      Assert.notNull(fileInfo, () -> "页面不存在或未上传文件: fileInfoId=" + fileInfoId);
      fileId = fileInfo.getFileId();
    }
    Assert.notNull(fileId, () -> "页面不存在或未上传文件: pageCode=" + pageCode);
    byte[] bytes = fileStoreService.downloadFile(fileId);
    Assert.isTrue(bytes != null && bytes.length > 0, () -> "页面文件不存在: pageCode=" + pageCode);
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
