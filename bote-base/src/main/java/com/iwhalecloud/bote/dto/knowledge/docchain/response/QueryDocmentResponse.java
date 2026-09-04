package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询文档列表响应
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Getter
@Setter
@ToString
public class QueryDocmentResponse {
  /** 是否成功 */
  private Boolean success;
  /** 异常信息 */
  private String err;
  /** 文档分页信息 */
  private DocmentPageInfo data;

  /**
   * 文档分页信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocmentPageInfo {
    /** 当前页面 */
    private Integer current;
    /** 总页数 */
    private Integer total;
    /** 文档信息 */
    private List<DocmentInfo> list;
  }

  /**
   * 文档信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonNaming(SnakeCaseStrategy.class)
  public static class DocmentInfo {
    /** 文档 ID */
    private Long id;
    /** 主题 ID */
    private Long topicId;
    /** 转换 MD 状态 */
    private String convertMdState;
    /** 拆分 MD 状态 */
    private String splitMdState;
    /** 生成摘要状态 */
    private String summaryState;
    /** 创建时间 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createDate;
    /** 文件名称 */
    private String path;
    /** 文件大小 */
    private Long fileSize;

    /**
     * 获取文档状态（博特平台的状态枚举值）
     */
    @JsonIgnore
    public String getDocStatus() {
      if (isFailed()) {
        return KnowledgeConsts.DOCUMENT_STATUS_FAILED;
      }
      if (isProcessing()) {
        return KnowledgeConsts.DOCUMENT_STATUS_ANALYZING;
      }
      if (isSuccess()) {
        return KnowledgeConsts.DOCUMENT_STATUS_FINISH;
      }
      return KnowledgeConsts.DOCUMENT_STATUS_ANALYZING;
    }

    /**
     * 是否处理失败
     */
    @JsonIgnore
    public boolean isFailed() {
      // https://docs.iwhalecloud.com/doi/cjtgmr/docchain-doc-space/interface-manual/file-proc-api
      // 和 DocChain 文档空间页面判断处理失败的逻辑保持一致
      return "3".equals(convertMdState) || "12".equals(summaryState);
    }
    /**
     * 是否处理中，知识图谱有新过程状态
     * convert_md_state：2，summary_state：12---处理失败
     * convert_md_state：9，summary_state：19---处理中
     * convert_md_state：2，summary_state：10---待处理
     * convert_md_state：1，summary_state：11---处理成功
     */
    @JsonIgnore
    public boolean isProcessing() {
      // https://docs.iwhalecloud.com/doi/cjtgmr/docchain-doc-space/interface-manual/file-proc-api
      // 和 DocChain 文档空间页面判断处理失败的逻辑保持一致
      return ("9".equals(convertMdState) && "19".equals(summaryState)) || ("2".equals(convertMdState) && "10".equals(summaryState));
    }

    /**
     * 是否处理成功
     */
    @JsonIgnore
    public boolean isSuccess() {
      return "1".equals(splitMdState);
    }
  }
}
