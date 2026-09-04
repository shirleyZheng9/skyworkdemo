package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.google.common.base.CaseFormat;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author qian.sisheng
 * @since 2025-3-1
 */
@Component
@RequiredArgsConstructor
public class DocumentHelper {
  private final Logger logger = LoggerFactory.getLogger(DocumentHelper.class);

  private static final int TINY_LENGTH = 64;
  private static final int SMALL_LENGTH = 128;
  private static final int MEDIUM_LENGTH = 256;
  private static final int LARGE_LENGTH = 512;
  private final JdbcTemplate jdbcTemplate;
  private final DocumentManageMapper documentManageMapper;

  @Transactional
  public int updateDocumentDcDocumentId(Long fileInfoId, Long tenantId, String dcDocumentId) {
    return documentManageMapper.updateDocumentDcDocumentId(fileInfoId, tenantId, dcDocumentId);
  }

  /**
   * 分配字段
   *
   * @param parameter 参数
   * @param usedMappings 已使用的映射字段
   * @param maxLength 字段最大长度
   * @return 映射字段
   */
  public String allocateField(DocumentParameterDTO parameter, List<String> usedMappings, int maxLength) {
    maxLength = calculateMaxLength(parameter, maxLength);
    String currentMapping = parameter.getMappingCode();

    if (isExistingMappingValid(currentMapping, maxLength)) {
      return currentMapping;
    }

    String prefix = getPrefix(maxLength);
    Optional<String> availableCode = findAvailableCode(prefix, usedMappings);

    return availableCode.orElseGet(() -> upgradeFieldLevel(parameter, usedMappings, prefix));
  }

  private int calculateMaxLength(DocumentParameterDTO parameter, int maxLength) {
    if (maxLength == 0) {
      maxLength = estimateByDataType(parameter.getDataType());
    }
    return parameter.getMaxLength() != null && parameter.getMaxLength() > maxLength ? parameter.getMaxLength() : maxLength;
  }

  private int estimateByDataType(String dataType) {
    switch (dataType) {
      case "string":
        return SMALL_LENGTH;
      case "integer":
      case "number":
      case "date":
      case "dateTime":
        return TINY_LENGTH;
      default:
        return 0;
    }
  }

  private boolean isExistingMappingValid(String mappingCode, int requiredLength) {
    return StringUtils.isNotEmpty(mappingCode) && getLength(mappingCode) >= requiredLength;
  }

  private Optional<String> findAvailableCode(String prefix, List<String> usedMappings) {
    for (int i = 1; i <= 10; i++) {
      String code = prefix + i;
      if (!usedMappings.contains(code)) {
        return Optional.of(code);
      }
    }
    return Optional.empty();
  }

  private String upgradeFieldLevel(DocumentParameterDTO parameter, List<String> usedMappings, String currentPrefix) {
    switch (currentPrefix) {
      case "varcharTinyCol":
        return allocateField(parameter, usedMappings, TINY_LENGTH + 1);
      case "varcharSmallCol":
        return allocateField(parameter, usedMappings, SMALL_LENGTH + 1);
      case "varcharMediumCol":
        return allocateField(parameter, usedMappings, MEDIUM_LENGTH + 1);
      case "varcharLargeCol":
        return allocateField(parameter, usedMappings, Integer.MAX_VALUE);
      default:
        throw new BssException("没有可用的字段: " + parameter.getParameterName());
    }
  }

  private String getPrefix(int estimatedLength) {
    String prefix;
    if (estimatedLength > LARGE_LENGTH) {
      prefix = "textCol";
    }
    else if (estimatedLength > MEDIUM_LENGTH) {
      prefix = "varcharLargeCol";
    }
    else if (estimatedLength > SMALL_LENGTH) {
      prefix = "varcharMediumCol";
    }
    else if (estimatedLength > TINY_LENGTH) {
      prefix = "varcharSmallCol";
    }
    else {
      prefix = "varcharTinyCol";
    }
    return prefix;
  }

