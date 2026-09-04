package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordItemDto;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsFeedBackRatioDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsComprehensiveDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsContributorDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsHotQuestionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsOverviewDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeOpsQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeOpsMapper;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.impl.KnowledgeQuestionVectorHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowlegeOpsService;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库运营服务实现类
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Service
@RequiredArgsConstructor
public class KnowledgeOpsServiceImpl implements IKnowlegeOpsService {

  private final KnowledgeOpsMapper knowledgeOpsMapper;
  private final AttrSpecCache attrSpecCache;
  private final KnowledgeQuestionVectorHelper knowledgeQuestionVectorHelper;

  @Override
  public KnowledgeOpsOverviewDTO queryKnowledgeOpsOverview(KnowledgeOpsQueryParams params) {
    return knowledgeOpsMapper.selectKnowledgeOpsOverview(params);
  }

  @Override
  public PageInfo<KnowledgeReferenceDTO> queryKnowledgeReferenceRank(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    // noinspection resource
    PageInfo<KnowledgeReferenceDTO> pageInfo = knowledgeOpsMapper.selectKnowledgeReference(params, rowBounds).toPageInfo();
    return fillRankNo(pageInfo, KnowledgeReferenceDTO::setRankNo);
  }

  @Override
  public PageInfo<DocumentReferenceDTO> queryDocumentReferenceRank(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    // noinspection resource
    PageInfo<DocumentReferenceDTO> pageInfo = knowledgeOpsMapper.selectDocumentReference(params, rowBounds).toPageInfo();
    return fillRankNo(pageInfo, DocumentReferenceDTO::setRankNo);
  }

  @Override
  public PageInfo<KnowledgeOpsHotQuestionDTO> queryHotQuestionRank(KnowledgeOpsQueryParams params) {
    if (StringUtils.isEmpty(params.getTimeType())) {
      params.setTimeType("day");
    }
    applyTimeRangeByType(params, Set.of("day", "month", "year"), "高频议题统计");
    RowBounds rowBounds = params.buildRowBounds();
    PageInfo<KnowledgeOpsHotQuestionDTO> pageInfo;
    // 检查Elasticsearch和向量化模型是否可用
    if (knowledgeQuestionVectorHelper.isQuestionEmbeddingAvailable()) {
      // noinspection resource
      pageInfo = knowledgeOpsMapper.selectHotQuestionRankByStandarQuestion(params, rowBounds).toPageInfo();
    }
    else {
      // noinspection resource
      pageInfo = knowledgeOpsMapper.selectHotQuestionRank(params, rowBounds)
        .toPageInfo();
    }
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    // 为每个结果生成一个随机的 UUID 作为临时 ID
    pageInfo.getList().forEach(hotQuestion -> {
      hotQuestion.setId(UUID.randomUUID().toString().replace("-", ""));
    });
    return pageInfo;
  }

  @Override
  public List<BtDcQaRecordItemDto> queryDislikeReasonStats(KnowledgeOpsQueryParams params) {
    if (StringUtils.isEmpty(params.getTimeType())) {
      params.setTimeType("week");
    }
    applyTimeRangeByType(params, Set.of("week", "month", "year"), "点踩原因分布");
    // 1. 查询反馈类型字典
    List<SimpleAttrDTO> attrList = attrSpecCache.get(-1L, "FEEDBACK_REASON_TYPE");
    Map<String, String> codeToName = attrList == null ? Collections.emptyMap()
      : attrList.stream()
        .filter(a -> a.getAttrValueName() != null)
        .collect(Collectors.toMap(SimpleAttrDTO::getAttrValue, SimpleAttrDTO::getAttrValueName, (a, b) -> a));

    // 2. 查询所有点踩记录的 feedback_type 原始字符串列表
    List<String> rawList = knowledgeOpsMapper.selectDislikeRawFeedbackTypes(params);

    // 3. 拆分逗号分隔的 feedback_type，统计每个编码出现次数
    Map<String, Long> countMap = new LinkedHashMap<>();
    // 先初始化所有字典类型为0
    codeToName.keySet().forEach(code -> countMap.put(code, 0L));
    // 统计原始字符串列表中的编码出现次数
    if (rawList != null && !rawList.isEmpty()) {
      for (String raw : rawList) {
        if (raw == null || raw.trim().isEmpty()) {
          continue;
        }
        for (String code : raw.split(",")) {
          String trimmed = code.trim();
          if (!trimmed.isEmpty()) {
            countMap.merge(trimmed, 1L, Long::sum);
          }
        }
      }
    }

    // 4. 计算总数，按计数降序排列，计算每种原因的占比
    return computeDislikeReasonRates(countMap, codeToName);
  }

