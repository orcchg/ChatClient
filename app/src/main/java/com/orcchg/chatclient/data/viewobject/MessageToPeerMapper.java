package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;

public class MessageToPeerMapper implements Mapper<Message, PeerVO> {
    @Override
    public PeerVO map(Message message) {
        return new PeerVO.Builder(message.getId())
                .setLogin(message.getLogin())
                .setEmail(message.getEmail())
                .build();
    }
}
