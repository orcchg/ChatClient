package com.orcchg.chatclient.ui.chat.peerslist;

import com.orcchg.chatclient.data.viewobject.PeerVO;

public abstract class ChatPeersList {
    protected long mDestId;

    public void setDestId(long id) {
        mDestId = id;
    }

    abstract public void addItem(long id, PeerVO peer);
    abstract public void removeItem(long id);
}
