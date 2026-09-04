package com.iwhalecloud.bote.sms.bill.config;

import com.iwhalecloud.bote.sms.bill.data.BillDataSvr;
import com.iwhalecloud.bote.sms.bill.data.BillDataSvrPortType;
import com.iwhalecloud.bote.sms.bill.data.ObjectFactory;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class BillClientConfig {

  /**
   * 创建 WebService Service 实例
   */
  @Bean
  public BillDataSvr billDataSvr(BillClientProperties properties) {
    try {
      URL wsdlURL = new ClassPathResource(properties.getWsdlLocation()).getURL();
      return new BillDataSvr(wsdlURL, new QName("http://intf.smpin.tydic.com", "BillDataSvr"));
    }
    catch (Exception e) {
      throw new WebServiceException("Failed to load WSDL: " + e.getMessage(), e);
    }
  }

  @Bean
  public ObjectFactory objectFactory() {
    return new ObjectFactory();
  }

  /**
   * 创建 WebService Port Bean（核心客户端）
   */
  @Bean
  public BillDataSvrPortType billDataSvrPortType(BillDataSvr billDataSvr, BillClientProperties properties) {
    BillDataSvrPortType port = billDataSvr.getBillDataSvrHttpPort();
    // 设置实际的服务地址（覆盖 WSDL 中的地址）
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.getServiceUrl());

    // 添加自定义请求头：X-APP-ID 和 X-APP-KEY
    Map<String, List<String>> headers = new HashMap<>();
    headers.put("X-APP-ID", Collections.singletonList(properties.getAppId()));
    headers.put("X-APP-KEY", Collections.singletonList(properties.getAppKey()));
    ((BindingProvider) port).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);

    // 设置 Client 超时时间
    Client client = ClientProxy.getClient(port); //NOPMD - suppressed CloseResource - 不能关闭
    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setConnectionTimeout(properties.getConnectionTimeout());
    httpClientPolicy.setReceiveTimeout(properties.getReceiveTimeout());
    httpConduit.setClient(httpClientPolicy);

    // 添加日志拦截器：打印请求和响应报文
    client.getInInterceptors().add(new LoggingInInterceptor());
    client.getOutInterceptors().add(new LoggingOutInterceptor());

    return port;
  }

}
