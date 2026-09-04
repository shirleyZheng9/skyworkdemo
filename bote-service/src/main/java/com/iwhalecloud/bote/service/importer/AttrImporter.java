package com.iwhalecloud.bote.service.importer;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.skill.AttrSpecValueImport;
import com.iwhalecloud.bote.service.skill.ISkillAttrManageService;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.transform.dto.ImportConfigDTO;
import com.iwhalecloud.bss.litchi.transform.dto.ImportResultDTO;
import com.iwhalecloud.bss.litchi.transform.imports.importers.ImportDataWarp;
import com.iwhalecloud.bss.litchi.transform.imports.importers.ImportLineInfoDTO;
import com.iwhalecloud.bss.litchi.transform.imports.importers.ImportServiceProvider;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * 脚本导入数据解析器
 *
 * @author qian.sisheng
 * @since 2024/1/9
 */
public class AttrImporter implements ImportServiceProvider<AttrSpecValueImport> {
  private static final Logger logger = LoggerFactory.getLogger(AttrImporter.class);

  private static final ISkillAttrManageService attrManageService = SpringUtil.getBean(ISkillAttrManageService.class);
  private static final IRefreshCacheService refreshCacheService = SpringUtil.getBean(IRefreshCacheService.class);

  private final Long tenantId;
  private final Long catalogItemId;

  public AttrImporter(Long tenantId, Long catalogItemId) {
    this.tenantId = tenantId;
    this.catalogItemId = catalogItemId;
  }

  @Override
  public ImportDataWarp<AttrSpecValueImport> toDto(ImportLineInfoDTO infoDTO, ImportLineInfoDTO header) {
    List<Object> headerDataList = Arrays.stream(header.getData()).filter(Objects::nonNull).collect(Collectors.toList());
    Object[] data = Arrays.copyOf(infoDTO.getData(), headerDataList.size());
    try {
      // @formatter:off
      AttrSpecValueImport tableColumnImport = AttrSpecValueImport.builder()
        .attrNbr(Objects.toString(data[0], null))
        .attrName(Objects.toString(data[1], null))
        .attrDesc(Objects.toString(data[2], null))
        .attrValue(Objects.toString(data[3], null))
        .attrValueName(Objects.toString(data[4], null))
        .build();
      // @formatter:on
      // 校验输入数据的合法性
      String errorMsg = validate(tableColumnImport);
      if (StringUtils.isNotEmpty(errorMsg)) {
        return ImportDataWarp.<AttrSpecValueImport>builder().success(false).message(errorMsg).build();
      }
      // 转换数据格式
      convert(tableColumnImport);
      return ImportDataWarp.<AttrSpecValueImport>builder().success(true).data(tableColumnImport).build();
    }
    catch (Exception e) {
      logger.error("Failed to convert import data", e);
      return ImportDataWarp.<AttrSpecValueImport>builder().success(false).message("excel内容格式错误").build();
    }
  }

  private String validate(AttrSpecValueImport attrSpecValueImport) {
    StringBuilder str = new StringBuilder();
    String attrNbr = attrSpecValueImport.getAttrNbr();
    if (StringUtils.isEmpty(attrNbr)) {
      str.append("静态数据编码不能为空");
    }
    if (StringUtils.isEmpty(attrSpecValueImport.getAttrName())) {
      str.append("静态数据名称不能为空");
    }
    if (StringUtils.isEmpty(attrSpecValueImport.getAttrValue())) {
      str.append("属性值不能为空");
    }
    if (StringUtils.isEmpty(attrSpecValueImport.getAttrValueName())) {
      str.append("属性值名称不能为空");
    }
    return str.toString();
  }

  private void convert(AttrSpecValueImport attrSpecValueImport) {
    // attrDesc 的null值转换
    String attrDesc = attrSpecValueImport.getAttrDesc();
    attrSpecValueImport.setAttrDesc(attrDesc == null ? "" : attrDesc);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ImportResultDTO invoke(List<AttrSpecValueImport> list, ImportConfigDTO config) {
    ImportResultDTO importResult = ImportResultDTO.builder().totalCount(list.size()).build();
    Integer limit = SystemParameter.ATTR_IMPORT_LIMIT_PROPERTY.getRequiredIntegerValueFromDb();
    if (list.size() > limit) {
      throw BaseErrorConstant.ATTR_IMPORT_LIMIT_ERROR.toException(limit, list.size());
    }
    else {
      attrManageService.parseSpec(list, tenantId, catalogItemId);
      // 刷新缓存
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_ATTR_SPEC);
      importResult.addSuccessList(list);
      importResult.setSuccessCount(list.size());
    }
    return importResult;
  }
}

