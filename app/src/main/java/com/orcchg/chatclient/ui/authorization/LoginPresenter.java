package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;
import com.orcchg.chatclient.ui.chat.ChatActivity;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

public class LoginPresenter extends BasePresenter<LoginMvpView> {

    DataManager mDataManager;  // TODO: inject
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

    void setDirectConnectionCallback() {
        mDataManager.setConnectionCallback(createConnectionCallback());
    }

    void removeDirectConnectionCallback() {
        mDataManager.setConnectionCallback(null);
    }

    /* Login */
    // --------------------------------------------------------------------------------------------
    void requestLoginForm() {
        getMvpView().onLoading();

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
        getMvpView().onLoading();

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
                String userName = status.getPayload();
                Activity activity1 = (Activity) getMvpView();
                Intent intent1 = new Intent(activity1, ChatActivity.class);
                intent1.putExtra(ChatActivity.EXTRA_USER_ID, status.getId());
                intent1.putExtra(ChatActivity.EXTRA_USER_NAME, userName);
                activity1.startActivity(intent1);
                break;
            case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                Timber.d("Wrong password");
                getMvpView().onWrongPassword();
                break;
            case ApiStatusFactory.STATUS_NOT_REGISTERED:
                Timber.d("Not registered");
                Activity activity2 = (Activity) getMvpView();
                Intent intent2 = new Intent(activity2, RegistrationActivity.class);
                activity2.startActivity(intent2);
                break;
            case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                Timber.w("Server's responded with forbidden error: already registered");
                break;
            case ApiStatusFactory.STATUS_ALREADY_LOGGED_IN:
                getMvpView().onAlreadyLoggedIn();
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
            public void onNext(Response response) {
                super.onNext(response);
                LoginPresenter presenter = getPresenterRef().get();
                if (presenter != null) {

                } else {
                    Timber.v("Presenter has already been GC'ed");
                }
            }
        };
    }
}
