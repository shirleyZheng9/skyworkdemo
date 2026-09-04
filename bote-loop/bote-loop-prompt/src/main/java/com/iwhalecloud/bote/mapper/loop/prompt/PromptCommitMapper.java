package com.iwhalecloud.bote.mapper.loop.prompt;

import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDCommitVersionPair;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * Prompt提交记录Mapper接口
 * 对应Go代码中的PromptCommit表操作
 */
@Mapper
public interface PromptCommitMapper {

  /**
   * 插入Prompt提交记录
   *
   * @param promptCommitPO Prompt提交记录对象
   * @return 影响行数
   */
  int insert(PromptCommitEntity promptCommitPO);

  /**
   * 根据PromptID和版本查询提交记录
   *
   * @param promptId Prompt ID
   * @param commitVersion 提交版本
   * @return Prompt提交记录
   */
  PromptCommitEntity selectByPromptIdAndVersion(@Param("promptId") Long promptId,
                                                @Param("commitVersion") String commitVersion);

  /**
   * 根据条件对查询提交记录
   *
   * @param pairs PromptID和版本对列表
   * @return Prompt提交记录列表
   */
  List<PromptCommitEntity> selectByPairs(@Param("pairs") List<PromptIDCommitVersionPair> pairs);

  /**
   * 根据条件查询提交记录列表
   *
   * @param param 查询参数
   * @return Prompt提交记录列表
   */
  List<PromptCommitEntity> selectByCondition(ListCommitParam param, RowBounds rowBounds);

  /**
   * 根据ID更新提交记录
   *
   * @param promptCommitPO Prompt提交记录对象
   * @return 影响行数
   */
  int updateById(PromptCommitEntity promptCommitPO);

  /**
   * 根据ID删除提交记录
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(@Param("id") Long id);
}
