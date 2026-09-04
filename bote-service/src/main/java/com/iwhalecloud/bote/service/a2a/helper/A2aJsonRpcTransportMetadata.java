package com.iwhalecloud.bote.service.a2a.helper;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;

/**
 * A2A JSON-RPC 传输协议
 *
 * @author bianjp
 * @since 2025-09-28
 */
public class A2aJsonRpcTransportMetadata implements TransportMetadata {
  @Override
  public String getTransportProtocol() {
    return TransportProtocol.JSONRPC.asString();
  }
}
