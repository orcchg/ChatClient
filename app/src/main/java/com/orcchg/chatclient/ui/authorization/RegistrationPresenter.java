package com.orcchg.chatclient.ui.authorization;

import android.app.Activity;
import android.content.Intent;
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
import com.orcchg.chatclient.ui.chat.ChatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observer;
import rx.Subscription;
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

    void setDirectConnectionCallback() {
        mDataManager.setConnectionCallback(createConnectionCallback());
    }

    void removeDirectConnectionCallback() {
        mDataManager.setConnectionCallback(null);
    }

    /* Registration */
    // --------------------------------------------------------------------------------------------
    private void requestRegistrationForm() {
        onLoading();

//        final Mapper<RegistrationForm, AuthFormVO> mapper = new RegistrationFormMapper();
//
//        mSubscriptionGet = mDataManager.getRegistrationForm()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .flatMap(new Func1<RegistrationForm, Observable<AuthFormVO>>() {
//                @Override
//                public Observable<AuthFormVO> call(RegistrationForm registrationForm) {
//                    AuthFormVO viewObject = mapper.map(registrationForm);
//                    return Observable.just(viewObject);
//                }
//            })
//            .subscribe(processAuthForm());
        mDataManager.getRegistrationFormDirect();
    }

    void sendRegistrationForm() {
        onLoading();

        String login = getMvpView().getLogin();
        String email = getMvpView().getEmail();
        String password = getMvpView().getPassword();
        RegistrationForm form = new RegistrationForm(login, email, password);

//        mSubscriptionSend = mDataManager.sendRegistrationForm(form)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(processStatus());
        mDataManager.sendRegistrationFormDirect(form);
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
                Timber.i("Successfully registered");
                String userName = status.getPayload();
                Activity activity1 = (Activity) getMvpView();
                Intent intent1 = new Intent(activity1, ChatActivity.class);
                intent1.putExtra(ChatActivity.EXTRA_USER_ID, status.getId());
                intent1.putExtra(ChatActivity.EXTRA_USER_NAME, userName);
                activity1.startActivity(intent1);
                break;
            case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                Timber.w("Server's responded with forbidden error: wrong password");
                break;
            case ApiStatusFactory.STATUS_NOT_REGISTERED:
                Timber.w("Server's responded with forbidden error: not registered");
                break;
            case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                getMvpView().onAlreadyRegistered();
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
                        return;

                    } catch (JSONException e) {
                        Timber.e("Server has responed with malformed json body: %s", response.getBody());
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

    private void showForm(final AuthFormVO viewObject) {
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showAuthForm(viewObject);
            }
        });
    }
}
