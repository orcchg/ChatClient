package com.orcchg.chatclient.ui.chat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.resources.PhotoItem;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.jgravatar.Gravatar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity<ChatPresenter> implements ChatMvpView {
    public static final int REQUEST_CODE = FrameworkUtility.RequestCode.CHAT_ACTIVITY;

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_USER_EMAIL = "extra_user_email";

    static String WRONG_CHANNEL_MESSAGE;
    static String SAME_CHANNEL_MESSAGE;
    static String CHAT_CHANNEL_MESSAGE;
    static String DEDICATED_MESSAGE;

    static final String BUNDLE_KEY_LOGIN = "bundle_key_login";
    static final String BUNDLE_KEY_EMAIL = "bundle_key_email";

    static final int MENU_GROUP_ID_SYSTEM = 0;
    static final int MENU_GROUP_ID_USERS = 1;
    private static final int MENU_ITEM_ID_SWITCH_CHANNEL = 0;
    private static final int MENU_ITEM_ID_LOGOUT = 1;

    @Bind(R.id.root_coordinator) ViewGroup mRootCoordinator;
    @Bind(R.id.root_container) ViewGroup mRootContainer;
    @Bind(R.id.action_container) View mActionContainer;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.photo) PhotoItem mPhotoItem;
    @Bind(R.id.rv_messages) RecyclerView mMessagesView;
    @Bind(R.id.et_message) EditText mMessagesEditView;
    @Bind(R.id.btn_send_message) ImageButton mSendMessageButton;
    @Bind(R.id.progress) View mProgressView;
    @Bind(R.id.error) View mErrorView;
    @Bind(R.id.retry_button) Button mRetryButton;

    private LinearLayoutManager mLayoutManager;
    private PopupMenu mPopupMenu;

    @Override
    protected ChatPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        long id = getIntent().getLongExtra(EXTRA_USER_ID, Status.UNKNOWN_ID);
        String name = getIntent().getStringExtra(EXTRA_USER_NAME);
        String email = getIntent().getStringExtra(EXTRA_USER_EMAIL);
        return new ChatPresenter(application.getDataManager(), id, name, email);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameworkUtility.setActive(REQUEST_CODE);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        initToolbar();

        WRONG_CHANNEL_MESSAGE = getResources().getString(R.string.error_wrong_channel);
        SAME_CHANNEL_MESSAGE = getResources().getString(R.string.error_same_channel);
        CHAT_CHANNEL_MESSAGE = getResources().getString(R.string.chat_channel_label);
        DEDICATED_MESSAGE = getResources().getString(R.string.menu_chat_dedicated_message_to);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(mLayoutManager);
        mMessagesView.setAdapter(mPresenter.getChatAdapter());

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.sendMessage();
                mMessagesEditView.setText("");  // clean up
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.removeDirectConnectionCallback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPresenter.onBackPressed();
        mPresenter.logout();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unsubscribe();
        FrameworkUtility.setInactive(REQUEST_CODE);
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onComplete() {
        if (mMessagesView.getVisibility() != View.VISIBLE) {
            mActionContainer.setVisibility(View.VISIBLE);
            mMessagesView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError() {
        mActionContainer.setVisibility(View.GONE);
        mMessagesView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        mProgressView.setVisibility(View.GONE);
    }

    @Override
    public void onUnauthorizedError() {
        onError();
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.openLoginActivity();
            }
        });
    }

    @Override
    public void onLoading() {
        mActionContainer.setVisibility(View.GONE);
        mMessagesView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public String getMessage() {
        return mMessagesEditView.getText().toString();
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    @Override
    public void scrollListTo(int position) {
        mLayoutManager.scrollToPosition(position);
    }

    @Override
    public void showSnackbar(String message, int duration) {
        Snackbar.make(mRootCoordinator, message, duration).show();
    }

    @Override
    public void setTitleWithChannel(int channel, int peersOnChannel) {
        mPhotoItem.setVisibility(View.GONE);
        mToolbar.setTitle(String.format(CHAT_CHANNEL_MESSAGE, channel, peersOnChannel));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onDedicatedMessagePrepare(Bundle args) {
        String login = args.getString(BUNDLE_KEY_LOGIN);
        String email = args.getString(BUNDLE_KEY_EMAIL);
        Gravatar gravatar = new Gravatar();
        String url = gravatar.getUrl(email);

        mPhotoItem.setVisibility(View.VISIBLE);
        mPhotoItem.setPhoto(url, true);
        mToolbar.setTitle(login);
        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.dropDedicatedMessageMode();
            }
        });
    }

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mPhotoItem.setVisibility(View.GONE);
        mToolbar.setTitle(R.string.chat_label);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.inflateMenu(R.menu.chat_menu);
        View anchorView = mToolbar.findViewById(R.id.chat_settings);
        int popupSize = 0;
        mPopupMenu = new PopupMenu(this, anchorView);
        mPopupMenu.getMenu().add(MENU_GROUP_ID_SYSTEM, MENU_ITEM_ID_SWITCH_CHANNEL, popupSize++, R.string.menu_chat_item_switch_channel);
        mPopupMenu.getMenu().add(MENU_GROUP_ID_SYSTEM, MENU_ITEM_ID_LOGOUT, popupSize++, R.string.menu_chat_item_logout);

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_ITEM_ID_SWITCH_CHANNEL:
                        mPresenter.onMenuSwitchChannel();
                        return true;
                    case MENU_ITEM_ID_LOGOUT:
                        mPresenter.onMenuLogout();
                        return true;
                    default:
                        mPresenter.onMenuItemClick(item);
                        return true;
                }
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chat_settings:
                        mPopupMenu.show();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public PopupMenu getPopupMenu() {
        return mPopupMenu;
    }

    /* Dialogs */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showSwitchChannelDialog(int channel) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_switch_channel, null);
        final EditText channelEditText = (EditText) dialogView.findViewById(R.id.et_channel);
        if (channel != Status.WRONG_CHANNEL && channel != Status.DEFAULT_CHANNEL) {
            channelEditText.setText(Integer.toString(channel));
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(R.string.dialog_switch_channel_title)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int channel = Integer.parseInt(channelEditText.getText().toString());
                        mPresenter.switchChannel(channel);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create()
                .show();
    }
}
