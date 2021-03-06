package com.orcchg.chatclient.ui.authorization;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
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
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.NetworkUtility;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RegistrationActivity extends BaseActivity<RegistrationPresenter> implements RegistrationMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.REGISTRATION_ACTIVITY;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.login) AutoCompleteTextView mLoginView;
    @BindView(R.id.email) AutoCompleteTextView mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.sign_up_button) Button mSignUpButton;
    @BindView(R.id.form_container_scroll) View mFormContainer;
    @BindView(R.id.progress) View mProgressView;
    @BindView(R.id.error) View mErrorView;
    @BindView(R.id.retry_button) Button mRetryButton;
    @BindView(R.id.optional_text) TextView mOptionalText;

    private View mFocusedView;

    private boolean mIsBackPressed;

    @Override
    protected RegistrationPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        return new RegistrationPresenter(application.getDataManager());
    }

    @Override
    @FrameworkUtility.RequestCode.Code
    protected int getActivityRequestCode() {
        return REQUEST_CODE;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FrameworkUtility.isActive(FrameworkUtility.RequestCode.CHAT_ACTIVITY) &&
            !FrameworkUtility.isFinishing(FrameworkUtility.RequestCode.CHAT_ACTIVITY)/* &&
            !ServerBridge.hasNetworkError()*/) {
            Timber.w("Chat is still alive but paused. Go to Chat instead");
            finish();
            return;
        }

        mIsBackPressed = false;
        FrameworkUtility.setActive(REQUEST_CODE);
        FrameworkUtility.diagnostic();
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
    protected void onStop() {
        super.onStop();
        FrameworkUtility.setFinishing(REQUEST_CODE, isFinishing());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        FrameworkUtility.setInactive(REQUEST_CODE);
        FrameworkUtility.diagnostic();
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
        Timber.d("onSuccess");
        Utility.showProgress(getResources(), mFormContainer, mProgressView, false);
        mErrorView.setVisibility(View.GONE);
        if (mFocusedView != null) mFocusedView.requestFocus();
    }

    @Override
    public void onTerminate() {
        Timber.d("onTerminate");
        if (!isFinishing()) {
            @NetworkUtility.ConnectionError String error = ServerBridge.getLastNetworkError();
            if (!TextUtils.isEmpty(error)) {
                onError();
            }
        }
    }

    @Override
    public void onLoading() {
        Timber.d("onLoading");
        Utility.showProgress(getResources(), mFormContainer, mProgressView, true);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void onComplete() {
        Timber.d("onComplete");
        onSuccess();
    }

    @Override
    public void onError() {
        if (!mIsBackPressed) {
            Timber.e("onError");
            mFormContainer.setVisibility(View.GONE);
            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
        }
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
        return mPasswordView.getText().toString();
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    private void attemptRegister() {
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
