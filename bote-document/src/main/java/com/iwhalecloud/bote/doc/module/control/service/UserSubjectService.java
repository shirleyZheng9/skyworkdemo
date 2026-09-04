package com.iwhalecloud.bote.doc.module.control.service;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Service
@RequiredArgsConstructor
public class UserSubjectService {

  private final IDcOrgService dcOrgService;

  /**
   * 查询用户关联的所有unitId, 主要是组织ID
   *
   * @param userId 用户ID
   * @return 组织ID或者角色ID集合
   */
  public List<ControlSubject> queryUserAllRelUnitId(Long userId) {
    // 查询用户所属的组织ID, 暂时只有组织
    List<OrgDTO> orgList = dcOrgService.queryUserOrgList(userId);
    if (CollectionUtils.isEmpty(orgList)) {
      return Collections.singletonList(SubjectBuilder.userId(userId));
    }
    List<ControlSubject> subjectList = orgList.stream()
      .map(item -> SubjectBuilder.orgId(item.getOrgId()))
      .collect(Collectors.toList());
    subjectList.add(SubjectBuilder.userId(userId));
    return subjectList;
  }
}
