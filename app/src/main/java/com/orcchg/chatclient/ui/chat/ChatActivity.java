package com.orcchg.chatclient.ui.chat;

import com.orcchg.chatclient.ui.base.BaseActivity;

public class ChatActivity extends BaseActivity<ChatPresenter> implements ChatMvpView {

    @Override
    protected ChatPresenter createPresenter() {
        return new ChatPresenter();
    }
}
