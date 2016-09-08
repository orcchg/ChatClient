package com.orcchg.chatclient.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.NetworkUtility;
import com.orcchg.chatclient.util.WindowUtility;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity<MainPresenter> implements MainMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.MAIN_ACTIVITY;
    public static final String EXTRA_SNACKBAR_MESSAGE_RES_ID = "extra_snackbar_message_res_id";
    static String SHARED_PREFS_KEY_USER_EMAIL;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;
    @Bind(R.id.optional_text) TextView mOptionalText;

    private boolean mIsBackPressed;

    @Override
    protected MainPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return new MainPresenter(application.getDataManager());
    }

    @Override
    @FrameworkUtility.RequestCode.Code
    protected int getActivityRequestCode() {
        return REQUEST_CODE;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FrameworkUtility.isActive(FrameworkUtility.RequestCode.CHAT_ACTIVITY) &&
            !FrameworkUtility.isFinishing(FrameworkUtility.RequestCode.CHAT_ACTIVITY)/* &&
            !ServerBridge.hasNetworkError()*/) {
            Timber.w("Chat is still alive but paused. Go to Chat instead");
            finish();
            return;
        }

        mIsBackPressed = false;
        FrameworkUtility.setActive(REQUEST_CODE);
        FrameworkUtility.diagnostic();
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
        @StringRes int snackbarResId = getIntent().getIntExtra(EXTRA_SNACKBAR_MESSAGE_RES_ID, 0);
        if (snackbarResId > 0) {
            showSnackbar(snackbarResId, Snackbar.LENGTH_SHORT);
        }

        mPresenter.init();
        mPresenter.onRetry();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.removeDirectConnectionCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FrameworkUtility.setFinishing(REQUEST_CODE, isFinishing());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
        mPresenter.closeDirectConnection();
    }

    @Override
    protected void onDestroy() {
        FrameworkUtility.setInactive(REQUEST_CODE);
        FrameworkUtility.diagnostic();
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSuccess() {
        Timber.d("onSuccess");
        mProgressView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void onTerminate() {
        Timber.d("onTerminate");
        if (!isFinishing()) {
            @NetworkUtility.ConnectionError String error = ServerBridge.getLastNetworkError();
            if (!TextUtils.isEmpty(error)) {
                onError();
            }
        }
    }

    @Override
    public void onComplete() {
        Timber.d("onComplete");
        onSuccess();
    }

    @Override
    public void onLoading() {
        Timber.d("onLoading");
        mProgressView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        if (!mIsBackPressed) {
            Timber.e("onError");
            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mToolbar.setTitle(R.string.main_label);
    }
}
