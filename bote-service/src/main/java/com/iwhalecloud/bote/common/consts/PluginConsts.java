package com.iwhalecloud.bote.common.consts;

/**
 * 插件常量
 *
 * @author qian.sisheng
 * @since 2025-04-15
 */
public final class PluginConsts {
  private PluginConsts() {
  }

  /** 插件类型 - MCP */
  public static final String PLUGIN_TYPE_MCP = "mcp";
  /** 插件类型 - 工具 */
  public static final String PLUGIN_TYPE_TOOL = "tool";

  /** 自动发现插件工具方式 - 无 */
  public static final String DISCOVERY_TYPE_NONE = "none";
  /** 自动发现插件工具方式 - SDK */
  public static final String DISCOVERY_TYPE_SDK = "sdk";

  /** 网关类型 - http */
  public static final String GATEWAY_TYPE_HTTP = "http";
  /** 网关类型 - higress */
  public static final String GATEWAY_TYPE_HIGRESS = "higress";


  /** 插件编码：钉钉发送通知 */
  public static final String PLUGIN_CODE_DING_DING_BOT_NOTICE = "dingDingBotNotice";
  /** 插件编码：钉钉发送工作通知 */
  public static final String PLUGIN_CODE_DING_DING_WORK_NOTICE = "dingDingWorkNotice";
  /** 插件编码：企业微信发送机器人通知 */
  public static final String PLUGIN_CODE_WE_CHAT_BOT_NOTICE = "weChatBotNotice";
  /** 插件编码：文件读取 */
  public static final String PLUGIN_CODE_FILE_READ = "fileRead";
  /** 插件编码：聚智DICT文件解析 */
  public static final String PLUGIN_CODE_JUZHI_DICT_FILE_PARSE = "juzhiDictFileParse";

