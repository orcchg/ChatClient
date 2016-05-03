package com.orcchg.chatclient.ui.chat;

import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.data.viewobject.SystemMessageVO;
import com.orcchg.chatclient.ui.base.MvpView;

public interface ChatMvpView extends MvpView {
    void showMessage(MessageVO message);
    void showSystemMessage(SystemMessageVO systemMessage);
}
