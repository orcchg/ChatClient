package com.orcchg.chatclient.ui.authorization;

import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.MvpView;

public interface RegistrationMvpView extends MvpView {
    void showAuthForm(AuthFormVO viewObject);

    void onAlreadyLoggedIn();
    void onAlreadyRegistered();

    String getLogin();
    String getEmail();
    String getPassword();
}
