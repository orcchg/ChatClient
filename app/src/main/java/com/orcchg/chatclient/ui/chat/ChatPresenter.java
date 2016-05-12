package com.orcchg.chatclient.ui.chat;

import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.viewobject.MessageMapper;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.mock.MockProvider;
import com.orcchg.chatclient.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import timber.log.Timber;

public class ChatPresenter extends BasePresenter<ChatMvpView> {

    DataManager mDataManager;  // TODO: inject
    List<MessageVO> mMessagesList;
    ChatAdapter mChatAdapter;

    ChatPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mMessagesList = new ArrayList<>();
        mChatAdapter = new ChatAdapter(mMessagesList);
    }

    ChatAdapter getChatAdapter() {
        return mChatAdapter;
    }

    void loadMessages() {
        final Mapper<Message, MessageVO> mapper = new MessageMapper();

        Observable.from(MockProvider.createMessages()).flatMap(new Func1<Message, Observable<MessageVO>>() {
            @Override
            public Observable<MessageVO> call(Message message) {
                MessageVO viewObject = mapper.map(message);
                return Observable.just(viewObject);
            }
        }).subscribe(createObserver());
    }

    private Observer<MessageVO> createObserver() {
        return new Observer<MessageVO>() {
            @Override
            public void onCompleted() {
                Timber.d("Complete");
                mChatAdapter.notifyDataSetChanged();
                checkViewAttached();
//                getMvpView().showCustomers(false);
            }

            @Override
            public void onError(Throwable e) {
//                getMvpView().showError();
            }

            @Override
            public void onNext(MessageVO viewObject) {
                Timber.d("Next: " + viewObject.getLogin());
                mMessagesList.add(viewObject);
            }
        };
    }
}
