package com.orcchg.chatclient.ui.authorization;

import android.os.Bundle;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.ui.base.BaseActivity;

public class RegistrationActivity extends BaseActivity<RegistrationPresenter> implements RegistrationMvpView {

    @Override
    protected RegistrationPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return new RegistrationPresenter(application.getDataManager());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
}
