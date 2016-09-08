package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Check;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.LoginFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;
import com.orcchg.chatclient.util.SharedUtility;
import com.orcchg.chatclient.util.crypting.Cryptor;
import com.orcchg.chatclient.util.crypting.SecurityUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import timber.log.Timber;

public class LoginPresenter extends BasePresenter<LoginMvpView> {

    private DataManager mDataManager;

    private String mPlainPassword;

    LoginPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    // --------------------------------------------------------------------------------------------
    void openDirectConnection() {
        Timber.v("openDirectConnection");
        mDataManager.openDirectConnection();
    }

    void setDirectConnectionCallback() {
        Timber.v("setDirectConnectionCallback");
        mDataManager.setConnectionCallback(createConnectionCallback());
    }

    void removeDirectConnectionCallback() {
        Timber.v("removeDirectConnectionCallback");
        mDataManager.setConnectionCallback(null);
    }

    void closeDirectConnection() {
        Timber.v("closeDirectConnection");
        mDataManager.closeDirectConnection();
    }

    /* Login */
    // --------------------------------------------------------------------------------------------
    private  void requestLoginForm() {
        onLoading();
        mDataManager.getLoginFormDirect();
    }

    void sendLoginForm() {
        if (!isViewAttached()) return;
        onLoading();
        LoginForm form = prepareLoginForm();
        mDataManager.sendLoginFormDirect(form);
    }

    private void checkAuth() {
        if (!isViewAttached()) return;
        onLoading();
        LoginForm form = prepareLoginForm();
        mDataManager.checkAuthDirect(form);
    }

    private void kickByAuth() {
        if (!isViewAttached()) return;
        onLoading();
        LoginForm form = prepareLoginForm();
        mDataManager.kickByAuthDirect(form);
    }

    void prepareToLogoutOnAllDevices() {
        kickByAuth();  // to get user id
    }

    private LoginForm prepareLoginForm() {
        String login = getMvpView().getLogin();
        String password = getMvpView().getPassword();
        mPlainPassword = password;
        password = Cryptor.encrypt(password);
        LoginForm form = new LoginForm(login, password);
        if (SecurityUtility.isSecurityEnabled((Activity) getMvpView())) {
            form.encrypt(SecurityUtility.getServerPublicKey());
        }
        return form;
    }

    // --------------------------------------------------------------------------------------------
    private void processCheck(Check check) {
        @Status.Action int action = check.getAction();
        Timber.v("Processing check: %s", check.toString());
        switch (action) {
            case Status.ACTION_KICK_BY_AUTH:
                if (check.getCheck() == Check.CHECK_TRUE) {
                    Timber.i("Successfully logged out on all devices");
                    showSnackbar(R.string.logout_on_all_devices_toast_message);
                } else {
                    Timber.w("Authentication failed !");
                    onWrongPassword();
                }
                break;
        }
    }

    private void processStatus(Status status, final @Status.Action int action) {
        if (!isViewAttached()) return;

        String errorMessage = "";
        boolean flag = false;
        @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
        switch (code) {
            case ApiStatusFactory.STATUS_SUCCESS:
                Timber.i("Successfully logged in");
                long id = status.getId();
                Map<String, String> map = SharedUtility.splitPayload(status.getPayload());
                String userName = map.get("login");
                String userEmail = map.get("email");
                Activity activity1 = (Activity) getMvpView();
                Utility.logInAndOpenChat(activity1, id, userName, userEmail);
                SharedUtility.storePassword(activity1, mPlainPassword);
                getMvpView().finishView();
                break;
            case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                Timber.d("Wrong password");
                onWrongPassword();
                break;
            case ApiStatusFactory.STATUS_NOT_REGISTERED:
                Timber.d("Not registered");
                Activity activity2 = (Activity) getMvpView();
                Intent intent2 = new Intent(activity2, RegistrationActivity.class);
                activity2.startActivity(intent2);
                getMvpView().finishView();
                break;
            case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                Timber.w("Server's responded with forbidden error: already registered");
                break;
            case ApiStatusFactory.STATUS_ALREADY_LOGGED_IN:
                onAlreadyLoggedIn();
                break;
            case ApiStatusFactory.STATUS_INVALID_FORM:
                errorMessage = "Client's requested with invalid form";
                flag = true;
            case ApiStatusFactory.STATUS_INVALID_QUERY:
                if (!flag) {
                    errorMessage = "Client's requested with invalid query";
                }
                Timber.e(errorMessage);
                throw new RuntimeException(errorMessage);
            case ApiStatusFactory.STATUS_UNAUTHORIZED:
                Timber.w("Server's responded with forbidden error: already unauthorized");
                showSnackbar(R.string.unauthorized_toast_message);
                break;
            case ApiStatusFactory.STATUS_WRONG_CHANNEL:
                Timber.w("Server's responded with forbidden error: wrong channel");
                break;
            case ApiStatusFactory.STATUS_UNKNOWN:
            default:
                Timber.d("Unknown status");
                break;
        }
    }

    /* Direct connection */
    // --------------------------------------------------------------------------------------------
    private ServerBridge.ConnectionCallback createConnectionCallback() {
        return new SimpleConnectionCallback<LoginPresenter>(this) {
            @Override
            public void onSuccess() {
                super.onSuccess();
                requestLoginForm();
            }

            @Override
            public void onNext(Response response) {
                super.onNext(response);
                LoginPresenter presenter = getPresenterRef().get();
                if (presenter != null) {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        if (json.has("check")) {
                            Timber.d("Check response: %s", response.getBody());
                            Check check = Check.fromJson(response.getBody());
                            presenter.onComplete();
                            presenter.processCheck(check);
                            return;
                        }

                        if (json.has("code")) {
                            Timber.d("Code response: %s", response.getBody());
                            Status status = Status.fromJson(response.getBody());
                            presenter.processStatus(status, status.getAction());
                            return;
                        }

                        Timber.d("Form response: %s", response.getBody());
                        LoginForm form = LoginForm.fromJson(response.getBody());
                        Mapper<LoginForm, AuthFormVO> mapper = new LoginFormMapper();
                        AuthFormVO viewObject = mapper.map(form);
                        presenter.showForm(viewObject);
                        presenter.onComplete();

                    } catch (JSONException e) {
                        Timber.e("Server has responded with malformed json body: %s", response.getBody());
                        Timber.e("%s", e.getMessage());
                        Timber.w("%s", Log.getStackTraceString(e));
                        presenter.onError();
                    }
                } else {
                    Timber.v("Presenter has already been GC'ed");
                }
            }
        };
    }

    /* View state */
    // --------------------------------------------------------------------------------------------
    private void onComplete() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onComplete();
            }
        });
    }

    private void onLoading() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onLoading();
            }
        });
    }

    private void onError() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onError();
            }
        });
    }

    void onRetry() {
        setDirectConnectionCallback();
        openDirectConnection();
    }

    private void showForm(final AuthFormVO viewObject) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showAuthForm(viewObject);
            }
        });
    }

    // --------------------------------------------------------------------------------------------
    private void onWrongPassword() {
        if (!isViewAttached()) return;
        mPlainPassword = null;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onWrongPassword();
            }
        });
    }

    private void onAlreadyLoggedIn() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onAlreadyLoggedIn();
            }
        });
    }
}
