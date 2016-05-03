package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;

public class MessageMapper implements Mapper<Message, MessageVO> {
    @Override
    public MessageVO map(Message object) {
        return new MessageVO.Builder()
                .setLogin(object.getLogin())
                .setMessage(object.getMessage())
                .setTimestamp(object.getTimestamp())
                .build();
    }
}
