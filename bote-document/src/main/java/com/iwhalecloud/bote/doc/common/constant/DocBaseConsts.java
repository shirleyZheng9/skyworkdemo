package com.iwhalecloud.bote.doc.common.constant;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 文档中心常量类
 *
 * @author 杨然
 * @since 2025-08-12
 */
public final class DocBaseConsts {
  /** 请求地址前缀 */
  public static final String API_PREFIX = "bote/";

  /**
   * 默认的系统内置用户
   */
  public static final Long DEFAULT_SYSTEM_USER_ID = 1L;

  /** 数据状态: 有效 */
  public static final String STATUS_CD_VALID = "00A";
  /** 数据状态: 无效 */
  public static final String STATUS_CD_INVALID = "00X";
  /** 数据状态: 禁用 */
  public static final String STATUS_CD_DISABLED = "00D";
  /** 数据状态: 待审批 */
  public static final String STATUS_CD_PING_APPROVAL = "00F";

  /**
   * 数据状态：回收站
   */
  public static final String STATUS_CD_RUBBISH = "00R";

  /**
   * 租户ID的参数名
   */
  public static final String PARAM_TENANT_ID = "tenantId";

  /**
   * 租住ID的请求头
   */
  public static final String TENANT_HEADER = "tenantId";

  /**
   * 企业空间ID的参数名
   */
  public static final String PARAM_SPACE_ID = "spaceId";

  /**
   * 企业空间ID的请求头
   */
  public static final String SPACE_HEADER = "spaceId";

  /**
   * 文档中心文件上传的基础路径
   */
  public static final String UPLOAD_FILE_BASE_PATH = "dc";

  /**
   * .字符
   */
  public static final String DOT = ".";

  // ==================== 共享模块排序相关常量 ====================

  /** 排序字段：修改时间 */
  public static final String SORT_FIELD_MODIFY_TIME = "modifytime";
  /** 排序字段：共享时间 */
  public static final String SORT_FIELD_SHARE_TIME = "sharetime";
  /** 排序字段：创建时间 */
  public static final String SORT_FIELD_CREATED_TIME = "created_time";
  /** 排序字段：更新时间 */
  public static final String SORT_FIELD_UPDATED_TIME = "updated_time";
  /** 排序方向：升序 */
  public static final String SORT_ORDER_ASC = "asc";
  /** 排序方向：降序 */
  public static final String SORT_ORDER_DESC = "desc";
  /** 默认排序字段 */
  public static final String DEFAULT_SORT_FIELD = SORT_FIELD_CREATED_TIME;
  /** 默认排序方向 */
  public static final String DEFAULT_SORT_ORDER = SORT_ORDER_DESC;

  /**
   * 布尔值
   */
  public static final String TRUE = "T";
  public static final String FALSE = "F";

  // ==================== 收藏模块相关常量 ====================

  /** 平台级租户 ID */
  public static final Long PLATFORM_TENANT_ID = -1L;
  /** 默认页码 */
  public static final int DEFAULT_PAGE_NUM = 1;
  /** 默认每页大小 */
  public static final int DEFAULT_PAGE_SIZE = 20;

  /** 平台助手租户 ID */
  public static final Long COPILOT_TENANT_ID = 1L;

  /** 企业目录 ID */
  public static final Long BUSINESS_PAR_CATALOG_ID = -99L;

  /** 项目目录 ID */
  public static final Long BUSINESS_PAR_CATALOG_ID_PROJECT = -88L;

  /** 目录类型：知识库 */
  public static final String CATALOG_TYPE_KNOWLEDGE = "knowledge";
  /** 文件业务类型 - 知识文档 */
  public static final String FILE_BUSI_TYPE_DOCUMENT = "document";
  /** 默认目录节点父 ID */
  public static final Long DEFAULT_CATALOG_ITEM_PARENT_ID = -1L;
  /** 租户设置信息类型：知识库 */
  public static final String FUNC_TYPE_KNOWLEDGE = "knowledge";
  /** 默认 excel 工作簿名称 */
  public static final String DEFAULT_SHEET_NAME = "sheet1";

  public static final String DEFAULT_LIBRARY_ICON = "IcoLogoAirplayLinear";
  public static final String DEFAULT_LIBRARY_COLOR = "#00B1FF";


  // ================== 文档相关常量 ====================

