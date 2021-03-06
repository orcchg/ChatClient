package com.orcchg.chatclient.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.ApiStatusFactory;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.Mapper;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.Peer;
import com.orcchg.chatclient.data.model.Peers;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.model.SystemMessage;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.data.viewobject.MessageMapper;
import com.orcchg.chatclient.data.viewobject.MessageToPeerMapper;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.data.viewobject.PeerMapper;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.resources.ItemClickListener;
import com.orcchg.chatclient.ui.authorization.LoginActivity;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;
import com.orcchg.chatclient.ui.chat.peerslist.ChatPeersList;
import com.orcchg.chatclient.ui.chat.util.ChatStyle;
import com.orcchg.chatclient.ui.main.MainActivity;
import com.orcchg.chatclient.ui.notification.NotificationMaster;
import com.orcchg.chatclient.util.NetworkUtility;
import com.orcchg.chatclient.util.SharedUtility;
import com.orcchg.chatclient.util.crypting.SecurityUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ChatPresenter extends BasePresenter<ChatMvpView> {
//    private static final String BUNDLE_KEY_STATE_RESTORED = "bundle_key_state_restored";
    private static final String BUNDLE_KEY_MESSAGES_LIST = "bundle_key_messages_list";
    private static final String BUNDLE_KEY_DEDICATED_PEER = "bundle_key_dedicated_peer";

    private DataManager mDataManager;
    private ArrayList<MessageVO> mMessagesList;
    private Map<Integer, LongSparseArray<PeerVO>> mAllPeers;
    private ChatAdapter mChatAdapter;
    private ChatPeersList mChatPeersList;

    private final long mUserId;
    private final String mUserName;
    private final String mUserEmail;
    private long mDestId = Status.UNKNOWN_ID;
    private int mCurrentChannel = Status.DEFAULT_CHANNEL;
    private int mLastChannel = Status.DEFAULT_CHANNEL;
    private MessageVO mLastMessage;
    private boolean mLogoutAndCloseApp = false;
    private boolean mStateRestored = false;
    private boolean mNeedReconnect = false;

    ChatPresenter(DataManager dataManager, long id, String name, String email) {
        mDataManager = dataManager;
        mUserId = id;
        mUserName = name;
        mUserEmail = email;

        mMessagesList = new ArrayList<>();
        mAllPeers = new HashMap<>();
        mChatAdapter = new ChatAdapter(mUserId, mMessagesList);
        mChatAdapter.setOnItemClickListener(createOnMessageItemClickListener(), createOnPhotoItemClickListener());
    }

    void setChatPeersList(ChatPeersList peersList) {
        mChatPeersList = peersList;
    }

    ChatAdapter getChatAdapter() {
        return mChatAdapter;
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

    /* Save & Restore state */
    // ------------------------------------------
    void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_KEY_MESSAGES_LIST, mMessagesList);
        outState.putLong(BUNDLE_KEY_DEDICATED_PEER, mDestId);
    }

    void onRestoreInstanceState(Bundle savedInstanceState) {
        mStateRestored = true;
        mMessagesList = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_MESSAGES_LIST);
        mDestId = savedInstanceState.getLong(BUNDLE_KEY_DEDICATED_PEER, Status.UNKNOWN_ID);
        mChatAdapter.restoreMessages(mMessagesList);
        mChatAdapter.notifyDataSetChanged();
    }

    /* Chat */
    // --------------------------------------------------------------------------------------------
    /**
     * Request to get all logged in peers on {@param channel}.
     *
     * @param channel to get all peers on. In case of {@link Status#WRONG_CHANNEL}
     *                retrieves all logged in peers.
     */
    void getAllPeers(int channel) {
        if (channel == Status.WRONG_CHANNEL) {
            mDataManager.getAllPeersDirect();
        } else {
            mDataManager.getAllPeersDirect(channel);
        }
    }

    void sendMessage() {
        if (!isViewAttached()) return;

        String messageString = getMvpView().getMessage();
        if (TextUtils.isEmpty(messageString)) {
            Timber.w("Empty message is forbidden");
            return;
        }

        Message message = new Message.Builder(mUserId, mUserName, mUserEmail)
            .setChannel(mCurrentChannel)
            .setDestId(mDestId)
            .setTimestamp(System.currentTimeMillis())
            .setEncrypted(false)
            .setMessage(messageString)
            .build();

        Mapper<Message, MessageVO> mapper = new MessageMapper();
        mLastMessage = mapper.map(message);
        mDataManager.sendMessageDirect(message);
    }

    void logout() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onLoading();
            }
        });
        mDataManager.logoutDirect(mUserId);
    }

    void switchChannel(int channel) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onLoading();
            }
        });
        mCurrentChannel = channel;
        mDataManager.switchChannelDirect(mUserId, mCurrentChannel);
    }

    // --------------------------------------------------------------------------------------------
    private void processStatus(Status status, final @Status.Action int action) {
        String errorMessage = "";
        boolean flag = false;
        @ApiStatusFactory.Status int code = ApiStatusFactory.getStatusByCode(status.getCode());
        switch (code) {
            case ApiStatusFactory.STATUS_SUCCESS:
                switch (action) {
                    case Status.ACTION_LOGIN:
                        Timber.i("Successfully logged in (reconnection)");
                        break;
                    case Status.ACTION_KICK:
                    case Status.ACTION_ADMIN:
                    case Status.ACTION_REGISTER:
                    case Status.ACTION_IS_LOGGED_IN:
                    case Status.ACTION_IS_REGISTERED:
                    case Status.ACTION_ALL_PEERS:
                        Timber.d("Action not processed: %s", Integer.toString(action));
                        break;
                    case Status.ACTION_MESSAGE:
                        Timber.i("Successfully sent message");
                        showLastMessage();
                        break;
                    case Status.ACTION_LOGOUT:
                        Timber.i("Successfully logged out");
                        onLogout(R.string.logout_toast_message);
                        break;
                    case Status.ACTION_SWITCH_CHANNEL:
                        Timber.i("Successfully switched channel to: %s", mCurrentChannel);
                        onChannelSwitched();
                        break;
                    case Status.ACTION_UNKNOWN:
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
                onMagic();
                break;
            case ApiStatusFactory.STATUS_UNAUTHORIZED:
                onUnauthorized();
                break;
            case ApiStatusFactory.STATUS_WRONG_CHANNEL:
                onWrongChannel();
                break;
            case ApiStatusFactory.STATUS_SAME_CHANNEL:
                onSameChannel();
                break;
            case ApiStatusFactory.STATUS_NO_SUCH_PEER:
            case ApiStatusFactory.STATUS_NOT_REQUESTED:
            case ApiStatusFactory.STATUS_ALREADY_REQUESTED:
            case ApiStatusFactory.STATUS_ALREADY_RESPONDED:
            case ApiStatusFactory.STATUS_REJECTED:
            case ApiStatusFactory.STATUS_ANOTHER_ACTION_REQUIRED:
            case ApiStatusFactory.STATUS_PUBLIC_KEY_MISSING:
                // TODO: impl private secure communication
                break;
            case ApiStatusFactory.STATUS_KICKED:
                Timber.w("You were kicked by administrator");
                onLogout(R.string.kicked_toast_message);
                break;
            case ApiStatusFactory.STATUS_FORBIDDEN_MESSAGE:
                onForbiddenMessage();
                break;
            case ApiStatusFactory.STATUS_PERMISSION_DENIED:
            case ApiStatusFactory.STATUS_REQUEST_REJECTED:
                // no-op - administrating priviledges not available on mobile client
                break;
            case ApiStatusFactory.STATUS_UNKNOWN:
            default:
                Timber.d("Unknown status");
                break;
        }
    }

    /* View state */
    // --------------------------------------------------------------------------------------------
    void onStart() {
        setDirectConnectionCallback();
        openDirectConnection();
    }

    /**
     * Connection lost during chat - retry to get onSuccess()
     */
    void onRetry() {
        if (mNeedReconnect) {
            logout();  // TODO: implement autoRelogin() next version
        } else {
            onStart();
        }
    }

    void onBackPressed() {
        mLogoutAndCloseApp = true;
    }

    boolean isBackPressed() {
        return mLogoutAndCloseApp;
    }

    /**
     * Unauthorized attempt to do an action from chat (send message, switch channel, etc.)
     */
    void openLoginActivity() {
        openActivity(LoginActivity.class);
    }

    void openMainActivity() {
        openActivity(MainActivity.class);
    }

    private void openActivity(Class clazz) {
        if (!isViewAttached()) return;
        removeDirectConnectionCallback();
        Activity activity = (Activity) getMvpView();
        Intent intent = new Intent(activity, clazz);
        activity.startActivity(intent);
        getMvpView().finishView();  // close chat
    }

    // --------------------------------------------------------------------------------------------
    private void showLastMessage() {
        mMessagesList.add(mLastMessage);
        mLastMessage = null;
        notifyViewChanged();
    }

    private void showMessage(Message message) {
        if (!isViewAttached()) return;

        boolean isDedicated = mUserId == message.getDestId();
        Mapper<Message, MessageVO> mapper = new MessageMapper(isDedicated);
        MessageVO viewObject = mapper.map(message);

        /* filter messages */
        if (mDestId == Status.UNKNOWN_ID || isDedicated) {
            mMessagesList.add(viewObject);
            notifyViewChanged();
        }

        /**
         * Messages dedicated to the current user are wrappend into notifications
         * in case Chat is paused.
         */
        if (getMvpView().isPaused() && message.getDestId() == mUserId) {
            Timber.d("Notification from peer: %s", message.getId());
            Mapper<Message, PeerVO> mapper1 = new MessageToPeerMapper();
            NotificationMaster.pushNotification(
                    (Activity) getMvpView(),
                    mapper1.map(message),
                    message.getMessage());
        }
    }

    private void showSystemMessage(String message) {
        MessageVO viewObject = new MessageVO.Builder(Status.SYSTEM_ID)
                .setMessage(message)
                .build();
        mMessagesList.add(viewObject);
        notifyViewChanged();
    }

    private void notifyViewChanged() {
        if (!isViewAttached()) return;

        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onComplete();
                getMvpView().scrollListTo(mMessagesList.size() - 1);
                mChatAdapter.notifyItemInserted(mMessagesList.size());
            }
        });
    }

    private void onChannelSwitched() {
        if (!isViewAttached()) return;

        getAllPeers(mCurrentChannel);  // request peers on new channel
        mLastChannel = mCurrentChannel;
        Activity activity = (Activity) getMvpView();
        final String message = String.format(activity.getResources().getString(R.string.switch_channel_toast_message), mCurrentChannel);
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                dropDedicatedMessageMode();
                getMvpView().onComplete();
                showSnackbar(message);
            }
        });
    }

    void onNetworkError(@NetworkUtility.ConnectionError String error) {
        if (TextUtils.isEmpty(error)) {
            return;
        }
        switch (error) {
            case NetworkUtility.ERROR_SERVER_SHUTDOWN:
                openMainActivity();
                break;
            case NetworkUtility.ERROR_CONNECTION_INTERRUPTED:
                onLostConnection();
                break;
        }
    }

    /**
     * User has done something bad that Server has got an error (fatal).
     */
    private void onMagic() {
        showSnackbar(R.string.warning_snackbar);
    }

    /**
     * User has attempted to send message or switch channel without being previously authorized.
     */
    private void onUnauthorized() {
        Timber.e("Unauthorized access");
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onUnauthorizedError();
            }
        });
    }

    private void onForbiddenMessage() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onForbiddenMessage(mLastMessage.getMessage());
            }
        });
    }

    private void onWrongChannel() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onComplete();
                showSnackbar(String.format(ChatActivity.WRONG_CHANNEL_MESSAGE, mCurrentChannel));
                mCurrentChannel = mLastChannel;  // restore previous valid channel
            }
        });
    }

    private void onSameChannel() {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().onComplete();
                showSnackbar(String.format(ChatActivity.SAME_CHANNEL_MESSAGE, mCurrentChannel));
            }
        });
    }

    private void onLogout(@StringRes int resId) {
        if (!isViewAttached()) return;
        Activity activity = (Activity) getMvpView();
        if (!activity.isFinishing()) {
            showToast(resId);
            SharedUtility.logOut(activity);
            if (!mLogoutAndCloseApp) {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            }
            getMvpView().finishView();
        }
    }

    private void onLostConnection() {
        mDataManager.lostDirectConnection();
    }

    private void showReconnectProgress(final boolean isShow) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showReconnectProgress(isShow);
            }
        });
    }

    /* Direct connection */
    // --------------------------------------------------------------------------------------------
    private ServerBridge.ConnectionCallback createConnectionCallback() {
        return new SimpleConnectionCallback<ChatPresenter>(this) {
            @Override
            public void onSuccess() {
                super.onSuccess();
                Timber.v("Connection has been established");
                getAllPeers(Status.WRONG_CHANNEL);
                getAllPeers(mCurrentChannel);
            }

            @Override
            public void onNext(Response response) {
                super.onNext(response);
                ChatPresenter presenter = getPresenterRef().get();
                if (presenter != null) {
                    if (response != null) {
                        if (response.getCodeline().getCode() == Response.TERMINATE_CODE) {
                            if (!isViewAttached()) return;
                            Timber.d("Received terminate code from Server");
                            Activity activity = (Activity) presenter.getMvpView();
                            String terminate_string = activity.getResources().getString(R.string.terminate_message);
                            presenter.showSystemMessage(terminate_string);
                            return;
                        }

                        try {
                            JSONObject json = new JSONObject(response.getBody());
                            if (json.has("code")) {
                                Timber.d("Code response: %s", response.getBody());
                                Status status = Status.fromJson(response.getBody());
                                presenter.processStatus(status, status.getAction());
                                return;
                            }

                            if (json.has("system")) {
                                if (!isViewAttached()) return;
                                Timber.d("System message: %s", response.getBody());
                                SystemMessage systemMessage = SystemMessage.fromJson(response.getBody());
                                Activity activity = (Activity) presenter.getMvpView();

                                String login = "", email;
                                PeerVO.Builder peerBuilder = new PeerVO.Builder(systemMessage.getId());
                                Map<String, String> map = SharedUtility.splitPayload(systemMessage.getPayload());
                                if (map.size() > 0) {
                                    if (SecurityUtility.isSecurityEnabled(activity) && map.containsKey("private_pubkey")) {
                                        Timber.d("Server's hello with public key has been received");
                                        String pem = map.get("private_pubkey");
                                        SecurityUtility.storeServerPublicKey(activity, pem);
                                        return;
                                    }
                                    login = map.get("login");
                                    email = map.get("email");
                                    peerBuilder.setLogin(login).setEmail(email);
                                }

                                // another peer has performed an action
                                switch (systemMessage.getAction()) {
                                    case Status.ACTION_LOGIN:
                                        peerBuilder.setChannel(Status.DEFAULT_CHANNEL);
                                        PeerVO peer = peerBuilder.build();
                                        presenter.addPeerMenuItem(systemMessage.getId(), peer);
                                        addPeer(peer);
                                        break;
                                    case Status.ACTION_SWITCH_CHANNEL:
                                        int prev = Integer.parseInt(map.get("channel_prev"));
                                        int next = Integer.parseInt(map.get("channel_next"));
                                        int move = Integer.parseInt(map.get("channel_move"));
                                        peerBuilder.setChannel(next);
                                        movePeer(peerBuilder.build(), prev);
                                        break;
                                    case Status.ACTION_LOGOUT:
                                        int channel = Integer.parseInt(map.get("channel"));
                                        peerBuilder.setChannel(channel);
                                        presenter.removePeerMenuItem(systemMessage.getId());
                                        removePeer(peerBuilder.build());
                                        break;
                                    case Status.ACTION_KICK:
                                    case Status.ACTION_ADMIN:
                                    case Status.ACTION_REGISTER:
                                    case Status.ACTION_MESSAGE:
                                    case Status.ACTION_IS_LOGGED_IN:
                                    case Status.ACTION_IS_REGISTERED:
                                    case Status.ACTION_ALL_PEERS:
                                    case Status.ACTION_UNKNOWN:
                                    default:
                                        // no-op
                                        break;
                                }
                                presenter.showSystemMessage(systemMessage.getMessage());
                                return;
                            }

                            if (json.has("message")) {
                                Timber.d("Message: %s", response.getBody());
                                Message message = Message.fromJson(response.getBody());
                                presenter.showMessage(message);
                                return;
                            }

                            if (json.has("peers")) {
                                Timber.d("Peers: %s", response.getBody());
                                Peers peers = Peers.fromJson(response.getBody());
                                presenter.fillPeersOnChannel(peers.getPeers(), peers.getChannel());
                                return;
                            }

                            Timber.w("Something doesn't like a message has been received. Skip");

                        } catch (JSONException e) {
                            Timber.e("Json error in response: %s", Log.getStackTraceString(e));
                        }
                    }
                } else {
                    Timber.v("Presenter has already been GC'ed");
                }
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
                mNeedReconnect = true;
            }
        };
    }

