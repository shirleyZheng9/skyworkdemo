package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * socket心跳数据包
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartbeatInfo {
}
