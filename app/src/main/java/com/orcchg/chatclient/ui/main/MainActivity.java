package com.orcchg.chatclient.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.WindowUtility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity<MainPresenter> implements MainMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.MAIN_ACTIVITY;
    static String SHARED_PREFS_KEY_USER_EMAIL;

    @Bind(R.id.toolbar) Toolbar mToolbar;
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
        FrameworkUtility.setActive(REQUEST_CODE);
        WindowUtility.logScreenParams(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();

        SHARED_PREFS_KEY_USER_EMAIL = getResources().getString(R.string.shared_prefs_user_email_key);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.init();
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
        mPresenter.closeDirectConnection();
    }

    @Override
    protected void onDestroy() {
        FrameworkUtility.setInactive(REQUEST_CODE);
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSuccess() {
        onComplete();
    }

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

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mToolbar.setTitle(R.string.main_label);
    }
}
