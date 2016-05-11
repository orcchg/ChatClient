package com.orcchg.chatclient.ui.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.data.viewobject.SystemMessageVO;
import com.orcchg.chatclient.ui.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity<ChatPresenter> implements ChatMvpView {

    @Bind(R.id.rv_messages) RecyclerView mMessagesView;
    @Bind(R.id.et_message) EditText mMessagesEditView;
    @Bind(R.id.btn_send_message) ImageButton mSendMessageButton;

    @Override
    protected ChatPresenter createPresenter() {
        return new ChatPresenter();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mMessagesView.setLayoutManager(new LinearLayoutManager(this));
        mMessagesView.setAdapter(mPresenter.getChatAdapter());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.loadMessages();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showMessage(MessageVO message) {

    }

    @Override
    public void showSystemMessage(SystemMessageVO systemMessage) {

    }
}