  /**
   * 计算点踩原因占比
   */
  private List<BtDcQaRecordItemDto> computeDislikeReasonRates(Map<String, Long> countMap,
    Map<String, String> codeToName) {
    long total = countMap.values().stream().mapToLong(Long::longValue).sum();
    BigDecimal totalDecimal = BigDecimal.valueOf(total);
    List<BtDcQaRecordItemDto> result = new ArrayList<>();
    countMap.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).forEach(entry -> {
      // 使用字典名称，无对应字典时回退显示编码
      String displayName = codeToName.getOrDefault(entry.getKey(), entry.getKey());
      BigDecimal rate = total == 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(entry.getValue()).divide(totalDecimal, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
      BtDcQaRecordItemDto dto = new BtDcQaRecordItemDto();
      dto.setName(displayName);
      dto.setValue(rate.toPlainString());
      result.add(dto);
    });
    return result;
  }

  @Override
  public KnowledgeOpsFeedBackRatioDTO queryFeedbackRatio(KnowledgeOpsQueryParams params) {
    if (StringUtils.isEmpty(params.getTimeType())) {
      params.setTimeType("week");
    }
    applyTimeRangeByType(params, Set.of("week", "month", "year"), "点赞点踩比例");
    KnowledgeOpsFeedBackRatioDTO result = knowledgeOpsMapper.selectFeedbackRatio(params);
    if (result == null) {
      result = new KnowledgeOpsFeedBackRatioDTO();
    }
    long likes = result.getLikeCount() == null ? 0L : result.getLikeCount();
    long dislikes = result.getDislikeCount() == null ? 0L : result.getDislikeCount();
    long total = likes + dislikes;
    if (total == 0L) {
      result.setLikeRate(BigDecimal.ZERO);
      result.setDislikeRate(BigDecimal.ZERO);
      return result;
    }
    BigDecimal totalValue = BigDecimal.valueOf(total);
    // 计算点赞占比
    BigDecimal likeRate = BigDecimal.valueOf(likes)
      .divide(totalValue, 4, RoundingMode.HALF_UP)
      .multiply(BigDecimal.valueOf(100))
      .setScale(2, RoundingMode.HALF_UP);
    // 计算点踩占比
    BigDecimal dislikeRate = BigDecimal.valueOf(dislikes)
      .divide(totalValue, 4, RoundingMode.HALF_UP)
      .multiply(BigDecimal.valueOf(100))
      .setScale(2, RoundingMode.HALF_UP);
    result.setLikeRate(likeRate);
    result.setDislikeRate(dislikeRate);
    return result;
  }

  @Override
  public PageInfo<KnowledgeOpsContributorDTO> queryDocContributorRank(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    Integer total = knowledgeOpsMapper.findDocTotal(params.getTenantId());
    // noinspection resource
    PageInfo<KnowledgeOpsContributorDTO> pageInfo = knowledgeOpsMapper.selectDocContributorRank(params, rowBounds).toPageInfo();
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    fillRankNo(pageInfo, KnowledgeOpsContributorDTO::setRankNo);
    fillLevelLabel(pageInfo, false);
    fillContributeRate(pageInfo, false, total);
    return pageInfo;
  }

