package com.orcchg.chatclient.ui.main;

import com.orcchg.chatclient.ui.base.BaseActivity;

import timber.log.Timber;

/**
 * This Activity has no view.
 */
public class MainActivity extends BaseActivity<MainPresenter> implements MainMvpView {

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onComplete() {
    }

    @Override
    public void onError() {
    }

    @Override
    public void onLoading() {
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    private void start() {
        if (mPresenter.isLoggedIn()) {
            Timber.d("User is logged in");
            mPresenter.openChatActivity();
        } else {
            Timber.d("User is not logged in");
            mPresenter.openLoginActivity();
        }
    }
}
