package com.iwhalecloud.bote.sms.bill;

import com.iwhalecloud.bote.sms.bill.data.BillDataSvrPortType;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillMockConfig {

  @Bean
  public Endpoint billDataEndpoint(Bus bus, BillDataSvrPortType billMockEndpoint) {
    EndpointImpl endpoint = new EndpointImpl(bus, billMockEndpoint);
    endpoint.publish("/BillDataSvr");
    return endpoint;
  }

}
