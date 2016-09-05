package com.orcchg.chatclient.ui.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
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
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.NetworkUtility;
import com.orcchg.chatclient.util.SharedUtility;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.LOGIN_ACTIVITY;

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

    private boolean mIsBackPressed;

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
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initToolbar();

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
        mPresenter.closeDirectConnection();
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
        mEmailView.setText(viewObject.getLogin());
        mPasswordView.setText(viewObject.getPassword());
        fillLoginFormFromStorage();
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
        return mPasswordView.getText().toString();
    }

    @Override
    public void fillLoginFormFromStorage() {
        String userEmail = SharedUtility.getEmail(this);
        String password = SharedUtility.getPassword(this);
        if (!TextUtils.isEmpty(userEmail)) {
            mEmailView.setText(userEmail);
        }
        if (!TextUtils.isEmpty(password)) {
            mPasswordView.setText(password);
        }
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    private boolean checkFields() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;

        if (Utility.tooShort(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mFocusedView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mFocusedView = mEmailView;
            cancel = true;
        } else if (!Utility.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mFocusedView = mEmailView;
            cancel = true;
        }

        return cancel;
    }

    private void attemptLogin() {
        if (checkFields()) {
            mFocusedView.requestFocus();
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
//        mToolbar.inflateMenu(R.menu.login_menu);
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.force_logout:
//                        onLogoutOnAllDevices();
//                        return true;
//                }
//                return false;
//            }
//        });
    }

    private void onLogoutOnAllDevices() {
        Timber.d("Logout on all devices clicked");
        if (checkFields()) {
            mFocusedView.requestFocus();
        } else {
            mPresenter.prepareToLogoutOnAllDevices();
        }
    }
}

