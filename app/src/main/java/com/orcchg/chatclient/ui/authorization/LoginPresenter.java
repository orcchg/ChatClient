package com.orcchg.chatclient.ui.authorization;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.LoginFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginPresenter extends BasePresenter<LoginMvpView> {

    DataManager mDataManager;  // TODO: inject

    LoginPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    /* Login */
    // --------------------------------------------------------------------------------------------
    void requestLoginForm() {
        final Mapper<LoginForm, AuthFormVO> mapper = new LoginFormMapper();

        mDataManager.getLoginForm()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Func1<LoginForm, Observable<AuthFormVO>>() {
                @Override
                public Observable<AuthFormVO> call(LoginForm loginForm) {
                    AuthFormVO viewObject = mapper.map(loginForm);
                    return Observable.just(viewObject);
                }
            }).subscribe(processAuthForm());
    }

    void sendLoginForm() {
        String login = getMvpView().getLogin();
        String password = getMvpView().getPassword();
        LoginForm form = new LoginForm(login, password);

        mDataManager.sendLoginForm(form)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(processStatus());
    }

    // --------------------------------------------------------------------------------------------
    private Observer<AuthFormVO> processAuthForm() {
        return new Observer<AuthFormVO>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(AuthFormVO viewObject) {
                getMvpView().showAuthForm(viewObject);
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private Observer<Status> processStatus() {
        return new Observer<Status>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Status status) {
                @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
                switch (code) {
                    case ApiStatusFactory.STATUS_SUCCESS:
                        // TODO: success logged in, registered + logged in --> start chat
                        break;
                    case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                        // TODO: retry password in login form
                        break;
                    case ApiStatusFactory.STATUS_NOT_REGISTERED:
                        // TODO: login failed --> go to registration
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                        // TODO: warning in registration form
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_LOGGED_IN:
                        // TODO: warning in login form
                        break;
                    case ApiStatusFactory.STATUS_INVALID_FORM:
                        // TODO: system error
                        break;
                    case ApiStatusFactory.STATUS_INVALID_QUERY:
                        // TODO: system error
                        break;
                    case ApiStatusFactory.STATUS_UNAUTHORIZED:
                        // TODO: system error, unreachable from auth screen
                        break;
                    case ApiStatusFactory.STATUS_UNKNOWN:
                    default:
                        break;
                }
            }
        };
    }
}
