package com.iwhalecloud.bote.sms.bill.client;

import com.iwhalecloud.bote.service.sms.BillSmsClient;
import com.iwhalecloud.bote.sms.bill.config.BillClientProperties;
import com.iwhalecloud.bote.sms.bill.data.ArrayOfBillReqVo;
import com.iwhalecloud.bote.sms.bill.data.BillDataSvrPortType;
import com.iwhalecloud.bote.sms.bill.data.BillReqVo;
import com.iwhalecloud.bote.sms.bill.data.BillResVo;
import com.iwhalecloud.bote.sms.bill.data.ObjectFactory;
import com.iwhalecloud.bote.sms.bill.data.UserVo;
import com.iwhalecloud.bote.sms.bill.util.BillHexUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class BillSmsServiceClient implements BillSmsClient {

  private static final Logger logger = LoggerFactory.getLogger(BillSmsServiceClient.class);

  private final BillClientProperties properties;
  private final BillDataSvrPortType billDataSvrPortType;
  private final ObjectFactory objectFactory;

  @Override
  public boolean sendSmsMessage(String phone, String msgContent) {
    // 创建推送消息的用户信息和短信数据
    UserVo userVo = createUserVo();
    BillReqVo billReq = createBillReq(phone, msgContent);

    // 调用 WebService
    BillResVo response = sendBillRequest(userVo, Collections.singletonList(billReq));

    // 处理结果
    if ("000".equals(getElementValue(response.getState()))) {
      logger.info("执行短信发送成功！");
      return true;
    }
    else {
      logger.error("执行短信发送失败: state={}, stateDesc={}", getElementValue(response.getState()), getElementValue(response.getStateDesc()));
      return false;
    }
  }

  /**
   * 创建消息推送请求的用户信息
   */
  private UserVo createUserVo() {
    UserVo userVo = new UserVo();
    // 用户名
    userVo.setUserName(objectFactory.createUserVoUserName(properties.getUsername()));
    // 用户密码
    userVo.setPassWord(objectFactory.createUserVoPassWord(properties.getPassword()));
    // 所属系统
    userVo.setSysCode(objectFactory.createUserVoSysCode(properties.getSysCode()));
    // 产品ID
    userVo.setProductId(objectFactory.createUserVoProductId(properties.getProductId()));
    return userVo;
  }

  /**
   * 创建消息推送请求
   */
  private BillReqVo createBillReq(String toTel, String content) {
    BillReqVo req = new BillReqVo();
    // 渠道类型 1:短信
    req.setChannelType(1);
    // 发送到的电话号码
    req.setToTel(objectFactory.createBillReqVoToTel(toTel));
    // 发送内容
    req.setSentContent(objectFactory.createBillReqVoSentContent(BillHexUtil.string2HexGBK(content)));
    // 系统编号
    req.setSysCode(objectFactory.createBillReqVoSysCode(properties.getSysCode()));
    // 流水号：保证工单唯一性
    req.setFlowCode(objectFactory.createBillReqVoFlowCode(String.valueOf(IDUtils.nextId())));
    // 本地网id
    req.setLatnId(objectFactory.createBillReqVoLatnId(properties.getLatnId()));
    // 发送类型: SUB:短信下行
    req.setSentType(objectFactory.createBillReqVoSentType("SUB"));
    // 业务场境ID
    req.setBusinessId(properties.getBusinessId());
    return req;
  }

  /**
   * 推送消息给统一消息管理平台
   *
   * @param billRequests 请求列表
   * @return 响应结果
   */
  public BillResVo sendBillRequest(UserVo userVo, List<BillReqVo> billRequests) {
    // 构造 ArrayOfBillReqVo
    ArrayOfBillReqVo arrayOfBillReqVo = new ArrayOfBillReqVo();
    arrayOfBillReqVo.getBillReqVo().addAll(billRequests);

    // 调用 WebService
    try {
      logger.debug("执行短信消息推送：serviceUrl={}",  properties.getServiceUrl());
      return billDataSvrPortType.billInfo(userVo, arrayOfBillReqVo);
    }
    catch (Exception e) {
      throw new RuntimeException("短信推送失败", e);
    }
  }

  /**
   * 提取 JAXBElement 元素内容
   */
  private <T> T getElementValue(JAXBElement<T> element) {
    return element != null ? element.getValue() : null;
  }

}
