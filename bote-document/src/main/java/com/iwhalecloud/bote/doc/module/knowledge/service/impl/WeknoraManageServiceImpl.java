package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.doc.module.knowledge.dto.WeKnoraTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraKnowledgeClientHelper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraKnowledgeBaseRespDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraLoginHelper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraLoginResponse;
import com.iwhalecloud.bote.doc.module.knowledge.service.IWeknoraManageService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import lombok.RequiredArgsConstructor;

@Service
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@RequiredArgsConstructor
public class WeknoraManageServiceImpl implements IWeknoraManageService {

  private final WeKnoraLoginHelper weKnoraLoginHelper;
  private final WeKnoraKnowledgeClientHelper weKnoraKnowledgeClientHelper;

  @Override
  public List<WeKnoraKnowledgeBaseRespDTO> queryKnowledgeBases(Long tenantId) {
    return weKnoraKnowledgeClientHelper.listKnowledgeBases(tenantId);
  }

  @Override
  public ResultVO<WeKnoraTokenDTO> getWeKnoraToken(Long tenantId) {
    WeKnoraLoginResponse loginInfo = weKnoraLoginHelper.getLoginInfo(tenantId);
    WeKnoraTokenDTO token = new WeKnoraTokenDTO();
    token.setWeKnoraToken(loginInfo.getToken());
    token.setWeKnoraRefreshToken(loginInfo.getRefreshToken());
    token.setWeKnoraUser(JsonUtil.toJsonString(loginInfo.getUser()));
    return ResultVO.success(token);
  }
}
