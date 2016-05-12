package com.orcchg.chatclient.ui.authorization;

import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.ui.base.BasePresenter;

public class AuthPresenter extends BasePresenter<AuthMvpView> {

    DataManager mDataManager;  // TODO: inject

    AuthPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }
}
