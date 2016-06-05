package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.RegistrationFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.chat.ChatActivity;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RegistrationPresenter extends BasePresenter<RegistrationMvpView> {

    DataManager mDataManager;  // TODO: inject
    private Subscription mSubscriptionGet;
    private Subscription mSubscriptionSend;

    RegistrationPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    boolean hasRequestedRegistrationForm() {
        return mSubscriptionGet != null && !mSubscriptionGet.isUnsubscribed();
    }

    void unsubscribe() {
        if (mSubscriptionGet != null) mSubscriptionGet.unsubscribe();
        if (mSubscriptionSend != null) mSubscriptionSend.unsubscribe();
    }

    /* Registration */
    // --------------------------------------------------------------------------------------------
    void requestRegistrationForm() {
        getMvpView().onLoading();

        final Mapper<RegistrationForm, AuthFormVO> mapper = new RegistrationFormMapper();

        mSubscriptionGet = mDataManager.getRegistrationForm()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Func1<RegistrationForm, Observable<AuthFormVO>>() {
                @Override
                public Observable<AuthFormVO> call(RegistrationForm registrationForm) {
                    AuthFormVO viewObject = mapper.map(registrationForm);
                    return Observable.just(viewObject);
                }
            })
            .subscribe(processAuthForm());
    }

    void sendRegistrationForm() {
        getMvpView().onLoading();

        String login = getMvpView().getLogin();
        String email = getMvpView().getEmail();
        String password = getMvpView().getPassword();
        RegistrationForm form = new RegistrationForm(login, email, password);

        mSubscriptionSend = mDataManager.sendRegistrationForm(form)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(processStatus());
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
                @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
                switch (code) {
                    case ApiStatusFactory.STATUS_SUCCESS:
                        Timber.i("Successfully registered");
                        String userName = getMvpView().getLogin();
                        Activity activity1 = (Activity) getMvpView();
                        Intent intent1 = new Intent(activity1, ChatActivity.class);
                        intent1.putExtra(ChatActivity.EXTRA_USER_ID, status.getId());
                        intent1.putExtra(ChatActivity.EXTRA_USER_NAME, userName);
                        activity1.startActivity(intent1);
                        break;
                    case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                        Timber.e("Server's responded with forbidden error: wrong password");
                        break;
                    case ApiStatusFactory.STATUS_NOT_REGISTERED:
                        Timber.e("Server's responded with forbidden error: not registered");
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                        getMvpView().onAlreadyRegistered();
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_LOGGED_IN:
                        getMvpView().onAlreadyLoggedIn();
                        break;
                    case ApiStatusFactory.STATUS_INVALID_FORM:
                        String message = "Client's requested with invalid form";
                        Timber.e(message);
                        throw new RuntimeException(message);
                    case ApiStatusFactory.STATUS_INVALID_QUERY:
                        Timber.e("Server's responded with forbidden error: invalid query");
                        break;
                    case ApiStatusFactory.STATUS_UNAUTHORIZED:
                        Timber.e("Server's responded with forbidden error: unauthorized");
                        break;
                    case ApiStatusFactory.STATUS_UNKNOWN:
                    default:
                        Timber.d("Unknown status");
                        break;
                }
            }
        };
    }

}
