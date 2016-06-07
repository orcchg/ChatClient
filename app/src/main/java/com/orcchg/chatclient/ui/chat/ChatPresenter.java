package com.orcchg.chatclient.ui.chat;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.util.Log;

import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.MessageMapper;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.mock.MockProvider;
import com.orcchg.chatclient.ui.base.BasePresenter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ChatPresenter extends BasePresenter<ChatMvpView> {

    private static final int ACTION_UNKNOWN = -1;
    private static final int ACTION_MESSAGE = 0;
    private static final int ACTION_LOGOUT = 1;
    private static final int ACTION_SWITCH_CHANNEL = 2;
    @IntDef({ACTION_UNKNOWN, ACTION_MESSAGE, ACTION_LOGOUT, ACTION_SWITCH_CHANNEL})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Action {}

    DataManager mDataManager;  // TODO: inject
    List<MessageVO> mMessagesList;
    ChatAdapter mChatAdapter;

    private final long mUserId;
    private final String mUserName;
    private int mCurrentChannel;
    private long mDestId;
    private MessageVO mLastMessage;

    private Subscription mSubscriptionSend;
    private Subscription mSubscriptionLogout;

    ChatPresenter(DataManager dataManager, long id, String name) {
        mDataManager = dataManager;
        mUserId = id;
        mUserName = name;

        mMessagesList = new ArrayList<>();
        mChatAdapter = new ChatAdapter(mUserId, mMessagesList);
    }

    ChatAdapter getChatAdapter() {
        return mChatAdapter;
    }

    void unsubscribe() {
        if (mSubscriptionSend != null) mSubscriptionSend.unsubscribe();
        if (mSubscriptionLogout != null) mSubscriptionLogout.unsubscribe();
    }

    /* Chat */
    // --------------------------------------------------------------------------------------------
    void loadMessages() {
        getMvpView().onLoading();

        final Mapper<Message, MessageVO> mapper = new MessageMapper();

        Observable.from(MockProvider.createMessages())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Func1<Message, Observable<MessageVO>>() {
                @Override
                public Observable<MessageVO> call(Message message) {
                    MessageVO viewObject = mapper.map(message);
                    return Observable.just(viewObject);
                }
            }).subscribe(createObserver());
    }

    void sendMessage() {
        String messageString = getMvpView().getMessage();
        Message message = new Message.Builder(mUserId, mUserName)
            .setChannel(mCurrentChannel)
            .setDestId(mDestId)
            .setTimestamp(System.currentTimeMillis())
            .setMessage(messageString)
            .build();

        final Mapper<Message, MessageVO> mapper = new MessageMapper();
        mLastMessage = mapper.map(message);

        mSubscriptionSend = mDataManager.sendMessage(message)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(processStatus(ACTION_MESSAGE));
    }

    void logout() {
        getMvpView().onLoading();
        mSubscriptionLogout = mDataManager.logout(mUserId, mUserName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(processStatus(ACTION_LOGOUT));
    }

    // --------------------------------------------------------------------------------------------
    private Observer<MessageVO> createObserver() {
        return new Observer<MessageVO>() {
            @Override
            public void onCompleted() {
                Timber.d("onComplete (Message)");
                mChatAdapter.notifyDataSetChanged();
                getMvpView().onComplete();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Error (Message): %s", Log.getStackTraceString(e));
                getMvpView().onError();
            }

            @Override
            public void onNext(MessageVO viewObject) {
                Timber.d("onNext (Message): %s", viewObject.getLogin());
                mMessagesList.add(viewObject);
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private Observer<Status> processStatus(final @Action int action) {
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
                String errorMessage = "";
                boolean flag = false;
                @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
                switch (code) {
                    case ApiStatusFactory.STATUS_SUCCESS:
                        switch (action) {
                            case ACTION_MESSAGE:
                                Timber.i("Successfully sent message");
                                showLastMessage();
                                break;
                            case ACTION_LOGOUT:
                                Timber.i("Successfully logged out");
                                Activity activity = (Activity) getMvpView();
                                if (!activity.isFinishing()) {
                                    activity.finish();
                                }
                                break;
                            case ACTION_SWITCH_CHANNEL:
                                Timber.i("Successfully switched channel");
                                // TODO: switch channel
                                break;
                            case ACTION_UNKNOWN:
                            default:
                                Timber.d("Unknown action on Success");
                                break;
                        }
                        break;
                    case ApiStatusFactory.STATUS_WRONG_PASSWORD:
                        Timber.w("Server's responded with forbidden error: wrong password");
                        break;
                    case ApiStatusFactory.STATUS_NOT_REGISTERED:
                        Timber.w("Server's responded with forbidden error: not registered");
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_REGISTERED:
                        Timber.w("Server's responded with forbidden error: already registered");
                        break;
                    case ApiStatusFactory.STATUS_ALREADY_LOGGED_IN:
                        Timber.w("Server's responded with forbidden error: already logged in");
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
                        // TODO: not logged in
                        break;
                    case ApiStatusFactory.STATUS_UNKNOWN:
                    default:
                        Timber.d("Unknown status");
                        break;
                }
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private void showLastMessage() {
        mMessagesList.add(mLastMessage);
        mLastMessage = null;

        mChatAdapter.notifyItemInserted(mMessagesList.size());
    }

    /* Direct connection */
    // --------------------------------------------------------------------------------------------
    private ServerBridge.ConnectionCallback mConnectionCallback = new ServerBridge.ConnectionCallback() {
        @Override
        public void onComplete() {
            mDataManager.setConnectionCallback(null);
        }

        @Override
        public void onNext(Response response) {
            // TODO:
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e.getMessage());
            mDataManager.setConnectionCallback(null);
        }
    };
}