  /**
   * 文档附件的下载路径，两个变量分别为文档ID和附件ID的Base58编码
   * 例：/bote/dc/document/attachment/files/docWDACvRoFomWkWvyT8JiGuD/A1NW6m1EQMtJX2DVLmqKaRikYX.png
   */
  public static final String DOCUMENT_ATTACHMENT_GET_PATH = "/" + API_PREFIX + "dc/document/attachment/files/{0}/{1}";

  /**
   * 文档初始化版本号
   */
  public static final Long DOCUMENT_INIT_REVISION = 1L;

  /**
   * word文档后缀
   */
  public static final String DOCUMENT_DOCX_EXTENSION = "docx";
  /**
   * excel文档后缀
   */
  public static final String DOCUMENT_EXCEL_EXTENSION = "xls";

  /**
   * 编辑动作-锁定
   */
  public static final String DOCUMENT_EDIT_ACTION_LOCK = "LOCK";
  /**
   * 编辑动作-解锁
   */
  public static final String DOCUMENT_EDIT_ACTION_UNLOCK = "UNLOCK";

  /**
   * 文档的最大搜索数据量
   */
  public static final Integer DOCUMENT_SEARCH_MAX_COUNT = 50;

  /** 知识库问答的看板时间 -今天 */
  public static final String TIME_TYPE_1D = "1D";
  /** 知识库问答的看板时间 -三天 */
  public static final String TIME_TYPE_3D = "3D";
  /** 知识库问答的看板时间 -一周 */
  public static final String TIME_TYPE_1W = "1W";
  /** 知识库问答的看板时间 -一月 */
  public static final String TIME_TYPE_1M = "1M";
  /** 知识库问答的看板时间 -一年 */
  public static final String TIME_TYPE_1Y = "1Y";
  /** 知识库检索：问答 */
  public static final String KNOWLEDGE_QA_TYPE_QA = "QA";
  /** 知识库检索：召回 */
  public static final String KNOWLEDGE_QA_TYPE_RECALL = "RECALL";

  /** 创建在线表格的默认数据 */
  public static final String DEFULT_WORK_BOOK_CONTENT = "{\"id\":\"gv1Hvi\",\"sheetOrder\":[\"xxLY3UDc4DFNHuuL6eBR8\"],\"name\":\"\",\"appVersion\":\"0.10.5\",\"locale\":\"zhCN\",\"styles\":{},\"sheets\":{\"xxLY3UDc4DFNHuuL6eBR8\":{\"id\":\"xxLY3UDc4DFNHuuL6eBR8\",\"name\":\"Sheet1\",\"tabColor\":\"\",\"hidden\":0,\"rowCount\":1000,\"columnCount\":20,\"zoomRatio\":1,\"freeze\":{\"xSplit\":0,\"ySplit\":0,\"startRow\":-1,\"startColumn\":-1},\"scrollTop\":0,\"scrollLeft\":0,\"defaultColumnWidth\":88,\"defaultRowHeight\":24,\"mergeData\":[],\"cellData\":{},\"rowData\":{},\"columnData\":{},\"showGridlines\":1,\"rowHeader\":{\"width\":46,\"hidden\":0},\"columnHeader\":{\"height\":20,\"hidden\":0},\"rightToLeft\":0}},\"resources\":[{\"name\":\"SHEET_RANGE_PROTECTION_PLUGIN\",\"data\":\"\"},{\"name\":\"SHEET_AuthzIoMockService_PLUGIN\",\"data\":\"{}\"},{\"name\":\"SHEET_WORKSHEET_PROTECTION_PLUGIN\",\"data\":\"{}\"},{\"name\":\"SHEET_WORKSHEET_PROTECTION_POINT_PLUGIN\",\"data\":\"{}\"},{\"name\":\"SHEET_DEFINED_NAME_PLUGIN\",\"data\":\"\"},{\"name\":\"SHEET_RANGE_THEME_MODEL_PLUGIN\",\"data\":\"{}\"}]}";
  /** 创建在线表格老数据默认数据 */
  public static final String DEFULT_NULL_CONTENT = "{}";

  /** ai门户 */
  public static final String AI_PORTAL = "portal";

  /** ai门户-查询模式 */
  public static final String AI_PORTAL_QUERY_MODE = "allData";

  /** 历史文档分组名称-今天 */
  public static final String DOC_HISTORY_GROUP_NAME_TODAY = "今天";

  /** 历史记录非「今天」时按自然日分组的日期键（与 DOC_HISTORY_GROUP_NAME_TODAY 对应） */
  public static final DateTimeFormatter DOC_HISTORY_OTHER_DAY_GROUP_KEY = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.CHINA);

  private DocBaseConsts() {
  }

}