//    private void autoRelogin() {
//        Activity activity = (Activity) getMvpView();
//        LoginForm form = new LoginForm(mUserName, SharedUtility.getPassword(activity));
//        mDataManager.sendLoginFormDirect(form);
//    }

    /* Chat menu */
    // --------------------------------------------------------------------------------------------
    void onMenuSwitchChannel() {
        if (!isViewAttached()) return;
        getMvpView().showSwitchChannelDialog(mCurrentChannel);
    }

    void onMenuItemClick(long id) {
        if (!isViewAttached()) return;
        mDestId = id;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatPeersList.setDestId(mDestId);
            }
        });
        PeerVO peer = findPeerById(mDestId);
        if (peer != null) {
            String login = peer.getLogin();
            String email = peer.getEmail();
            final Bundle args = new Bundle();
            args.putString(ChatActivity.BUNDLE_KEY_LOGIN, login);
            args.putString(ChatActivity.BUNDLE_KEY_EMAIL, email);
            getMvpView().postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().onDedicatedMessagePrepare(args);
                }
            });
        } else if (id != Status.UNKNOWN_ID) {
            Timber.e("Inconsistency in peers list: id and position aren't correspond !");
        }
    }

    private void addPeerMenuItem(final long id, final PeerVO peer) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatPeersList.addItem(id, peer);
            }
        });
    }

    private void removePeerMenuItem(final long id) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatPeersList.removeItem(id);
            }
        });
    }

    /* List of peers */
    // --------------------------------------------------------------------------------------------
    private void fillPeersOnChannel(List<Peer> peers, int channel) {
        if (!isViewAttached()) return;

        Mapper<Peer, PeerVO> mapper = new PeerMapper();
        if (channel == Status.WRONG_CHANNEL) {  // all logged in peers
            Timber.d("All logged in peers: ");
            mAllPeers.clear();
            for (Peer peer : peers) {
                Timber.d("%s", peer.toString());
                if (peer.getId() != mUserId) {  // don't add self as peer
                    PeerVO viewObject = mapper.map(peer);
                    addPeerMenuItem(peer.getId(), viewObject);
                    if (!mAllPeers.containsKey(peer.getChannel())) {
                        mAllPeers.put(peer.getChannel(), new LongSparseArray<PeerVO>());
                    }
                    mAllPeers.get(peer.getChannel()).put(peer.getId(), viewObject);
                }
            }
        } else {
            Timber.d("All logged in peers on channel %s", channel);
            if (!mAllPeers.containsKey(channel)) {
                mAllPeers.put(channel, new LongSparseArray<PeerVO>());
            } else {
                mAllPeers.get(channel).clear();
            }
            for (Peer peer : peers) {
                Timber.d("%s", peer.toString());
                PeerVO viewObject = mapper.map(peer);
                mAllPeers.get(peer.getChannel()).put(peer.getId(), viewObject);
            }
            updateTitle();
        }

        if (mStateRestored) {
            Timber.i("State restored, destId = %s", mDestId);
            /**
             * Posted on UI thread, because predecessing calls of methods
             * {@link ChatPresenter#addPeerMenuItem(long, PeerVO)} performs
             * actions on UI thread as well, and such addition of items
             * must finish before setting destination id inside of
             * {@link ChatPresenter#onMenuItemClick(long)} method, in order
             * to get {@link ChatPeersList} to update correctly.
             */
            getMvpView().postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMenuItemClick(mDestId);  // restore selected dedicated peer if any
                }
            });
        }
    }

    private void addPeer(PeerVO peer) {
        if (!mAllPeers.containsKey(peer.getChannel())) {
            mAllPeers.put(peer.getChannel(), new LongSparseArray<PeerVO>());
        }
        mAllPeers.get(peer.getChannel()).put(peer.getId(), peer);
        updateTitle();
    }

    private void movePeer(PeerVO peer, int prevChannel) {
        removePeerFromChannel(peer, prevChannel);
        addPeer(peer);
        updateTitle();
    }

    private void removePeer(final PeerVO peer) {
        if (!isViewAttached()) return;

        removePeerFromChannel(peer, peer.getChannel());
        updateTitle();
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDestId == peer.getId()) {
                    dropDedicatedMessageMode();
                }
            }
        });
    }

    // internal method
    private void removePeerFromChannel(PeerVO peer, int channel) {
        if (mAllPeers.containsKey(channel)) {
            mAllPeers.get(channel).remove(peer.getId());
        }
    }

    private void updateTitle() {
        if (!isViewAttached()) return;

        int total = 0;
        if (mAllPeers.containsKey(mCurrentChannel)) {
            total = mAllPeers.get(mCurrentChannel).size();
        }
        final int peersOnChannel = total;

        if (mDestId != Status.UNKNOWN_ID) {  // restore communication with dedicated peer if any
            onMenuItemClick(mDestId);
        } else {
            getMvpView().postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().setTitleWithChannel(mCurrentChannel, peersOnChannel);
                }
            });
        }
    }

    @Nullable
    private PeerVO findPeerById(long id) {
        for (Map.Entry<Integer, LongSparseArray<PeerVO>> entry: mAllPeers.entrySet()) {
            PeerVO peer = entry.getValue().get(id, null);
            if (peer != null) {
                return peer;
            }
            // continue on next channel
        }
        return null;
    }

    /* Dedicated message mode */
    // ------------------------------------------
    private void decorate(final @ChatStyle.Style int style) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().decorate(style);
            }
        });
    }

    void dropDedicatedMessageMode() {
        mDestId = Status.UNKNOWN_ID;
        mChatPeersList.setDestId(mDestId);
        decorate(ChatStyle.STYLE_NORMAL);
        updateTitle();
    }

    /* Adapter item click listeners */
    // --------------------------------------------------------------------------------------------
    private ItemClickListener createOnMessageItemClickListener() {
        ItemClickListener listener = new ItemClickListener();
        listener.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        listener.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO
                return false;
            }
        });
        return listener;
    }

    private ItemClickListener createOnPhotoItemClickListener() {
        ItemClickListener listener = new ItemClickListener();
        listener.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        listener.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO
                return false;
            }
        });
        return listener;
    }
}
