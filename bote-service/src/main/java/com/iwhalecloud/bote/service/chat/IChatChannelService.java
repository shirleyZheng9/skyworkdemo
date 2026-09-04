package com.iwhalecloud.bote.service.chat;

import com.iwhalecloud.bote.dto.channel.SimpleChannelJobDTO;

/**
 *
 */
public interface IChatChannelService {
  void execute(SimpleChannelJobDTO channel);
}
