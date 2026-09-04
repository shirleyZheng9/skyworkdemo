package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetIOJobEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IIOJobRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.IOJobDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.IOJobConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * IO任务仓库实现类
 */
@Component("iOJobRepoImpl")
@RequiredArgsConstructor
public class IOJobRepoImpl implements IIOJobRepo {
  private final IOJobDAO ioJobDAO;
  private final IIDGenerator idGenerator;

  /**
   * 创建IO任务
   */
  @Override
  public void createIOJob(IOJob job) {
    // 生成ID
    job.setId(idGenerator.genId());

    // 转换为PO
    DatasetIOJobEntity po = IOJobConvertor.ioJobDO2PO(job);

    // 创建任务
    ioJobDAO.createIOJob(po);

    // 设置返回信息
    job.setId(po.getId());
    job.setCreatedAt(po.getCreatedAt() != null ? po.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli() : null);
    job.setUpdatedAt(po.getUpdatedAt() != null ? po.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli() : null);
  }

  /**
   * 获取IO任务
   */
  @Override
  public IOJob getIOJob(Long id) {
    DatasetIOJobEntity job = ioJobDAO.getIOJob(id);
    if (job == null) {
      return null;
    }
    return IOJobConvertor.ioJobPO2DO(job);
  }

  /**
   * 更新IO任务
   * 迁移对应关系: Go语言DatasetRepo.UpdateIOJob
   * - 功能: 更新IO任务
   * - 参数: id - 任务ID, delta - 更新数据
   * - 用途: 更新IO任务
   */
  @Override
  public void updateIOJob(Long id, DeltaDatasetIOJob delta) {
    String status = null;
    if (delta.getStatus() != null) {
      status = delta.getStatus();
    }

    DeltaDatasetIOJob daoParam = DeltaDatasetIOJob.builder()
      .total(delta.getTotal())
      .status(status)
      .preProcessed(delta.getPreProcessed())
      .deltaProcessed(delta.getDeltaProcessed())
      .deltaAdded(delta.getDeltaAdded())
      .subProgresses(delta.getSubProgresses())
      .errors(delta.getErrors())
      .startedAt(delta.getStartedAt())
      .endedAt(delta.getEndedAt())
      .build();

    ioJobDAO.updateIOJob(id, daoParam);
  }

  /**
   * 列表查询IO任务
   */
  @Override
  public List<IOJob> listIOJobs(ListIOJobsParams params) {
    ListIOJobsParams daoParam = ListIOJobsParams.builder()
      .spaceId(params.getSpaceId())
      .datasetId(params.getDatasetId())
      .types(params.getTypes())
      .statuses(params.getStatuses())
      .build();

    List<DatasetIOJobEntity> jobs = ioJobDAO.listIOJobs(daoParam);
    if (jobs == null || jobs.isEmpty()) {
      return List.of();
    }

    return jobs.stream()
      .map(IOJobConvertor::ioJobPO2DO)
      .collect(Collectors.toList());
  }
}
