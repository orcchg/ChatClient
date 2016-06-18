package com.orcchg.chatclient.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.ui.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity<MainPresenter> implements MainMvpView {

    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;

    @Override
    protected MainPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return new MainPresenter(application.getDataManager());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.setDirectConnectionCallback();
        mPresenter.openDirectConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.removeDirectConnectionCallback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPresenter.closeDirectConnection();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onComplete() {
        mProgressView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        mProgressView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoading() {
        mProgressView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
    }
}
