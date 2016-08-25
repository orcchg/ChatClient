package com.orcchg.chatclient.ui.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.LOGIN_ACTIVITY;
    private static String SHARED_PREFS_KEY_USER_EMAIL;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.email) AutoCompleteTextView mEmailView;
    @Bind(R.id.password) EditText mPasswordView;
    @Bind(R.id.btn_clear_email) ImageButton mClearEmailButton;
    @Bind(R.id.sign_in_button) Button mSignInButton;
    @Bind(R.id.sign_up_button) Button mSignUpButton;
    @Bind(R.id.form_container_scroll) View mFormContainer;
    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;
    
    private View mFocusedView;

    @Override
    protected LoginPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return new LoginPresenter(application.getDataManager());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameworkUtility.setActive(REQUEST_CODE);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initToolbar();

        SHARED_PREFS_KEY_USER_EMAIL = getResources().getString(R.string.shared_prefs_user_email_key);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
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
        mClearEmailButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailView.setText("");
            }
        });
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegistration();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra(SHARED_PREFS_KEY_USER_EMAIL);
        if (!TextUtils.isEmpty(userEmail)) {
            mEmailView.setText(userEmail);
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        mPresenter.closeDirectConnection();
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
        mEmailView.setText(viewObject.getLogin());
        mPasswordView.setText(viewObject.getPassword());
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
    public void onWrongPassword() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        mFocusedView = mPasswordView;
        if (mFocusedView != null) mFocusedView.requestFocus();
    }

    @Override
    public void onAlreadyLoggedIn() {
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mEmailView.setError(getString(R.string.error_already_logged_in));
        mFocusedView = mEmailView;
        if (mFocusedView != null) mFocusedView.requestFocus();
    }

    @Override
    public String getLogin() {
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
    private void attemptLogin() {
        if (mPresenter.hasRequestedLoginForm()) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (Utility.tooShort(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
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
            mPresenter.sendLoginForm();
        }
    }

    private void openRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mToolbar.setTitle(R.string.login_label);
    }
}

