package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.knowledge.docchain.request.DocSplitRequest;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.SplitDocResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.SplitDocResponse.Table;
import com.iwhalecloud.bote.dto.plugin.TableExtractedPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 表格提取插件，支持word、pdf、图片
 *
 * @author qian.sisheng
 * @since 2025-11-24
 */
@Component
public class TableExtractedPlugin extends AbstractPlugin<TableExtractedPluginParams> {
  private final IFileStoreService fileStoreService;
  private final DocChainDocumentHelper docChainDocumentHelper;

  public TableExtractedPlugin(IFileStoreService fileStoreService, DocChainDocumentHelper docChainDocumentHelper) {
    super(TableExtractedPluginParams.class);
    this.fileStoreService = fileStoreService;
    this.docChainDocumentHelper = docChainDocumentHelper;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_EXTRACT_TABLE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("file", "文件", AttrDataType.STRING),
      ParameterSpec.newProperty("type", "文件类型", AttrDataType.STRING),
      ParameterSpec.newProperty("fileName", "文件名", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("tables", "表格列表",
        ParameterSpec.newProperty("table", "表格", AttrDataType.STRING))));
  }

  @Override
  public void validateParams(TableExtractedPluginParams params) {
    Assert.hasText(params.getFile(), "file不能为空");
    Assert.hasText(params.getType(), "type不能为空");
  }

  @Override
  public Object doRun(TableExtractedPluginParams pluginParams) {
    SplitDocResponse splitDocResponse;
    List<String> htmlTableList;
    if ("bote".equals(pluginParams.getType())) {
      if (!StringUtils.isNumeric(pluginParams.getFile())) {
        throw new BssException("file参数必须是文件ID且只包含数字");
      }
      Long fileId = Long.valueOf(pluginParams.getFile());
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      Assert.notNull(fileInfo, "文件不存在");
      byte[] bytes = fileStoreService.downloadFile(fileId);
      String base64 = Base64.getEncoder().encodeToString(bytes);
      splitDocResponse = getSplitDocResponse(base64, fileInfo.getFileName());
    }
    else if ("base64".equals(pluginParams.getType())) {
      Assert.hasText(pluginParams.getFileName(), "fileName不能为空");
      if (!pluginParams.getFileName().contains(".") && pluginParams.getFileName().lastIndexOf(".") <= 0) {
        throw new BssException("fileName参数格式错误， fileName需使用完整的文件名称(带扩展名)，如xx.docx");
      }
      String base64String = pluginParams.getFile();
      // 兼容base64字符串格式
      if (base64String.contains(";base64,")) {
        base64String = base64String.substring(base64String.indexOf(";base64,") + ";base64,".length());
      }
      splitDocResponse = getSplitDocResponse(base64String, pluginParams.getFileName());
    }
    else {
      throw new BssException("不支持的type参数");
    }
    List<Table> tables = splitDocResponse.getData().getTables();
    htmlTableList = CollectionUtils.emptyIfNull(tables).stream().map(Table::getHtmlContent).toList();
    return Map.of("tables", htmlTableList);
  }

  /**
   * 获取文档块拆分响应
   */
  private SplitDocResponse getSplitDocResponse(String base64, String fileName) {
    DocSplitRequest request = new DocSplitRequest();
    request.setContent(base64);
    request.setTitle(fileName);
    return docChainDocumentHelper.syncDocSplit(CommonConsts.COPILOT_TENANT_ID, request);
  }
}
