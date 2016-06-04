package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.LoginForm;

public class LoginFormMapper implements Mapper<LoginForm, AuthFormVO> {
    @Override
    public AuthFormVO map(LoginForm object) {
        return new AuthFormVO.Builder()
                .setLogin(object.getLogin())
                .setPassword(object.getPassword())
                .build();
    }
}
