package com.orcchg.chatclient.ui.authorization;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegistrationActivity extends BaseActivity<RegistrationPresenter> implements RegistrationMvpView {

    @Bind(R.id.login) AutoCompleteTextView mLoginView;
    @Bind(R.id.email) AutoCompleteTextView mEmailView;
    @Bind(R.id.password) EditText mPasswordView;
    @Bind(R.id.sign_up_button) Button mSignUpButton;
    @Bind(R.id.form_container_scroll) View mFormContainer;
    @Bind(R.id.progress) View mProgressView;

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
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.requestRegistrationForm();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unsubscribe();
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showAuthForm(AuthFormVO viewObject) {
        mLoginView.setText(viewObject.getLogin());
        mEmailView.setText(viewObject.getEmail());
        mPasswordView.setText(viewObject.getPassword());
    }

    @Override
    public void onAlreadyLoggedIn() {
        mLoginView.setError(getString(R.string.error_already_logged_in));
        mLoginView.requestFocus();
    }

    @Override
    public void onAlreadyRegistered() {
        mLoginView.setError(getString(R.string.error_already_registered));
        mLoginView.requestFocus();
    }

    @Override
    public String getLogin() {
        return mLoginView.getText().toString();
    }

    @Override
    public String getEmail() {
        return mEmailView.getText().toString();
    }

    @Override
    public String getPassword() {
        return mPasswordView.getText().toString();
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    private void attemptRegister() {
        if (mPresenter.hasRequestedRegistrationForm()) {
            return;
        }

        mLoginView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String login = mLoginView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !Utility.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Utility.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Utility.showProgress(getResources(), mFormContainer, mProgressView, true);
            mPresenter.sendRegistrationForm();
        }
    }
}
