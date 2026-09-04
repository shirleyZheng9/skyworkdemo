package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeDocContentResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.ResultResponse;
import com.iwhalecloud.bote.dto.plugin.params.KnowledgeFileReadParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgePlatformApiService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 知识中台文档内容读取插件执行器
 *
 * @author fan.cong
 * @since 2025-07-21
 */
@Component
public class KnowledgeFileReadPlugin extends AbstractPlugin<KnowledgeFileReadParams> {
  private final IKnowledgePlatformApiService knowledgePlatformApiService;

  public KnowledgeFileReadPlugin(IKnowledgePlatformApiService knowledgePlatformApiService) {
    super(KnowledgeFileReadParams.class);
    this.knowledgePlatformApiService = knowledgePlatformApiService;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("resourceWid", "资源ID", AttrDataType.STRING),
      ParameterSpec.newProperty("resourceType", "资源类型", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("content", "知识中台文件内容", AttrDataType.STRING),
        ParameterSpec.newProperty("docId", "文档id", AttrDataType.INTEGER),
        ParameterSpec.newProperty("redFormat", "文档内容格式", AttrDataType.STRING)));
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_KNOWLEDGE_FILE_READ;
  }

  @Override
  public void validateParams(KnowledgeFileReadParams params) {
    Assert.notNull(params.getResourceType(), "资源类型不能为空");
    Assert.notNull(params.getResourceWid(), "资源ID不能为空");
  }

  @Override
  public Object doRun(KnowledgeFileReadParams plugin) {
    ResultResponse<KnowledgeDocContentResponse> response = knowledgePlatformApiService.knowledgeDocContent(
      plugin.getResourceType(), plugin.getResourceWid());
    if (response != null && response.isSuccess()) {
      return response.getData();
    }
    else {
      Map<String, String> error = new HashMap<>();
      if (response != null) {
        error.put("message", response.getMessage());
        error.put("code", response.getCode());
      }
      return error;
    }
  }
}
