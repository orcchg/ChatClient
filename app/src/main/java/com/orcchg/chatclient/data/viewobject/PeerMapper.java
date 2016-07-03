package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Peer;

public class PeerMapper implements Mapper<Peer, PeerVO> {
    @Override
    public PeerVO map(Peer object) {
        return new PeerVO.Builder(object.getId())
                .setLogin(object.getLogin())
                .setChannel(object.getChannel())
                .build();
    }
}
