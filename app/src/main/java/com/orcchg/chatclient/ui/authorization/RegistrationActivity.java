package com.orcchg.chatclient.ui.authorization;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.crypting.Cryptor;

import java.security.NoSuchAlgorithmException;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RegistrationActivity extends BaseActivity<RegistrationPresenter> implements RegistrationMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.REGISTRATION_ACTIVITY;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.login) AutoCompleteTextView mLoginView;
    @Bind(R.id.email) AutoCompleteTextView mEmailView;
    @Bind(R.id.password) EditText mPasswordView;
    @Bind(R.id.sign_up_button) Button mSignUpButton;
    @Bind(R.id.form_container_scroll) View mFormContainer;
    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;

    private View mFocusedView;

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
        FrameworkUtility.setActive(REQUEST_CODE);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        initToolbar();

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

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
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
    protected void onResume() {
        super.onResume();
        mPresenter.onRetry();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.removeDirectConnectionCallback();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unsubscribe();
        FrameworkUtility.setInactive(REQUEST_CODE);
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
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
    public void onSuccess() {
        onComplete();
    }

    @Override
    public void onComplete() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mErrorView.setVisibility(View.GONE);
        if (mFocusedView != null) mFocusedView.requestFocus();
    }

    @Override
    public void onError() {
        mFormContainer.setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoading() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, true);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void onAlreadyLoggedIn() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mEmailView.setError(getString(R.string.error_already_logged_in));
        mFocusedView = mEmailView;
        if (mFocusedView != null) mFocusedView.requestFocus();
    }

    @Override
    public void onAlreadyRegistered() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mEmailView.setError(getString(R.string.error_already_registered));
        mFocusedView = mEmailView;
        if (mFocusedView != null) mFocusedView.requestFocus();
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
        String data = mPasswordView.getText().toString();
        try {
            return Cryptor.hash256(data);
        } catch (NoSuchAlgorithmException e) {
            Timber.e("Failed to encrypt password: %s", Log.getStackTraceString(e));
        }
        return data;
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

        if (Utility.tooShort(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (Utility.tooShort(login)) {
            @StringRes int resId = TextUtils.isEmpty(login) ? R.string.error_field_required : R.string.error_field_too_short;
            mLoginView.setError(getString(resId));
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

        mFocusedView = focusView;

        if (cancel) {
            focusView.requestFocus();
        } else {
            mPresenter.sendRegistrationForm();
        }
    }

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mToolbar.setTitle(R.string.registration_label);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
