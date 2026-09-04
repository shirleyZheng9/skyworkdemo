package com.iwhalecloud.bote.service.publish.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.dto.publish.PublishGatewayQueryParams;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import com.iwhalecloud.bote.mapper.publish.PublishGatewayManageMapper;
import com.iwhalecloud.bote.service.publish.IPublishGatewayManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 在线环境维护服务实现
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Service
@RequiredArgsConstructor
public class PublishGatewayManageServiceImpl implements IPublishGatewayManageService {

  private final PublishGatewayManageMapper publishGatewayManageMapper;

  @Override
  public PublishGatewayDTO findPublishGateway(Long tenantId, Long gatewayId) {
    return publishGatewayManageMapper.getPublishGateway(tenantId, gatewayId);
  }

  @Override
  public PublishGatewaySimpleDTO findPublishGatewaySimple(Long tenantId, Long gatewayId) {
    return publishGatewayManageMapper.getPublishGatewaySimple(tenantId, gatewayId);
  }

  @Override
  @Transactional
  public ResultVO<PublishGatewayDTO> savePublishGateway(PublishGatewayDTO dto) {
    PublishGatewayDTO old = dto.getGatewayId() == null ? null : findPublishGateway(dto.getTenantId(), dto.getGatewayId());
    if (old == null) {
      dto.setGatewayId(Sequences.PUBLISH_GATEWAY_ID.next());
      dto.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    }
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    DataDifferenceStarter.computeSave(old, dto, false, dto.getTenantId());
    return ResultVO.success(dto);
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePublishGateway(Long tenantId, Long gatewayId) {
    // 检查记录是否存在
    PublishGatewayDTO gateway = publishGatewayManageMapper.getPublishGateway(tenantId, gatewayId);
    Assert.notNull(gateway, "网关不存在，无法删除");

    // 逻辑删除：更新status_cd为'00X'
    publishGatewayManageMapper.deletePublishGateway(tenantId, gatewayId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<PublishGatewayDTO> queryPublishGatewayPage(PublishGatewayQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return publishGatewayManageMapper.selectPublishGatewayPage(queryParams, rowBounds).toPageInfo();
  }
}

