package com.orcchg.chatclient.ui.authorization;

import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.MvpView;

public interface LoginMvpView extends MvpView {
    void showAuthForm(AuthFormVO viewObject);

    void onWrongPassword();
    void onAlreadyLoggedIn();

    String getLogin();
    String getPassword();
}
