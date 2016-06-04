package com.orcchg.chatclient.ui.authorization;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.AuthFormVO;
import com.orcchg.chatclient.data.viewobject.RegistrationFormMapper;
import com.orcchg.chatclient.ui.base.BasePresenter;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RegistrationPresenter extends BasePresenter<RegistrationMvpView> {

    DataManager mDataManager;  // TODO: inject

    RegistrationPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    /* Registration */
    // --------------------------------------------------------------------------------------------
    void requestRegistrationForm() {
        final Mapper<RegistrationForm, AuthFormVO> mapper = new RegistrationFormMapper();

        mDataManager.getRegistrationForm()
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
        String login = getMvpView().getLogin();
        String email = getMvpView().getEmail();
        String password = getMvpView().getPassword();
        RegistrationForm form = new RegistrationForm(login, email, password);

        mDataManager.sendRegistrationForm(form)
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
