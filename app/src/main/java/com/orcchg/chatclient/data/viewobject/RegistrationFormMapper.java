package com.orcchg.chatclient.data.viewobject;

import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.RegistrationForm;

public class RegistrationFormMapper implements Mapper<RegistrationForm, AuthFormVO> {
    @Override
    public AuthFormVO map(RegistrationForm object) {
        return new AuthFormVO.Builder()
                .setLogin(object.getLogin())
                .setEmail(object.getEmail())
                .setPassword(object.getPassword())
                .build();
    }
}
