package com.iwhalecloud.bote.intent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionImportDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 意图问句管理服务
 *
 * @author auto
 * @since 2024-12-18
 */
public interface IIntentQuestionManageService {

  /**
   * 查询单个意图问句
   *
   * @param tenantId 租户 ID
   * @param id 主键
   * @return 意图问句
   */
  IntentQuestionDTO findIntentQuestion(Long tenantId, Long id);

  /**
   * 保存意图问句
   *
   * @param question 意图问句
   * @return 结果
   */
  ResultVO<IntentQuestionDTO> saveIntentQuestion(IntentQuestionDTO question);

  /**
   * 批量保存意图问句
   *
   * @param questions 意标问句列表
   * @return 意标问句列表
   */
  List<IntentQuestionDTO> batchSaveIntentQuestion(List<IntentQuestionDTO> questions);

  /**
   * 删除意图问句
   *
   * @param tenantId 租户 ID
   * @param id 主键
   * @return 结果
   */
  ResultVO<Void> deleteIntentQuestion(Long tenantId, Long id);

  /**
   * 查询意图问句列表
   *
   * @param queryParams 查询条件
   * @return 意图问句列表
   */
  List<IntentQuestionDTO> queryIntentQuestionList(IntentQueryParams queryParams);

  /**
   * 查询意图问句列表（分页）
   *
   * @param queryParams 查询条件
   * @return 意图问句分页列表
   */
  PageInfo<IntentQuestionDTO> queryIntentQuestionPage(IntentQueryParams queryParams);

  /**
   * 收集租户下为意图问题，生成 excel，用于微调评测
   *
   * @param tenantId 租户 ID
   * @return excel 文件
   */
  File createIntentQuestionFile(Long tenantId);

  /**
   * 批量导入意图问句
   *
   * @param file Excel 文件
   * @param tenantId 租户 ID
   * @param sceneId 场景 ID，可为空
   * @return 导入结果
   */
  ResultVO<IntentQuestionImportDTO> batchImportIntentQuestions(MultipartFile file, Long tenantId, Long sceneId);
}