  private int getLength(String prefix) {
    int length;
    if (Strings.CS.startsWith(prefix, "textCol")) {
      length = Integer.MAX_VALUE;
    }
    else if (Strings.CS.startsWith(prefix, "varcharLargeCol")) {
      length = LARGE_LENGTH;
    }
    else if (Strings.CS.startsWith(prefix, "varcharMediumCol")) {
      length = MEDIUM_LENGTH;
    }
    else if (Strings.CS.startsWith(prefix, "varcharSmallCol")) {
      length = SMALL_LENGTH;
    }
    else {
      length = TINY_LENGTH;
    }
    return length;
  }

  /**
   * 调整映射字段
   *
   * @param contentList 文档内容
   * @param parameters 文档参数
   */
  public void adjustMappingCode(List<DocumentContentDTO> contentList, List<DocumentParameterDTO> parameters, Long documentId, Long tenantId) {
    if (CollectionUtils.isEmpty(contentList) || CollectionUtils.isEmpty(parameters)) {
      return;
    }
    List<String> mappingCodes = parameters.stream().map(DocumentParameterDTO::getMappingCode).collect(Collectors.toList());
    try {
      for (DocumentParameterDTO parameter : parameters) {
        int maxLength = getMaxLength(contentList, parameter.getMappingCode());
        String prefix = getPrefix(maxLength);
        if (!Strings.CS.startsWith(parameter.getMappingCode(), prefix) && getLength(parameter.getMappingCode()) < maxLength) {
          String mappingCode = allocateField(parameter, mappingCodes, maxLength);
          updateContentFields(contentList, parameter.getMappingCode(), mappingCode);
          parameter.setOldMappingCode(parameter.getMappingCode());
          parameter.setMappingCode(mappingCode);
          parameter.setIsChanged(true);
          batchUpdateMappingCodes(parameter, documentId, tenantId);
        }
      }
    }
    catch (Exception e) {
      logger.error("Failed to adjust mappingCode: ", e);
      throw new BssException("调整字段映射异常: ", e);
    }
  }

  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  private void batchUpdateMappingCodes(DocumentParameterDTO parameter, Long documentId, Long tenantId) {
    // 驼峰转下划线
    String mappingCode = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, parameter.getMappingCode());
    String oldMappingCode = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, parameter.getOldMappingCode());

    // 更新新字段值
    List<Object[]> args1 = new ArrayList<>();
    String updateSql =
      "update bt_document_content set " + mappingCode + " = " + oldMappingCode + " where document_id = ? and tenant_id = ? and " + oldMappingCode
        + " is not null;";
    args1.add(new Object[] {documentId, tenantId});
    // 将旧字段值置为 null
    List<Object[]> args2 = new ArrayList<>();
    String nullSql = "update bt_document_content set " + oldMappingCode + " = null" + " where document_id = ? and tenant_id = ?;";
    args2.add(new Object[] {documentId, tenantId});
    // 执行批量更新
    jdbcTemplate.batchUpdate(updateSql, args1);
    jdbcTemplate.batchUpdate(nullSql, args2);
  }

  private void updateContentFields(List<DocumentContentDTO> contentList, String oldMappingCode, String newMappingCode) {
    try {
      for (DocumentContentDTO content : contentList) {
        Object value = BeanUtils.getProperty(content, oldMappingCode);
        BeanUtils.setProperty(content, oldMappingCode, null);
        BeanUtils.setProperty(content, newMappingCode, value);
      }
    }
    catch (Exception e) {
      throw new BssException("Update content filed failed: " + oldMappingCode, e);
    }
  }

  private int getMaxLength(List<DocumentContentDTO> contentList, String mappingCode) {
    try {
      int maxLength = 0;
      for (DocumentContentDTO content : contentList) {
        String value = BeanUtils.getProperty(content, mappingCode);
        if (StringUtils.isNotEmpty(value)) {
          maxLength = Math.max(maxLength, value.length());
        }
      }
      return maxLength;
    }
    catch (Exception e) {
      throw new BssException("Get max length failed: mappingCode=" + mappingCode, e);
    }
  }

}

