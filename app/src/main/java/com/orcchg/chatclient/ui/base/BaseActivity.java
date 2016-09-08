package com.orcchg.chatclient.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.NetworkUtility;
import com.orcchg.chatclient.util.crypting.SecurityUtility;

import timber.log.Timber;

/**
 * Base class for every {@link Activity} in MVP architecture.
 */
public abstract class BaseActivity<P extends Presenter> extends AppCompatActivity implements MvpView {
    protected final String TAG = this.getClass().getSimpleName();

    protected ViewGroup mRootContainer;

    protected P mPresenter;

    protected abstract P createPresenter();

    @FrameworkUtility.RequestCode.Code
    protected abstract int getActivityRequestCode();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mRootContainer = (ViewGroup) findViewById(R.id.root_container);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);
        Timber.v("+++ onCreate() +++");
        SecurityUtility.enableSecurity(this, true);
        mPresenter = createPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.tag(TAG);
        Timber.v("+++ onStart() +++");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.tag(TAG);
        Timber.v("+++ onResume() +++");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.tag(TAG);
        Timber.v("+++ onPause() +++");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.tag(TAG);
        Timber.v("+++ onStop() +++");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.tag(TAG);
        Timber.v("+++ onDestroy() +++");
        mPresenter.detachView();
        mPresenter = null;
    }

    @Override
    public void postOnUiThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override
    public void finishView() {
        FrameworkUtility.setFinishing(getActivityRequestCode(), true);
        finish();
    }

    @Override
    public void onNetworkError(@NetworkUtility.ConnectionError String error) {
        // override in subclasses
    }

    protected boolean isLoggingOut() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return application.getDataManager().isLoggingOut();
    }

    @Override
    public void showSnackbar(String message, int duration) {
        Snackbar.make(mRootContainer, message, duration).show();
    }

    @Override
    public void showSnackbar(@StringRes int resId, int duration) {
        String message = getResources().getString(resId);
        Snackbar.make(mRootContainer, message, duration).show();
    }
}
