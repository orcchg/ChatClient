package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.LoginFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

public class LoginPresenter extends BasePresenter<LoginMvpView> {

    private DataManager mDataManager;  // TODO: inject
    private Subscription mSubscriptionGet;
    private Subscription mSubscriptionSend;

    LoginPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    boolean hasRequestedLoginForm() {
        return mSubscriptionGet != null && !mSubscriptionGet.isUnsubscribed();
    }

    void unsubscribe() {
        if (mSubscriptionGet != null) mSubscriptionGet.unsubscribe();
        if (mSubscriptionSend != null) mSubscriptionSend.unsubscribe();
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

//        final Mapper<LoginForm, AuthFormVO> mapper = new LoginFormMapper();
//
//        mSubscriptionGet = mDataManager.getLoginForm()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .flatMap(new Func1<LoginForm, Observable<AuthFormVO>>() {
//                @Override
//                public Observable<AuthFormVO> call(LoginForm loginForm) {
//                    AuthFormVO viewObject = mapper.map(loginForm);
//                    return Observable.just(viewObject);
//                }
//            }).subscribe(processAuthForm());
        mDataManager.getLoginFormDirect();
    }

    void sendLoginForm() {
        onLoading();

        String login = getMvpView().getLogin();
        String password = getMvpView().getPassword();
        LoginForm form = new LoginForm(login, password);

//        mSubscriptionSend = mDataManager.sendLoginForm(form)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(processStatus());
        mDataManager.sendLoginFormDirect(form);
    }

    // --------------------------------------------------------------------------------------------
    private Observer<AuthFormVO> processAuthForm() {
        return new Observer<AuthFormVO>() {
            @Override
            public void onCompleted() {
                Timber.d("onCompleted (Form)");
                getMvpView().onComplete();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Error (Form): %s", Log.getStackTraceString(e));
                getMvpView().onError();
            }

            @Override
            public void onNext(AuthFormVO viewObject) {
                Timber.d("onNext (Form)");
                getMvpView().showAuthForm(viewObject);
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private Observer<Status> processStatus() {
        return new Observer<Status>() {
            @Override
            public void onCompleted() {
                Timber.d("onCompleted (Status)");
                getMvpView().onComplete();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Error (Status): %s", Log.getStackTraceString(e));
                getMvpView().onError();
            }

            @Override
            public void onNext(Status status) {
                Timber.d("onNext (Status)");
                processStatus(status);
            }
        };
    }

    private void processStatus(Status status) {
        String errorMessage = "";
        boolean flag = false;
        @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
        switch (code) {
            case ApiStatusFactory.STATUS_SUCCESS:
                Timber.i("Successfully logged in");
                long id = status.getId();
                String userName = status.getPayload();
                Activity activity1 = (Activity) getMvpView();
                Utility.logInAndOpenChat(activity1, id, userName);
                activity1.finish();
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
                activity2.finish();
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
                        if (json.has("code")) {
                            Timber.d("Code response: %s", response.getBody());
                            Status status = Status.fromJson(response.getBody());
                            presenter.processStatus(status);
                            return;
                        }

                        Timber.d("Form response: %s", response.getBody());
                        LoginForm form = LoginForm.fromJson(response.getBody());
                        Mapper<LoginForm, AuthFormVO> mapper = new LoginFormMapper();
                        AuthFormVO viewObject = mapper.map(form);
                        presenter.showForm(viewObject);
                        presenter.onComplete();
                        return;

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
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onComplete();
            }
        });
    }

    private void onLoading() {
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onLoading();
            }
        });
    }

    private void onError() {
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
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showAuthForm(viewObject);
            }
        });
    }

    // --------------------------------------------------------------------------------------------
    private void onWrongPassword() {
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onWrongPassword();
            }
        });
    }

    private void onAlreadyLoggedIn() {
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onAlreadyLoggedIn();
            }
        });
    }
}
