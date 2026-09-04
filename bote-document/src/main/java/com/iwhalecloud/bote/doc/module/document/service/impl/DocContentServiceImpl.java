package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.doc.common.model.PageParams;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.YdocContent;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocContentEntity;
import com.iwhalecloud.bote.doc.module.document.entity.DocContentHistoryEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocContentHistoryMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.DocContentMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocContentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentContributorService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 在线文档内容 service
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Service
@RequiredArgsConstructor
public class DocContentServiceImpl implements IDocContentService {

  private final DocContentMapper docContentMapper;

  private final DocContentHistoryMapper docContentHistoryMapper;

  private final DistributedLockFactory distributedLockFactory;

  private final IDocumentContributorService documentContributorService;

  private final DocumentMapper documentMapper;
  private final IDcUserService dcUserService;

  @Override
  public DocContentHistoryDTO selectLastDocHistory(String documentId) {
    PageParams pageParams = new PageParams();
    pageParams.setPageNum(1);
    pageParams.setPageSize(1);
    Page<DocContentHistoryDTO> page = docContentHistoryMapper.selectLastDocHistory(documentId, pageParams.buildRowBounds());  //NOPMD - suppressed CloseResource - 不需要关闭
    if (page.getTotal() == 0) {
      return null;
    }
    return page.getResult().get(0);
  }

  @Transactional
  @Override
  public void saveContentHistory(String documentId, String content, Long updatorId) {
    Assert.hasText(documentId, "文档ID不能为空");
    Assert.notNull(updatorId, "更新人ID不能为空");

    // 从 bt_dc_document 表获取版本号
    Long documentRevision = documentMapper.selectRevisionByDocumentId(documentId);

    DocContentHistoryEntity existingHistory = docContentHistoryMapper.selectByDocumentIdAndRevision(documentId,
      documentRevision);
    if (existingHistory != null) {
      return;
    }
    DocContentHistoryEntity historyEntity = new DocContentHistoryEntity();
    historyEntity.setId(IDUtils.nextId());
    historyEntity.setDocumentId(documentId);
    historyEntity.setContent(content);
    historyEntity.setRevision(documentRevision);
    historyEntity.setUpdatorId(updatorId);
    historyEntity.setUpdatedTime(new Date());
    historyEntity.setCreatorId(updatorId);
    historyEntity.setCreatedTime(new Date());
    historyEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    historyEntity.setTenantId(TenantContextHolder.getTenantId());
    docContentHistoryMapper.insert(historyEntity);
  }