  /** 插件编码：Base64编码 */
  public static final String PLUGIN_CODE_BASE_64_ENCODER = "base64Encoder";
  /** 插件编码：Base64解码 */
  public static final String PLUGIN_CODE_BASE_64_DECODER = "base64Decoder";
  /** 插件编码：图片转Base64 */
  public static final String PLUGIN_CODE_IMAGE_TO_BASE_64 = "imageToBase64";
  /** 插件编码：AES加密 */
  public static final String PLUGIN_CODE_AES_ENCRYPT = "aesEncrypt";
  /** 插件编码：网页内容提取插件 */
  public static final String PLUGIN_CODE_WEB_PAGE_FETCH = "webPageFetch";
  /** 插件编码：图片识别插件 */
  public static final String PLUGIN_CODE_IMAGE_ANALYSIS = "imageAnalysis";
  /** 插件编码：nl2sql插件 */
  public static final String PLUGIN_CODE_IMAGE_NL2SQL = "nl2sql";
  /** 插件编码：AES解密 */
  public static final String PLUGIN_CODE_AES_DECRYPT = "aesDecrypt";
  /** 插件编码：语音转文字 */
  public static final String PLUGIN_CODE_VOICE_TO_TEXT = "voiceToText";
  /** 插件编码：数据转换 */
  public static final String ATTR_DATA_TRANSFORM = "attrDataTransform";
  /** 插件编码：生成ECharts图表 */
  public static final String ECHARTS_GENERATOR = "echartsGenerator";
  /** 插件编码：百度翻译 */
  public static final String PLUGIN_CODE_BAIDU_TRANSLATE = "baiduTranslate";
  /** 插件编码：抖音热门话题 */
  public static final String PLUGIN_CODE_DOUYIN_HOT_TOP = "douyinHotList";
  /** 插件编码：抖音评论 */
  public static final String PLUGIN_CODE_DOUYIN_COMMENT = "douyinComment";
  /** 插件编码：抖音视频解析 */
  public static final String PLUGIN_CODE_DOU_YIN_VIDEO_ANALYSIS = "douyinVideoAnalysis";
  /** 插件编码：多平台视频下载 */
  public static final String PLUGIN_CODE_PLATFORM_VIDEO_DOWNLOAD = "platformVideoDownload";
  /** 插件编码：小红书搜索-综合 */
  public static final String PLUGIN_CODE_RED_NOTE_SEARCH_COMMON = "redNoteSearchCommon";
  /** 插件编码：小红书搜索-爆款 */
  public static final String PLUGIN_CODE_RED_NOTE_SEARCH_POPULAR = "redNoteSearchPopular";
  /** 插件编码：微博热搜 */
  public static final String PLUGIN_CODE_WEI_BO_HOT_SEARCH = "weiboHotSearch";
  /** 插件编码：今日头条热榜 */
  public static final String PLUGIN_CODE_TOU_TIAO_HOT_LIST = "touTiaoHotList";
  /** 插件编码：知乎热搜 */
  public static final String PLUGIN_CODE_ZHIHU_HOT_SEARCH = "zhiHuHotSearch";
  /** 插件编码：哔哩哔哩热门视频列表 */
  public static final String PLUGIN_CODE_BILIBILI_HOT_LIST = "bilibiliHotList";
  /** 插件编码：哔哩哔哩视频搜索 */
  public static final String PLUGIN_CODE_BILIBILI_VIDEO_SEARCH = "bilibiliVideoSearch";
  /** 插件编码：小红书笔记评论 */
  public static final String PLUGIN_CODE_RED_NOTE_COMMENT = "redNoteComment";
  /** 插件编码：飞书多维表格 */
  public static final String PLUGIN_CODE_CREATE_LARK_BITABLE = "larkCreateBitable";
  /** 插件编码：飞书多维表格数据表 */
  public static final String PLUGIN_CODE_LARK_LIST_TABLES = "larkListTables";
  /** 插件编码：飞书多维表格字段 */
  public static final String PLUGIN_CODE_LARK_TABLE_FIELDS = "larkTableFields";
  /** 插件编码：飞书多维表格字段添加 */
  public static final String PLUGIN_CODE_LARK_ADD_FIELDS = "larkAddField";
  /** 插件编码：飞书多维表格记录查询 */
  public static final String PLUGIN_CODE_LARK_SEARCH_RECORD = "larkSearchRecord";
  /** 插件编码：飞书删除数据表 */
  public static final String PLUGIN_CODE_LARK_DELETE_TABLE = "larkDeleteTable";
  /** 插件编码：飞书批量添加多维表格记录 */
  public static final String PLUGIN_CODE_LARK_ADD_RECORDS = "larkAddRecords";
  /** 插件编码：飞书更新表格字段 */
  public static final String PLUGIN_CODE_LARK_UPDATE_FIELD = "larkUpdateField";
  /** 插件编码：飞书批量更新数据表的记录 */
  public static final String PLUGIN_CODE_LARK_UPDATE_RECORDS = "larkUpdateRecords";
  /** 插件编码：飞书搜索多维表格类型的文档 */
  public static final String PLUGIN_CODE_LARK_SEARCH_DOC = "larkSearchDoc";
  /** 插件编码：飞书批量删除记录 */
  public static final String PLUGIN_CODE_LARK_DELETE_RECORDS = "larkDeleteRecords";
  /** 插件编码：飞书删除表格字段 */
  public static final String PLUGIN_CODE_LARK_DELETE_FIELD = "larkDeleteField";
  /** 插件编码：飞书获取多维表格元数据 */
  public static final String PLUGIN_CODE_LARK_GET_BITABLE_META_DATA = "larkGetMetadata";
  /** 插件编码：飞书创建多维表格数据表 */
  public static final String PLUGIN_CODE_LARK_CREATE_TABLE = "larkCreateTable";

  /** 消息类型：文本 */
  public static final String MESSAGE_TYPE_TEXT = "text";
  /** 消息类型：卡片 */
  public static final String MESSAGE_TYPE_ACTION_CARD = "actionCard";
  /** 消息类型：markdown */
  public static final String MESSAGE_TYPE_MARKDOWN = "markdown";
  /** 消息类型：图文 */
  public static final String MESSAGE_TYPE_NEWS = "news";
  /** 消息类型：模板卡片 */
  public static final String MESSAGE_TYPE_TEMPLATE_CARD = "templateCard";
  /** 消息类型：图片 */
  public static final String MESSAGE_TYPE_IMAGE = "image";
  /** 消息类型：音频 */
  public static final String MESSAGE_TYPE_VOICE = "voice";
  /** 消息类型：文件 */
  public static final String MESSAGE_TYPE_FILE = "file";
  /** 消息类型：链接 */
  public static final String MESSAGE_TYPE_LINK = "link";
  /** 消息类型：OA */
  public static final String MESSAGE_TYPE_OA = "oa";
  /** 消息类型：feedCard */
  public static final String MESSAGE_TYPE_FEED_CARD = "feedCard";

  /** 消息类型参数编码 */
  public static final String MESSAGE_TYPE = "msgtype";
  /** 插件状态：启用 */
  public static final String PLUGIN_STATUS_ENABLE = "1";
  /** 插件编码：知识库文件读取 */
  public static final String PLUGIN_CODE_KNOWLEDGE_FILE_READ = "knowledgeFileRead";

