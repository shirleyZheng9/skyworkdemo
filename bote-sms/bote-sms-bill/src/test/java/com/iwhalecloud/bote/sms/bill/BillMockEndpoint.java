package com.iwhalecloud.bote.sms.bill;

import com.iwhalecloud.bote.sms.bill.data.BillDataSvrPortType;
import com.iwhalecloud.bote.sms.bill.data.ObjectFactory;
import com.iwhalecloud.bote.sms.bill.data.ArrayOfBillReqVo;
import com.iwhalecloud.bote.sms.bill.data.BillReqVo;
import com.iwhalecloud.bote.sms.bill.data.BillResVo;
import com.iwhalecloud.bote.sms.bill.data.UserVo;
import com.iwhalecloud.bote.sms.bill.util.BillHexUtil;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

@WebService(
  serviceName = "BillDataSvr",
  portName = "BillDataSvrHttpPort",
  targetNamespace = "http://intf.smpin.tydic.com",
  endpointInterface = "com.iwhalecloud.bote.sms.bill.data.BillDataSvrPortType"
)
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class BillMockEndpoint implements BillDataSvrPortType {

  private static final Logger logger = LoggerFactory.getLogger(BillMockEndpoint.class);

  private final ObjectFactory objectFactory;

  @Resource
  private WebServiceContext webServiceContext;

  @Override
  public BillResVo billInfo(UserVo userVo, ArrayOfBillReqVo arrayOfBillReqVo) {
    return processBillInfo(userVo, arrayOfBillReqVo);
  }

  /**
   * 处理短信推送请求
   */
  public BillResVo processBillInfo(UserVo userVo, ArrayOfBillReqVo arrayOfBillReqVo) {
    logger.info("收到短信推送请求");

    // 获取并打印请求头信息
    MessageContext messageContext = webServiceContext.getMessageContext();
    @SuppressWarnings("unchecked")
    Map<String, List<String>> httpHeaders = (Map<String, List<String>>) messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);
    if (httpHeaders != null) {
      logger.info("=== 请求头信息 ===");
      for (Map.Entry<String, List<String>> headerEntry : httpHeaders.entrySet()) {
        String headerName = headerEntry.getKey();
        List<String> headerValues = headerEntry.getValue();
        logger.info("请求头: {} = {}", headerName, headerValues);
      }
    }

    // 打印用户信息
    if (userVo != null) {
      logger.info("用户信息: userName={}, sysCode={}, productId={}", getElementValue(userVo.getUserName()), getElementValue(userVo.getSysCode()),
        getElementValue(userVo.getProductId()));
    }

    // 打印请求参数
    if (arrayOfBillReqVo != null && arrayOfBillReqVo.getBillReqVo() != null) {
      for (BillReqVo req : arrayOfBillReqVo.getBillReqVo()) {
        logger.info("短信请求参数信息: 手机号={}, 内容={}, 系统编号={}, 流水号={}, 本地网id={}, 发送类型={}, 业务场境ID={}", getElementValue(req.getToTel()), BillHexUtil.hexGBK2String(getElementValue(req.getSentContent())),
          getElementValue(req.getSysCode()), getElementValue(req.getFlowCode()), getElementValue(req.getLatnId()), getElementValue(req.getSentType()),
          req.getBusinessId());
      }
    }

    // 模拟响应
    BillResVo response = new BillResVo();
    response.setState(objectFactory.createBillResVoState("000"));
    response.setStateDesc(objectFactory.createBillResVoStateDesc("处理成功 - Mock 服务"));

    logger.info("返回响应: state={}, desc={}", getElementValue(response.getState()), getElementValue(response.getStateDesc()));
    return response;
  }

  /**
   * 提取 JAXBElement 元素内容
   */
  private <T> T getElementValue(JAXBElement<T> element) {
    return element != null ? element.getValue() : null;
  }

}