  @Transactional
  @Override
  public boolean saveDocContent(String documentId, DocumentContentDTO contentDTO) {
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_CONTENT_SAVE_LOCK, documentId);
    // 加锁防止并发保存
    boolean locked = lock.tryLock();
    if (!locked) {
      return false;
    }
    Long updatorId = contentDTO.getUpdatorId();
    try {
      DcDocContentEntity dcDocContentEntity = docContentMapper.selectByDocId(documentId);
      if (dcDocContentEntity == null) {
        dcDocContentEntity = new DcDocContentEntity();
        dcDocContentEntity.setId(IDUtils.nextId());
        dcDocContentEntity.setDocumentId(documentId);
        dcDocContentEntity.setContent(JsonUtil.toJsonString(contentDTO.getContent()));
        dcDocContentEntity.setYdoc(contentDTO.getYdocBase64());
        dcDocContentEntity.setTextContent(contentDTO.getTextContent());
        dcDocContentEntity.setCreatorId(updatorId);
        dcDocContentEntity.setTenantId(TenantContextHolder.getTenantId());
        dcDocContentEntity.setUpdatorId(updatorId);
        dcDocContentEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        docContentMapper.insert(dcDocContentEntity);
      }
      else {
        dcDocContentEntity.setContent(JsonUtil.toJsonString(contentDTO.getContent()));
        dcDocContentEntity.setYdoc(contentDTO.getYdocBase64());
        dcDocContentEntity.setTextContent(contentDTO.getTextContent());
        dcDocContentEntity.setUpdatorId(updatorId);
        docContentMapper.updateByPrimaryKey(dcDocContentEntity);
      }
      // 更新最近修改
      IDocumentService documentService = SpringUtil.getBean(IDocumentService.class);
      documentService.updateLastModify(documentId, updatorId);
      boolean result = documentContributorService.addOrUpdateContributor(documentId, updatorId, BigDecimal.ZERO,
        updatorId);
      Assert.isTrue(result, "更新贡献数据失败");
    }
    finally {
      lock.unlock();
    }
    return true;
  }

  @Override
  public OnlineDocumentInfoDTO findContentByDocumentId(String documentId) {
    DcDocContentEntity dcDocContentEntity = docContentMapper.selectByDocId(documentId);
    if (dcDocContentEntity == null) {
      dcDocContentEntity = initDocContentEntity(documentId);
    }
    OnlineDocumentInfoDTO onlineDocumentInfoDTO = new OnlineDocumentInfoDTO();
    onlineDocumentInfoDTO.setDocumentId(documentId);
    onlineDocumentInfoDTO.setCreatedTime(dcDocContentEntity.getCreatedTime());
    onlineDocumentInfoDTO.setUpdatedTime(dcDocContentEntity.getUpdatedTime());
    onlineDocumentInfoDTO.setUpdatorId(dcDocContentEntity.getUpdatorId());
    onlineDocumentInfoDTO.setYdocBase64(dcDocContentEntity.getYdoc());
    onlineDocumentInfoDTO.setTextContent(dcDocContentEntity.getTextContent());
    YdocContent content = JsonUtil.parseJson(dcDocContentEntity.getContent(), YdocContent.class);
    onlineDocumentInfoDTO.setContent(content);
    return onlineDocumentInfoDTO;
  }

  /**
   * 初始化默认的在线文档内容
   *
   * @param documentId 文档ID
   * @return 文档内容实体
   */
  @Override
  public DcDocContentEntity initDocContentEntity(String documentId) {
    DcDocContentEntity dcDocContentEntity = new DcDocContentEntity();
    dcDocContentEntity.setId(IDUtils.nextId());
    dcDocContentEntity.setDocumentId(documentId);
    dcDocContentEntity.setContent(null);
    dcDocContentEntity.setYdoc(null);
    dcDocContentEntity.setTextContent(null);
    dcDocContentEntity.setCreatorId(-1L);
    dcDocContentEntity.setUpdatorId(-1L);
    dcDocContentEntity.setCreatedTime(new Date());
    dcDocContentEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    dcDocContentEntity.setTenantId(TenantContextHolder.getTenantId());
    docContentMapper.insert(dcDocContentEntity);
    return dcDocContentEntity;
  }

  @Override
  @Transactional
  public void restoreContentVersion(String documentId, Long id) {
    DocContentHistoryEntity docContentHistoryEntity = docContentHistoryMapper.selectByVersionId(id);
    if (docContentHistoryEntity == null) {
      throw new BssException("指定版本不存在！");
    }
    // selectByVersionId 仅按历史表主键查询，必须校验归属，避免用其他文档的历史 id 覆盖当前文档
    if (!Objects.equals(documentId, docContentHistoryEntity.getDocumentId())) {
      throw new BssException("指定版本不属于该文档！");
    }
    DcDocContentEntity dcDocContentEntity = docContentMapper.selectByDocId(documentId);
    if (dcDocContentEntity == null) {
      throw new BssException("源文件丢失！");
    }

    Long updatorId = SessionUtil.getLoginInfo().getUserId();

    TenantContextHolder.setTenantId(dcDocContentEntity.getTenantId());
    saveContentHistory(documentId, dcDocContentEntity.getContent(), updatorId);

    // 恢复版本内容
    dcDocContentEntity.setContent(docContentHistoryEntity.getContent());
    dcDocContentEntity.setYdoc(null); //客户端恢复连接让它根据content重新创建
    dcDocContentEntity.setTextContent(null);
    dcDocContentEntity.setUpdatorId(updatorId);
    docContentMapper.updateByPrimaryKey(dcDocContentEntity);

    // 更新文档表的最后修改时间和修改人，同时更新revision版本号
    IDocumentService documentService = SpringUtil.getBean(IDocumentService.class);
    documentService.updateLastModify(documentId, updatorId);

    // 更新贡献者信息
    boolean result = documentContributorService.addOrUpdateContributor(documentId, updatorId, BigDecimal.ZERO, updatorId);
    Assert.isTrue(result, "更新贡献数据失败");
  }

  @Override
  public DocContentHistoryDTO getDocumentContentVersion(String documentId, Long id) {
    return docContentHistoryMapper.getDocumentContentVersion(documentId, id);
  }

  @Override
  public List<DocContentHistoryInfoDTO> getDocumentContentVersions(String documentId) {
    if (StringUtils.isBlank(documentId)) {
      return new ArrayList<>();
    }
    List<DocContentHistoryDTO> list = docContentHistoryMapper.getDocumentContentVersions(documentId);
    if (list == null || list.isEmpty()) {
      list = new ArrayList<>();
    }
    DocContentHistoryDTO currentRow = null;
    DcDocContentDTO current = docContentMapper.selectDTOByDocId(documentId);
    if (current != null) {
      Long docRevision = current.getRevision();
      boolean revisionAlreadyInHistory = false;
      if (docRevision != null) {
        for (DocContentHistoryDTO h : list) {
          if (docRevision.equals(h.getRevision())) {
            revisionAlreadyInHistory = true;
            break;
          }
        }
      }
      // 历史里已有与当前文档 revision 一致的快照时，不再补充「再用/当前编辑」占位行，避免重复
      if (!revisionAlreadyInHistory) {
        currentRow = buildCurrentDocContentHistoryDto(current);
        list.add(0, currentRow);
      }
    }
    return groupDocContentHistoryByCalendarDay(list, currentRow);
  }

  /**
   * 将当前在线文档内容作为一条版本记录（展示名取最后更新人，创建时间取内容表更新时间），合并后固定排在首位。
   */
  private DocContentHistoryDTO buildCurrentDocContentHistoryDto(DcDocContentEntity entity) {
    DocContentHistoryDTO dto = new DocContentHistoryDTO();
    dto.setDocumentId(entity.getDocumentId());
    dto.setContent(entity.getContent());
    dto.setCreatorId(entity.getCreatorId());
    PortalUserDTO userDTO = dcUserService.findUserById(entity.getUpdatorId());
    if (userDTO != null) {
      dto.setUserName(userDTO.getUserName());
    }
    dto.setCreatedTime(entity.getUpdatedTime());
    return dto;
  }

  /**
   * 按自然日分组：{@code revisionName} 当天为「今天」，其余为 {@code uuuu-MM-dd}；今天优先、其余日期从新到旧；
   * 组内按创建时间倒序；当前文档内容行（若有）固定为该组第一条。
   */
  private List<DocContentHistoryInfoDTO> groupDocContentHistoryByCalendarDay(List<DocContentHistoryDTO> list,
    @Nullable DocContentHistoryDTO currentRow) {
    List<DocContentHistoryInfoDTO> out = new ArrayList<>();
    if (list.isEmpty()) {
      return out;
    }
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    Map<LocalDate, List<DocContentHistoryDTO>> byDay = new LinkedHashMap<>();
    for (DocContentHistoryDTO dto : list) {
      LocalDate day = dto.getCreatedTime().toInstant().atZone(zone).toLocalDate();
      byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(dto);
    }
    for (List<DocContentHistoryDTO> dayItems : byDay.values()) {
      sortDocContentHistoryNewestFirst(dayItems);
      moveCurrentRowFirstInGroup(dayItems, currentRow);
    }
    List<DocContentHistoryDTO> todayList = byDay.remove(today);
    if (todayList != null && !todayList.isEmpty()) {
      out.add(buildDocContentHistoryGroup(DocBaseConsts.DOC_HISTORY_GROUP_NAME_TODAY, todayList));
    }
    for (Map.Entry<LocalDate, List<DocContentHistoryDTO>> e : byDay.entrySet()) {
      out.add(buildDocContentHistoryGroup(e.getKey().format(DocBaseConsts.DOC_HISTORY_OTHER_DAY_GROUP_KEY), e.getValue()));
    }
    return out;
  }

  private DocContentHistoryInfoDTO buildDocContentHistoryGroup(String revisionName, List<DocContentHistoryDTO> data) {
    DocContentHistoryInfoDTO chunk = new DocContentHistoryInfoDTO();
    chunk.setRevisionName(revisionName);
    chunk.setData(data);
    return chunk;
  }

  /** 创建时间降序 */
  private void sortDocContentHistoryNewestFirst(List<DocContentHistoryDTO> items) {
    items.sort(Comparator
      .comparing(DocContentHistoryDTO::getCreatedTime, Comparator.nullsLast(Comparator.naturalOrder()))
      .reversed());
  }

  /**
   * 将「当前文档内容」对应 DTO 排到该自然日分组的第一条（与 {@code currentRow} 引用相同的一条）。
   */
  private void moveCurrentRowFirstInGroup(List<DocContentHistoryDTO> dayItems, @Nullable DocContentHistoryDTO currentRow) {
    if (currentRow == null) {
      return;
    }
    for (int i = 0; i < dayItems.size(); i++) {
      if (dayItems.get(i) == currentRow) {
        if (i > 0) {
          dayItems.remove(i);
          dayItems.add(0, currentRow);
        }
        break;
      }
    }
  }
}
