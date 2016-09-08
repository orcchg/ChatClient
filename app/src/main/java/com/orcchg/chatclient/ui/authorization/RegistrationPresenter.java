package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.util.Log;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.RegistrationFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;
import com.orcchg.chatclient.util.SharedUtility;
import com.orcchg.chatclient.util.crypting.Cryptor;
import com.orcchg.chatclient.util.crypting.SecurityUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import timber.log.Timber;

public class RegistrationPresenter extends BasePresenter<RegistrationMvpView> {

    private DataManager mDataManager;

    private String mPlainPassword;

    RegistrationPresenter(DataManager dataManager) {
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

    /* Registration */
    // --------------------------------------------------------------------------------------------
    private void requestRegistrationForm() {
        onLoading();
        mDataManager.getRegistrationFormDirect();
    }

    void sendRegistrationForm() {
        if (!isViewAttached()) return;
        onLoading();

        String login = getMvpView().getLogin();
        String email = getMvpView().getEmail();
        String password = getMvpView().getPassword();
        mPlainPassword = password;
        password = Cryptor.encrypt(password);
        RegistrationForm form = new RegistrationForm(login, email, password);
        if (SecurityUtility.isSecurityEnabled((Activity) getMvpView())) {
            form.encrypt(SecurityUtility.getServerPublicKey());
        }

        mDataManager.sendRegistrationFormDirect(form);
    }

    // --------------------------------------------------------------------------------------------
    private void processStatus(Status status) {
        if (!isViewAttached()) return;

        String errorMessage = "";
        boolean flag = false;
        @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
        switch (code) {
            case ApiStatusFactory.STATUS_SUCCESS:
                Timber.i("Successfully registered");
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
                Timber.w("Server's responded with forbidden error: wrong password");
                break;
            case ApiStatusFactory.STATUS_NOT_REGISTERED:
                Timber.w("Server's responded with forbidden error: not registered");
                break;
            case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                onAlreadyRegistered();
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
                Timber.w("Server's responded with forbidden error: unauthorized");
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
        return new SimpleConnectionCallback<RegistrationPresenter>(this) {
            @Override
            public void onSuccess() {
                super.onSuccess();
                requestRegistrationForm();
            }

            @Override
            public void onNext(Response response) {
                super.onNext(response);
                RegistrationPresenter presenter = getPresenterRef().get();
                if (presenter != null) {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        if (json.has("code")) {
                            Timber.d("Code response: %s", response.getBody());
                            Status status = Status.fromJson(response.getBody());
                            presenter.processStatus(status);
                            return;
                        }

                        Timber.d("Form response: %s", response.getBody());
                        RegistrationForm form = RegistrationForm.fromJson(response.getBody());
                        Mapper<RegistrationForm, AuthFormVO> mapper = new RegistrationFormMapper();
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
    private void onAlreadyRegistered() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onAlreadyRegistered();
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
