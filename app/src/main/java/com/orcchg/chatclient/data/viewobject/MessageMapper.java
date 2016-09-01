package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;

public class MessageMapper implements Mapper<Message, MessageVO> {
    private boolean mIsDedicated = false;  // this message is only to you

    public MessageMapper() {
    }

    public MessageMapper(boolean isDedicated) {
        mIsDedicated = isDedicated;
    }

    @Override
    public MessageVO map(Message object) {
        return new MessageVO.Builder(object.getId())
                .setDestId(object.getDestId())
                .setLogin(object.getLogin())
                .setEmail(object.getEmail())
                .setMessage(object.getMessage())
                .setTimestamp(object.getTimestamp())
                .setDedicated(mIsDedicated)
                .build();
    }
}