  /** 插件编码：图生视频 */
  public static final String PLUGIN_CODE_DOUBAO_IMAGE_2_VIDEO = "douBaoImage2video";
  /** 插件编码：文生视频 */
  public static final String PLUGIN_CODE_DOUBAO_TEXT_2_VIDEO = "douBaoText2video";
  /** 插件编码：文生图片 */
  public static final String PLUGIN_CODE_DOUBAO_TEXT_2_IMAGE = "douBaoText2image";

  /** 插件编码：视频解析 */
  public static final String PLUGIN_CODE_VIDEO_ANALYSIS = "videoAnalysis";
  /** 插件编码：音频解析 */
  public static final String PLUGIN_CODE_AUDIO_ANALYSIS = "audioAnalysis";

  /** 插件编码：Excel转数据 */
  public static final String PLUGIN_CODE_EXCEL_TO_DATA = "excelToData";
  /** 插件编码：链接提取 */
  public static final String PLUGIN_CODE_LINK_EXTRACTOR = "linkExtractor";
  /** 插件编码：视频转音频 */
  public static final String PLUGIN_CODE_VIDEO_TO_AUDIO = "videoToAudio";
  /** 插件编码：微信公众号获取稳定TOKEN  */
  public static final String PLUGIN_CODE_WE_CHAT_MP_GET_STABLE_TOKEN = "weChatMpGetStableToken";
  /** 插件编码：微信公众号上传素材  */
  public static final String PLUGIN_CODE_WE_CHAT_MP_ADD_MATERIAL = "weChatMpAddMaterial";
  /** 插件编码：微信公众号新增图文消息 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_ADD_GRAPHIC_MSG = "weChatMpAddGraphicMsg";
  /** 插件编码：微信公众号新增图片消息 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_ADD_PICTURE_MSG = "weChatMpAddPictureMsg";
  /** 插件编码：微信公众号发布草稿 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_PUBLISH_DRAFT = "weChatMpPublishDraft";
  /** 插件编码：微信公众号删除草稿 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_DEL_DRAFT = "weChatMpDelDraft";
  /** 插件编码：微信公众号删除素材 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_DEL_MATERIAL = "weChatMpDelMaterial";
  /** 插件编码：微信公众号删除发布 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_DEL_PUBLISH = "weChatMpDelPublish";
  /** 插件编码：微信公众号查询发布状态 */
  public static final String PLUGIN_CODE_WE_CHAT_MP_QRY_PUBLISH_STATE = "weChatMpQryPublishState";
  /** 插件编码：微信公众号查询发布信息 */
  public static final String PLUGN_CODE_WE_CHAT_MP_QRY_PUBLISH_INFO = "weChatMpQryPublishInfo";
  /** 插件编码：根据地址保存图片 */
  public static final String PLUGIN_CODE_SAVE_FILE_BY_URL = "saveFileByUrl";
  /** 插件编码：豆包语音合成 */
  public static final String PLUGIN_CODE_DOUBAO_VOICE = "douBaoVoice";
  /** 插件编码：根据地址解析内容 */
  public static final String PLUGIN_CODE_PARSE_FILE_BY_URL = "parseFileByUrl";
  /** 插件编码：数组转Map */
  public static final String PLUGIN_CODE_LIST_TO_MAP = "listToMap";
  /** 插件编码：数组筛选器 */
  public static final String PLUGIN_CODE_ARRAY_FILTER = "arrayFilter";
  /** 插件编码：Map转数组 */
  public static final String PLUGIN_CODE_MAP_TO_ARRAY = "mapToArray";
  /** 插件编码：时间格式转换 */
  public static final String PLUGIN_CODE_TIME_FORMAT_CONVERTER = "timeFormatConverter";
  /** 插件编码：SMTP邮件发送 */
  public static final String PLUGIN_CODE_SMTP_EMAIL = "smtpEmail";
  /** 插件编码：获取指定时区时间 */
  public static final String PLUGIN_CODE_GET_TIME_ZONE = "getTimeZone";
  /** 插件编码：时区转换工具 */
  public static final String PLUGIN_CODE_TIME_ZONE_CONVERTER = "timeZoneConverter";
  /** 插件编码：SHA哈希算法加密 */
  public static final String PLUGIN_CODE_SHA_HASH = "shaHash";
  /** 插件编码：SHA哈希算法加密验证 */
  public static final String PLUGIN_CODE_SHA_HASH_CHECK = "shaHashCheck";
  /** 插件编码：MD5哈希算法加密 */
  public static final String PLUGIN_CODE_MD5_HASH = "md5Hash";
  /** 插件编码：MD5哈希算法加密验证 */
  public static final String PLUGIN_CODE_MD5_HASH_CHECK = "md5HashCheck";
  /** 插件编码：数字验证码生成 */
  public static final String PLUGIN_CODE_VERIFICATION_CODE_GENERATOR = "verificationCodeGenerator";
  /** 插件编码：字母验证码生成 */
  public static final String PLUGIN_CODE_LETTER_VERIFICATION_CODE_GENERATOR = "letterVerificationCodeGenerator";
  /** 插件编码：混合验证码生成 */
  public static final String PLUGIN_CODE_MIXED_VERIFICATION_CODE_GENERATOR = "mixedVerificationCodeGenerator";
  /** 插件编码：随机数生成 */
  public static final String PLUGIN_CODE_RANDOM_NUMBER_GENERATOR = "randomNumberGenerator";
  /** 插件编码：高德地理编码 */
  public static final String PLUGIN_CODE_GAODE_GEOCODE = "gaodeGeocode";
  /** 插件编码：文生图提示词优化 */
  public static final String PLUGIN_CODE_PROMPT_OPTIMIZATION = "promptOptimization";
  /** 插件编码：图片格式转换 */
  public static final String PLUGIN_CODE_IMAGE_CONVERT = "imageConvert";
  /** 插件编码：图片压缩 */
  public static final String PLUGIN_CODE_IMAGE_COMPRESS = "imageCompress";
  /** 插件编码：查快递 */
  public static final String PLUGIN_CODE_KUAIDI_QUERY = "kuaidiQuery";
  /** 插件编码：图片水印 */
  public static final String PLUGIN_CODE_IMAGE_WATERMARK = "imageWatermark";
  /** 插件编码：查快递Pro（智能识别快递公司） */
  public static final String PLUGIN_CODE_KUAIDI_QUERY_PRO = "kuaidiQueryPro";
  /** 插件编码：图片识别 */
  public static final String PLUGIN_CODE_IMAGE_IDENTIFY = "imageIdentify";
  /** 插件编码：身份证识别 */
  public static final String PLUGIN_CODE_ID_CARD_IDENTIFY = "idCard";
  /** 插件编码：户口本识别 */
  public static final String PLUGIN_CODE_HOUSEHOLD_REGISTER = "householdRegister";
  /** 插件编码：营业执照识别 */
  public static final String PLUGIN_CODE_BUSINESS_LICENSE = "businessLicense";
  /** 插件编码：银行卡识别 */
  public static final String PLUGIN_CODE_BANK_CARD = "bankCard";
  /** 插件编码：护照识别 */
  public static final String PLUGIN_CODE_PASSPORT = "passport";
  /** 插件编码：毕业证识别 */
  public static final String PLUGIN_CODE_DIPLOMA = "diploma";
  /** 插件编码：学位证识别 */
  public static final String PLUGIN_CODE_DEGREE_CERTIFICATE = "degreeCertificate";
  /** 插件编码：驾驶证识别 */
  public static final String PLUGIN_CODE_DRIVERS_LICENSE = "driversLicense";
  /** 插件编码：通用护照识别 */
  public static final String PLUGIN_CODE_COMMON_PASSPORT = "commonPassport";
  /** 插件编码：发票识别 */
  public static final String PLUGIN_CODE_INVOICE = "invoice";
  /** 插件编码：支付凭证识别 */
  public static final String PLUGIN_CODE_PAYMENT_VOUCHER = "paymentVoucher";
  /** 插件编码：组织机构代码认证识别 */
  public static final String PLUGIN_CODE_ORG_CODE = "orgCode";
  /** 插件编码：文本转语音 */
  public static final String PLUGIN_CODE_WORD_TO_AUDIO = "wordToAudio";
  /** 插件编码：二维码生成 */
  public static final String PLUGIN_CODE_QR_CODE_GENERATE = "qrCodeGenerate";
  /** 插件编码：文档水印 */
  public static final String PLUGIN_CODE_DOCUMENT_WATERMARK = "documentWatermark";
  /** 插件编码：网易新闻热榜 */
  public static final String PLUGIN_CODE_NET_EASE_NEWS_HOT = "netEaseNewsHot";
  /** 插件编码：表格提取 */
  public static final String PLUGIN_CODE_EXTRACT_TABLE = "extractTable";

  /** 用户代理 */
  public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";
}
