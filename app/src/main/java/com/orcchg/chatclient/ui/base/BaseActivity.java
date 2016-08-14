package com.orcchg.chatclient.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orcchg.chatclient.util.crypting.SecurityUtility;

/**
 * Base class for every {@link Activity} in MVP architecture.
 */
public abstract class BaseActivity<P extends Presenter> extends AppCompatActivity implements MvpView {

    protected P mPresenter;

    protected abstract P createPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtility.enableSecurity(this, true);
        mPresenter = createPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mPresenter = null;
    }

    @Override
    public void postOnUiThread(Runnable runnable) {
        runOnUiThread(runnable);
    }
}
