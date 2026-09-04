package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportJobStatusVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportPartVO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 技能广场大批量异步导出任务状态：先在 JVM 内存中维护 {@link Stored}，在 {@link #putInitial} 时向固定单线程池提交刷盘任务，
 * 按间隔休眠并刷脏数据到 Redis（{@link CacheFactory} + {@link ICacheClient}）；内存状态长时间无更新后该任务结束。
 * 轮询 {@link #getForUser} 优先读 Redis，保证多实例一致；仅当 Redis 尚无键时回退读本机内存。
 */
@Component
public class SkillSquareBulkExportCache {

  private static final Logger logger = LoggerFactory.getLogger(SkillSquareBulkExportCache.class);

  private static final long TTL_HOURS = 24;
  private static final long TTL_SECONDS = TTL_HOURS * 3600;

  private final ICacheClient cacheClient;

  /** userId -> 最近一次发起的导出 jobId（Redis，多实例一致） */
  private final ICacheClient userJobCacheClient;
  /** 本机内存中的任务快照（与 Redis 结构一致）；终态成功刷 Redis 后可移除以控制占用 */
  private final ConcurrentMap<Long, Stored> memoryByJobId = new ConcurrentHashMap<>();
  /** 待同步到 Redis 的任务 ID（仅标记，实际数据在 {@link #memoryByJobId}） */
  private final Set<Long> dirtyJobIds = ConcurrentHashMap.newKeySet();
  /** 上次对本任务 {@link Stored} 的变更时间（epoch ms），用于刷盘线程空闲退出 */
  private final ConcurrentMap<Long, Long> lastMutatedAtMs = new ConcurrentHashMap<>();
  /** 已为 jobId 启动刷盘线程时占用，防止重复启动 */
  private final Set<Long> flushWorkerJobIds = ConcurrentHashMap.newKeySet();
  /**
   * 导出状态刷 Redis：固定单线程池，多任务排队执行；线程名统一为 {@code skill-square-bulk-export-redis}。
   */
  private final ExecutorService redisFlushExecutor = Executors.newFixedThreadPool(1, r -> {
    Thread t = new Thread(r, "skill-square-bulk-export-redis");
    t.setDaemon(true);
    return t;
  });
  /** 刷盘间隔（毫秒），可由配置覆盖 */
  @Value("${bote.skill-square.bulk-export.redis-flush-interval-ms:2000}")
  private long redisFlushIntervalMs;
  /**
   * 距离上次内存状态变更超过该时长（毫秒）后，对应任务的 Redis 刷盘线程退出（终态通常在最后一次刷盘后已从内存移除，线程随即结束）。
   */
  @Value("${bote.skill-square.bulk-export.redis-flush-idle-exit-ms:120000}")
  private long redisFlushIdleExitMs;

  /**
   * @param cacheFactory 缓存工厂
   */
  public SkillSquareBulkExportCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM,
      CacheConsts.KEY_PREFIX_SKILL_SQUARE_BULK_EXPORT);
    this.userJobCacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM,
      CacheConsts.KEY_PREFIX_SKILL_SQUARE_BULK_EXPORT_USER_JOB);
  }

  /** 业务主键：任务 ID 字符串（完整 key 由 ICacheClient 拼接前缀） */
  private static String cacheKey(long jobId) {
    return String.valueOf(jobId);
  }

  private static String userJobCacheKey(Long userId) {
    return String.valueOf(userId);
  }

  /**
   * 写入任务初始状态（PENDING），并设置创建人、总条数与分包总数；同时启动 TTL 倒计时。
   *
   * @param jobId 任务 ID
   * @param creatorUserId 发起人用户 ID，用于后续 {@link #getForUser} 鉴权
   * @param totalRecords 可导出技能总条数
   * @param totalParts 计划分包数（每包上限见业务常量）
   */
  public void putInitial(long jobId, Long creatorUserId, int totalRecords, int totalParts) {
    Stored stored = new Stored();
    stored.setJobId(jobId);
    stored.setCreatorUserId(creatorUserId);
    stored.setPhase(SkillSquareBulkExportJobStatusVO.Phase.PENDING.name());
    stored.setTotalRecords(totalRecords);
    stored.setTotalParts(totalParts);
    stored.setCompletedParts(0);
    stored.setProcessedRecords(0);
    stored.setParts(new ArrayList<>());
    stored.setErrorMessage("");
    stored.setStartTimeMillis(System.currentTimeMillis());
    memoryByJobId.put(jobId, stored);
    dirtyJobIds.add(jobId);
    touchMutationTime(jobId);
    startRedisFlushWorker(jobId);
    if (creatorUserId != null) {
      userJobCacheClient.opsForValue().set(userJobCacheKey(creatorUserId), String.valueOf(jobId), TTL_SECONDS,
        TimeUnit.SECONDS);
    }
  }

  /**
   * 解析当前登录用户最近一次发起的导出任务状态（用于关闭弹窗后再次打开时恢复进度）；无绑定或任务已过期时返回 {@code null}。
   */
  @Nullable
  public SkillSquareBulkExportJobStatusVO getForUserCurrentJob(Long userId) {
    if (userId == null) {
      return null;
    }
    String raw = userJobCacheClient.opsForValue().get(userJobCacheKey(userId));
    if (raw == null || raw.isEmpty()) {
      return null;
    }
    long jobId;
    try {
      jobId = Long.parseLong(raw.trim());
    }
    catch (NumberFormatException e) {
      userJobCacheClient.delete(userJobCacheKey(userId));
      return null;
    }
    SkillSquareBulkExportJobStatusVO vo = getForUser(jobId, userId);
    if (vo == null) {
      userJobCacheClient.delete(userJobCacheKey(userId));
    }
    return vo;
  }

  /**
   * 清除当前用户与「最近一次导出任务」的绑定（例如用户在前端点击「新任务」后不再恢复该任务展示）。
   */
  public void clearCurrentUserJob(Long userId) {
    if (userId == null) {
      return;
    }
    userJobCacheClient.delete(userJobCacheKey(userId));
  }

  /**
   * 将任务阶段更新为 RUNNING（异步线程开始执行分包导出时调用）。
   *
   * @param jobId 任务 ID
   */
  public void markRunning(long jobId) {
    update(jobId, st -> st.setPhase(SkillSquareBulkExportJobStatusVO.Phase.RUNNING.name()));
  }

  /**
   * 增加「已写入导出包」的技能条数（异步任务内按批调用，供前端轮询展示实时进度）。
   *
   * @param jobId 任务 ID
   * @param delta 本批新增条数，须为正
   */
  public void addProcessedRecords(long jobId, int delta) {
    if (delta <= 0) {
      return;
    }
    update(jobId, st -> st.setProcessedRecords(st.getProcessedRecords() + delta));
  }

  /**
   * 追加一个已完成上传的分包信息，并刷新 {@code completedParts}；轮询方可逐步拿到各包下载路径。
   *
   * @param jobId 任务 ID
   * @param part 单包元数据（序号、条数、fileId、downloadPath 等）
   */
  public void appendPart(long jobId, SkillSquareBulkExportPartVO part) {
    update(jobId, st -> {
      if (st.getParts() == null) {
        st.setParts(new ArrayList<>());
      }
      st.getParts().add(part);
      st.setCompletedParts(st.getParts().size());
    });
  }

  /**
   * 标记任务全部成功（所有分包已写入缓存后由异步收尾调用）。
   *
   * @param jobId 任务 ID
   */
  public void markSuccess(long jobId) {
    update(jobId, st -> {
      st.setPhase(SkillSquareBulkExportJobStatusVO.Phase.SUCCESS.name());
      st.setProcessedRecords(st.getTotalRecords());
    });
  }

  /**
   * 标记任务失败，并记录错误说明供前端展示。
   *
   * @param jobId 任务 ID
   * @param message 失败原因，可为 null（将使用默认文案「导出失败」）
   */
  public void markFailed(long jobId, @Nullable String message) {
    update(jobId, st -> {
      st.setPhase(SkillSquareBulkExportJobStatusVO.Phase.FAILED.name());
      st.setErrorMessage(message != null ? message : "导出失败");
    });
  }

  /**
   * 按任务 ID 读取状态；仅当 {@code currentUserId} 与创建人一致时返回，否则返回 {@code null}（防越权）。
   * <p>
   * 优先从 Redis 读取，避免多实例下部分请求命中本机内存、部分命中 Redis 导致进度不一致；仅当 Redis 尚无键时回退本机内存（如首次刷盘前的极短窗口）。
   *
   * @param jobId 任务 ID
   * @param currentUserId 当前登录用户 ID
   * @return 轮询用 VO；任务不存在、无权限或缓存已过期时返回 {@code null}
   */
  @Nullable
  public SkillSquareBulkExportJobStatusVO getForUser(long jobId, Long currentUserId) {
    Stored stored = null;
    String json = cacheClient.opsForValue().get(cacheKey(jobId));
    if (json != null) {
      stored = JsonUtil.parseJson(json, new TypeReference<Stored>() {
      });
    }
    if (stored == null) {
      stored = memoryByJobId.get(jobId);
    }
    if (stored == null) {
      return null;
    }
    Long creatorUserIdStored = stored.getCreatorUserId();
    if (creatorUserIdStored == null || !creatorUserIdStored.equals(currentUserId)) {
      return null;
    }
    SkillSquareBulkExportJobStatusVO.Phase phase;
    try {
      phase = SkillSquareBulkExportJobStatusVO.Phase.valueOf(stored.getPhase());
    }
    catch (Exception e) {
      phase = SkillSquareBulkExportJobStatusVO.Phase.FAILED;
    }
    List<SkillSquareBulkExportPartVO> parts = stored.getParts() != null ? stored.getParts() : new ArrayList<>();
    return SkillSquareBulkExportJobStatusVO.builder()
      .jobId(stored.getJobId())
      .startTimeMillis(stored.getStartTimeMillis())
      .phase(phase)
      .totalRecords(stored.getTotalRecords())
      .totalParts(stored.getTotalParts())
      .completedParts(stored.getCompletedParts())
      .processedRecords(stored.getProcessedRecords())
      .errorMessage(stored.getErrorMessage())
      .parts(parts)
      .build();
  }

  /**
   * 在内存中修改任务状态并标记脏数据，由 {@link #redisFlushExecutor} 中的刷盘任务写入 Redis。
   * 若内存未命中则从 Redis 加载一次（例如进程重启后仅 Redis 中仍有任务）。
   */
  private void update(long jobId, Consumer<Stored> mutator) {
    Stored s = loadStored(jobId);
    if (s == null) {
      return;
    }
    mutator.accept(s);
    dirtyJobIds.add(jobId);
    touchMutationTime(jobId);
  }

  private void touchMutationTime(long jobId) {
    lastMutatedAtMs.put(jobId, System.currentTimeMillis());
  }

  private void startRedisFlushWorker(long jobId) {
    if (!flushWorkerJobIds.add(jobId)) {
      return;
    }
    redisFlushExecutor.execute(() -> {
      try {
        runRedisFlushLoop(jobId);
      }
      finally {
        flushWorkerJobIds.remove(jobId);
      }
    });
  }

  @PreDestroy
  public void shutdownRedisFlushExecutor() {
    redisFlushExecutor.shutdown();
    try {
      if (!redisFlushExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
        redisFlushExecutor.shutdownNow();
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      redisFlushExecutor.shutdownNow();
    }
  }

  private void runRedisFlushLoop(long jobId) {
    long interval = Math.max(1L, redisFlushIntervalMs);
    long idleExit = Math.max(interval, redisFlushIdleExitMs);
    try {
      while (true) {
        Stored s = memoryByJobId.get(jobId);
        if (s == null) {
          lastMutatedAtMs.remove(jobId);
          dirtyJobIds.remove(jobId);
          return;
        }
        flushDirtyJobToRedis(jobId);
        s = memoryByJobId.get(jobId);
        if (s == null) {
          lastMutatedAtMs.remove(jobId);
          return;
        }
        long last = lastMutatedAtMs.getOrDefault(jobId, 0L);
        if (System.currentTimeMillis() - last >= idleExit) {
          flushDirtyJobToRedis(jobId);
          return;
        }
        Thread.sleep(interval);
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      flushDirtyJobToRedis(jobId);
    }
  }

  /**
   * 若该任务在内存中且为脏，则写入 Redis 并续期 TTL；终态写入成功后从内存移除。
   */
  private void flushDirtyJobToRedis(long jobId) {
    if (!dirtyJobIds.contains(jobId)) {
      return;
    }
    Stored s = memoryByJobId.get(jobId);
    if (s == null) {
      dirtyJobIds.remove(jobId);
      return;
    }
    try {
      cacheClient.opsForValue().set(cacheKey(jobId), JsonUtil.toJsonString(s), TTL_SECONDS, TimeUnit.SECONDS);
      dirtyJobIds.remove(jobId);
      String phase = s.getPhase();
      if (SkillSquareBulkExportJobStatusVO.Phase.SUCCESS.name().equals(phase)
        || SkillSquareBulkExportJobStatusVO.Phase.FAILED.name().equals(phase)) {
        memoryByJobId.remove(jobId, s);
        lastMutatedAtMs.remove(jobId);
      }
    }
    catch (Exception e) {
      logger.warn("bulk export job status flush to redis failed, jobId={}", jobId, e);
    }
  }

  @Nullable
  private Stored loadStored(long jobId) {
    Stored s = memoryByJobId.get(jobId);
    if (s != null) {
      return s;
    }
    String json = cacheClient.opsForValue().get(cacheKey(jobId));
    if (json == null) {
      return null;
    }
    s = JsonUtil.parseJson(json, new TypeReference<Stored>() {
    });
    if (s == null) {
      return null;
    }
    memoryByJobId.put(jobId, s);
    return s;
  }

  /**
   * 与缓存中 JSON 结构对应的持久化模型（仅供本类序列化使用）。
   */
  @Getter
  @Setter
  @ToString
  public static class Stored {
    private Long jobId;
    @Nullable
    private Long creatorUserId;
    /** 任务创建时间（epoch ms），与 {@link #putInitial} 写入时刻一致 */
    @Nullable
    private Long startTimeMillis;
    private String phase;
    private int totalRecords;
    private int totalParts;
    private int completedParts;
    /** 已写入导出 zip 的技能条数（跨分包累计） */
    private int processedRecords;
    private String errorMessage;
    @Nullable
    private List<SkillSquareBulkExportPartVO> parts;
  }
}
