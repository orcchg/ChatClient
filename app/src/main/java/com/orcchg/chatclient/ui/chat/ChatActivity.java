package com.orcchg.chatclient.ui.chat;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity<ChatPresenter> implements ChatMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.CHAT_ACTIVITY;

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    @Bind(R.id.root_container) ViewGroup mRootContainer;
    @Bind(R.id.action_container) View mActionContainer;
    @Bind(R.id.rv_messages) RecyclerView mMessagesView;
    @Bind(R.id.et_message) EditText mMessagesEditView;
    @Bind(R.id.btn_send_message) ImageButton mSendMessageButton;
    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;

    private LinearLayoutManager mLayoutManager;

    @Override
    protected ChatPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        long id = getIntent().getLongExtra(EXTRA_USER_ID, Status.UNKNOWN_ID);
        String name = getIntent().getStringExtra(EXTRA_USER_NAME);
        return new ChatPresenter(application.getDataManager(), id, name);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameworkUtility.setActive(REQUEST_CODE);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(mLayoutManager);
        mMessagesView.setAdapter(mPresenter.getChatAdapter());

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.sendMessage();
                mMessagesEditView.setText("");  // clean up
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onRetry();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.removeDirectConnectionCallback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPresenter.logout();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unsubscribe();
        FrameworkUtility.setInactive(REQUEST_CODE);
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onComplete() {
        if (mMessagesView.getVisibility() != View.VISIBLE) {
            mActionContainer.setVisibility(View.VISIBLE);
            mMessagesView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError() {
        mActionContainer.setVisibility(View.GONE);
        mMessagesView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        mProgressView.setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        mActionContainer.setVisibility(View.GONE);
        mMessagesView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public String getMessage() {
        return mMessagesEditView.getText().toString();
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    @Override
    public void scrollListTo(int position) {
        mLayoutManager.scrollToPosition(position);
    }

    @Override
    public void showSnackbar(String message, int duration) {
        Snackbar.make(mRootContainer, message, duration).show();
    }
}
