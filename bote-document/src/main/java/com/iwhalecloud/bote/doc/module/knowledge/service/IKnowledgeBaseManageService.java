package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseSimpleDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeComboboxDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeEnabledTypeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateKnowlegeVisibilityScopeDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocKnowledgeBaseQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeComboboxQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 知识库管理服务
 *
 * @author auto
 * @since 2024-09-20
 */
public interface IKnowledgeBaseManageService {

  /**
   * 查询单个知识库
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库主键
   * @return 知识库
   */
  KnowledgeBaseDTO findKnowledgeBase(Long tenantId, Long knowledgeId);

  /**
   * 保存知识库基本信息
   *
   * @param knowledge 知识库
   * @return 知识库
   */
  ResultVO<KnowledgeBaseDTO> saveKnowledgeBase(KnowledgeBaseDTO knowledge);

  /**
   * 删除知识库
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库主键
   * @return 结果
   */
  ResultVO<KnowledgeBaseDTO> deleteKnowledgeBase(Long tenantId, Long knowledgeId);

  /**
   * 重新构建知识库
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库主键
   * @return 结果
   */
  ResultVO<String> rebuild(Long tenantId, Long knowledgeId, Boolean newDoc);

  /**
   * 查询知识库列表
   *
   * @param queryParams 查询条件
   * @return 知识库列表
   */
  List<SimpleKnowledgeBaseDTO> queryKnowledgeBaseList(DocKnowledgeBaseQueryParams queryParams);

  /**
   * 查询知识库列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库分页列表
   */
  PageInfo<KnowledgeBaseDTO> queryKnowledgeBasePage(DocKnowledgeBaseQueryParams queryParams);

  /**
   * 查询知识库列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库分页列表
   */
  PageInfo<KnowledgeBaseSimpleDTO> queryKnowledgeBasePageByType(DocKnowledgeBaseQueryParams queryParams);

  /**
   * 查询知识库列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库分页列表
   */
  PageInfo<SimpleKnowledgeBaseDTO> querySimpleKnowledgeBasePage(DocKnowledgeBaseQueryParams queryParams);

  /**
   * 分页查询知识库下拉列表
   */
  PageInfo<KnowledgeComboboxDTO> queryKnowledgeComboboxPage(KnowledgeComboboxQueryParams queryParams);

  /**
   * 查询知识库最近的查询记录
   */
  List<String> queryLatestQueryRecords(Long tenantId, Long knowledgeId, String querySource);

  /**
   * 测试知识库召回
   */
  KnowledgeRecallResponse testRecall(KnowledgeRecallParamDTO params);

  /**
   * docChain文档召回
   */
  KnowledgeRecallResponse docChainBatchRecall(KnowledgeRecallParamDTO params);

  /**
   * 添加知识库查询记录
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库 ID
   * @param source 查询来源
   * @param query 查询内容
   */
  void addKnowledgeQueryRecord(Long tenantId, Long knowledgeId, String source, String query);

  /**
   * 根据对话窗口，上传的文件，实时构建知识库
   *<p>使用场景：将 API 封装成 groovy 脚本，在工作流中使用 </p>
   * @param tenantId 租户 ID
   * @param fileId 文件 ID
   * @param maxWaitTime 超时时间（毫秒）
   * @return 知识库 ID 和是否完成构建标识
   */
  ResultVO<Pair<Long, Boolean>> buildKnowledgeByFileId(Long tenantId, Long fileId, int maxWaitTime);

  /**
   * 清理实时构建的知识库
   */
  void clearTemporaryKnowledge();

  /**
   * 查询 DocChain 知识信息策略设置
   */
  List<DocChainExtraDTO> getDocChainExtraConfig();

  /**
   * 获取租户的知识库配置
   */
  ResultVO<KnowledgeInfoDTO> getTenantKnowledgeConfig(Long tenantId);

  /**
   * 获取知识库启用类型
   */
  ResultVO<KnowledgeEnabledTypeDTO> getKnowledgeEnabledType();

  /**
   * 更新知识库可见范围
   * @param params
   * @return
   */
  ResultVO<Void> updateKonwledgeVisibilityScope(UpdateKnowlegeVisibilityScopeDTO params);

  /**
   *
   * 知识飞轮的知识库查询接口
   * @param queryParams
   * @return
   */
  List<SimpleKnowledgeBaseDTO> querySimpleKnowledgeBaseList(DocKnowledgeBaseQueryParams queryParams);

  /**
   * 查询动态配置参数
   *
   * @param tenantId 租户 ID
   * @return 动态配置参数列表
   */
  Map<String, Object> queryDynamicConfigParams(Long tenantId);
}