  @Override
  public List<BtDcQaRecordItemDto> queryDocContributeDailyStats(KnowledgeOpsQueryParams params) {
    if (StringUtils.isEmpty(params.getTimeType())) {
      params.setTimeType("day");
    }
    applyTimeRangeByType(params, Set.of("day", "week", "month"), "文档贡献统计");
    List<BtDcQaRecordItemDto> rawList = knowledgeOpsMapper.selectDocContributeDailyStats(params);
    // 补全无数据的时间槽，保证折线图 X 轴连续
    return fillMissingTimeSlots(rawList, params.getTimeType(), params.getStartTime(), params.getEndTime());
  }

  @Override
  public PageInfo<KnowledgeOpsContributorDTO> queryUserQuestionRank(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    Integer total = knowledgeOpsMapper.findUserQuestionTotal(params.getTenantId());
    // noinspection resource
    PageInfo<KnowledgeOpsContributorDTO> pageInfo = knowledgeOpsMapper.selectUserQuestionRank(params, rowBounds).toPageInfo();
    fillRankNo(pageInfo, KnowledgeOpsContributorDTO::setRankNo);
    fillLevelLabel(pageInfo, true);
    fillContributeRate(pageInfo, true, total);
    return pageInfo;
  }

  /**
   * 填充每条数据的占比
   */
  private void fillContributeRate(PageInfo<KnowledgeOpsContributorDTO> pageInfo, boolean useQuestionCount, Integer total) {
    if (pageInfo == null || CollectionUtils.isEmpty(pageInfo.getList()) || total == null || total == 0) {
      return;
    }
    BigDecimal totalDecimal = BigDecimal.valueOf(total);
    for (KnowledgeOpsContributorDTO dto : pageInfo.getList()) {
      long count = useQuestionCount ? safeLong(dto.getQuestionCount()) : safeLong(dto.getUploadCount());
      BigDecimal rate = BigDecimal.valueOf(count)
        .divide(totalDecimal, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(2, RoundingMode.HALF_UP);
      if (useQuestionCount) {
        dto.setQuestionRate(rate);
      } else {
        dto.setUploadRate(rate);
      }
    }
  }

  @Override
  public List<BtDcQaRecordItemDto> queryUserQuestionDailyStats(KnowledgeOpsQueryParams params) {
    if (StringUtils.isEmpty(params.getTimeType())) {
      params.setTimeType("day");
    }
    applyTimeRangeByType(params, Set.of("day", "week", "month"), "用户提问统计");
    List<BtDcQaRecordItemDto> rawList = knowledgeOpsMapper.selectUserQuestionDailyStats(params);
    // 补全无数据的时间槽，保证折线图 X 轴连续
    return fillMissingTimeSlots(rawList, params.getTimeType(), params.getStartTime(), params.getEndTime());
  }

  @Override
  public PageInfo<KnowledgeOpsComprehensiveDTO> queryComprehensiveRank(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    // noinspection resource
    PageInfo<KnowledgeOpsComprehensiveDTO> pageInfo = knowledgeOpsMapper.selectComprehensiveRank(params, rowBounds)
      .toPageInfo();
    fillRankNo(pageInfo, KnowledgeOpsComprehensiveDTO::setRankNo);
    return pageInfo;
  }

  /**
   * 为分页结果中的每个元素设置排名编号
   */
  private <T> PageInfo<T> fillRankNo(PageInfo<T> pageInfo, BiConsumer<T, Integer> rankSetter) {
    if (pageInfo == null || CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    int pageNum = pageInfo.getPageNum();
    int pageSize = pageInfo.getPageSize();
    int startIndex = Math.max(pageNum - 1, 0) * Math.max(pageSize, 0);
    List<T> list = pageInfo.getList();
    for (int i = 0; i < list.size(); i++) {
      rankSetter.accept(list.get(i), startIndex + i + 1);
    }
    return pageInfo;
  }

  /**
   * 为贡献者列表填充贡献者等级标签
   */
  private void fillLevelLabel(PageInfo<KnowledgeOpsContributorDTO> pageInfo, boolean useQuestionCount) {
    if (pageInfo == null || CollectionUtils.isEmpty(pageInfo.getList())) {
      return;
    }
    for (KnowledgeOpsContributorDTO dto : pageInfo.getList()) {
      long count = useQuestionCount ? safeLong(dto.getQuestionCount()) : safeLong(dto.getUploadCount());
      dto.setLevelLabel(resolveContributorLevelLabel(count));
    }
  }

  private long safeLong(Long value) {
    return value == null ? 0L : value;
  }

  /**
   * 根据贡献次数，解析出对应的等级标签。
   */
  private String resolveContributorLevelLabel(long count) {
    if (count <= 0) {
      return "入门小白";
    }
    if (count <= 50) {
      return "初级用户";
    }
    if (count <= 200) {
      return "中级用户";
    }
    if (count <= 500) {
      return "高级用户";
    }
    if (count <= 1000) {
      return "资深专家";
    }
    return "权威专家";
  }

  /**
   * 根据 timeType 计算时间范围，并覆盖 startTime/endTime。
   *
   * @param params 查询参数
   * @param allowedTypes 允许的时间维度
   * @param sceneName 场景名称
   */
  private void applyTimeRangeByType(KnowledgeOpsQueryParams params, Set<String> allowedTypes, String sceneName) {
    if (params == null || params.getTimeType() == null || params.getTimeType().trim().isEmpty()) {
      return;
    }
    String timeType = params.getTimeType().trim().toLowerCase();
    if (!allowedTypes.contains(timeType)) {
      throw new BssException(sceneName + "仅支持时间维度: " + String.join("/", allowedTypes));
    }
    TimeRange timeRange = buildTimeRange(timeType);
    params.setStartTime(timeRange.startTime());
    params.setEndTime(timeRange.endTime());
  }

  /**
   * 根据时间维度构造时间范围。
   */
  private TimeRange buildTimeRange(String normalizedType) {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = LocalDate.now();
    LocalDateTime start;
    switch (normalizedType) {
      case "day" -> start = LocalDateTime.of(today, LocalTime.MIN);
      case "week" -> {
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        start = LocalDateTime.of(weekStartDate, LocalTime.MIN);
      }
      case "month" -> {
        LocalDate monthStartDate = today.withDayOfMonth(1);
        start = LocalDateTime.of(monthStartDate, LocalTime.MIN);
      }
      case "year" -> {
        LocalDate yearStartDate = today.withDayOfYear(1);
        start = LocalDateTime.of(yearStartDate, LocalTime.MIN);
      }
      default -> throw new BssException("不支持的时间维度: " + normalizedType);
    }
    ZoneId zoneId = ZoneId.systemDefault();
    return new TimeRange(Date.from(start.atZone(zoneId).toInstant()), Date.from(now.atZone(zoneId).toInstant()));
  }

  /**
   * 补全折线图时间槽，将无数据的时间点填充 value="0"，保证 X 轴连续。
   *
   * @param rawList   原始查询结果（只含有数据的时间点）
   * @param timeType  时间维度（day/week/month/year）
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 补全后的列表（按时间升序，与 SQL name 字段格式一致）
   */
  private List<BtDcQaRecordItemDto> fillMissingTimeSlots(List<BtDcQaRecordItemDto> rawList, String timeType,
    Date startTime, Date endTime) {
    if (timeType == null || startTime == null || endTime == null) {
      return rawList != null ? rawList : Collections.emptyList();
    }
    List<String> slots = buildTimeSlots(timeType, startTime, endTime);
    if (slots.isEmpty()) {
      return rawList != null ? rawList : Collections.emptyList();
    }
    // 将原始数据转为 Map（name -> value），便于 O(1) 查找
    Map<String, String> dataMap = rawList == null
      ? Collections.emptyMap()
      : rawList.stream()
        .collect(Collectors.toMap(BtDcQaRecordItemDto::getName, BtDcQaRecordItemDto::getValue, (a, b) -> a));
    // 按槽位顺序输出，缺失的槽填充 "0"
    return slots.stream().map(slot -> {
      BtDcQaRecordItemDto dto = new BtDcQaRecordItemDto();
      dto.setName(slot);
      dto.setValue(dataMap.getOrDefault(slot, "0"));
      return dto;
    }).collect(Collectors.toList());
  }

  /**
   * 根据时间维度生成全量时间槽标签，与 SQL name 字段格式保持一致：
   * <ul>
   *   <li>day  → HH:00（"00:00" ~ "23:00"，固定 24 个）</li>
   *   <li>week/month → yyyyMMdd（startTime ~ endTime 每天一个）</li>
   *   <li>year → yyyyMM（startTime ~ endTime 每月一个）</li>
   * </ul>
   *
   * @param timeType  时间维度
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 时间槽标签列表
   */
  private List<String> buildTimeSlots(String timeType, Date startTime, Date endTime) {
    ZoneId zone = ZoneId.systemDefault();
    LocalDate startDate = startTime.toInstant().atZone(zone).toLocalDate();
    LocalDate endDate = endTime.toInstant().atZone(zone).toLocalDate();
    switch (timeType) {
      case "day" -> {
        // 固定 24 小时槽，格式与 SQL 的 DATE_FORMAT('%H:00') 一致
        return IntStream.range(0, 24)
          .mapToObj(h -> String.format("%02d:00", h))
          .collect(Collectors.toList());
      }
      case "week", "month" -> {
        // 按天枚举，格式 yyyyMMdd
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<String> days = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
          days.add(cur.format(fmt));
          cur = cur.plusDays(1);
        }
        return days;
      }
      case "year" -> {
        // 按月枚举，格式 yyyyMM
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMM");
        List<String> months = new ArrayList<>();
        YearMonth cur = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        while (!cur.isAfter(end)) {
          months.add(cur.format(fmt));
          cur = cur.plusMonths(1);
        }
        return months;
      }
      default -> {
        return Collections.emptyList();
      }
    }
  }

  @Override
  public PageInfo<BtDcQaRecordDTO> queryQaRecordPage(KnowledgeOpsQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    // noinspection resource
    PageInfo<BtDcQaRecordDTO> pageInfo = knowledgeOpsMapper.selectDcQaRecordPage(params, rowBounds).toPageInfo();
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    for (BtDcQaRecordDTO dcQaRecord : pageInfo.getList()) {
      if (StringUtils.isNotEmpty(dcQaRecord.getFeedbackType())) {
        String[] split = StringUtils.split(dcQaRecord.getFeedbackType(), ",");
        List<SimpleAttrDTO> attrList = attrSpecCache.get(-1L, "FEEDBACK_REASON_TYPE");
        Map<String, String> codeToName = attrList == null ? Collections.emptyMap()
          : attrList.stream()
            .filter(a -> a.getAttrValueName() != null)
            .collect(Collectors.toMap(SimpleAttrDTO::getAttrValue, SimpleAttrDTO::getAttrValueName, (a, b) -> a));
        dcQaRecord.setFeedbackType(Arrays.stream(split)
          .map(code -> codeToName.getOrDefault(code, code))
          .collect(Collectors.joining(", ")));
      }
    }
    return pageInfo;
  }

  @Override
  @Transactional
  public void updateFeedbackOperateState(Long qaId, String operateState) {
    knowledgeOpsMapper.updateFeedbackOperateState(qaId, operateState);
  }

  /**
   * 时间范围对象。
   *
   * @param startTime 开始时间
   * @param endTime 结束时间
   */
  private record TimeRange(Date startTime, Date endTime) {
  }
}
