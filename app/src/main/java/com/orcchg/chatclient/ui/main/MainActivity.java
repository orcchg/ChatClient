package com.orcchg.chatclient.ui.main;

import com.orcchg.chatclient.ui.base.BaseActivity;

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
        if (!mPresenter.isLoggedIn()) {
            mPresenter.openLoginActivity();
        } else {
            mPresenter.openChatActivity();
        }
    }
}
