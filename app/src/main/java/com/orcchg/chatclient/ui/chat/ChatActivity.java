package com.orcchg.chatclient.ui.chat;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.resources.ButtonItem;
import com.orcchg.chatclient.resources.PhotoItem;
import com.orcchg.chatclient.ui.base.BaseActivity;
import com.orcchg.chatclient.ui.chat.peerslist.DrawerChatPeersList;
import com.orcchg.chatclient.ui.chat.peerslist.PopupMenuChatPeersList;
import com.orcchg.chatclient.ui.chat.peerslist.SideChatPeersList;
import com.orcchg.chatclient.ui.chat.util.ChatStyle;
import com.orcchg.chatclient.ui.main.MainActivity;
import com.orcchg.chatclient.ui.notification.NotificationMaster;
import com.orcchg.chatclient.util.FrameworkUtility;
import com.orcchg.chatclient.util.NetworkUtility;
import com.orcchg.chatclient.util.WindowUtility;
import com.orcchg.jgravatar.Gravatar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.orcchg.chatclient.R.id.error;

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
    private static final String BUNDLE_KEY_MESSAGES_LIST_STATE = "bundle_key_messages_list_state";

    public static final int MENU_GROUP_ID_SYSTEM = 0;
    public static final int MENU_GROUP_ID_USERS = 1;
    private static final int MENU_ITEM_ID_SWITCH_CHANNEL = 0;
    private static final int MENU_ITEM_ID_LOGOUT = 1;

    private static final int MENU_TYPE_DRAWER = 0;
    private static final int MENU_TYPE_POPUP = 1;
    private static final int MENU_TYPE_LIST = 2;
    @IntDef({MENU_TYPE_DRAWER, MENU_TYPE_POPUP, MENU_TYPE_LIST})
    @Retention(RetentionPolicy.SOURCE)
    private @interface MenuType {}

    private @ChatStyle.Style int mDecorateMode = ChatStyle.STYLE_NORMAL;

    @BindView(R.id.root_coordinator) ViewGroup mRootCoordinator;
    @BindView(R.id.action_container) View mActionContainer;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.photo) PhotoItem mPhotoItem;
    @BindView(R.id.rv_messages) RecyclerView mMessagesView;
    @BindView(R.id.et_message) EditText mMessagesEditView;
    @BindView(R.id.btn_send_message) ImageButton mSendMessageButton;
    @BindView(R.id.btn_clear_message) ImageButton mClearMessageButton;
    @BindView(R.id.progress) View mProgressView;
    @BindView(error) View mErrorView;
    @BindView(R.id.retry_button) Button mRetryButton;
    @BindView(R.id.optional_text) TextView mOptionalText;

    private LinearLayoutManager mLayoutManager;
    private ProgressDialog mProgressDialog;

    private boolean mIsPaused;
    private boolean mIsBackPressed;
    private Parcelable mMessagesListState;

    /* Peers lists */
    // ------------------------------------------
    @Nullable private PopupMenu mPopupMenu;

    @Nullable private Drawer mDrawer;
    @Nullable private DrawerArrowDrawable mDrawerToggle;

    @Nullable private SideChatPeersList.PeersAdapter mAdapter;
    @Nullable @BindView(R.id.rv_peers_list) RecyclerView mList;
    @Nullable @BindView(R.id.side_menu_container) ViewGroup mSideMenuContainer;
    @Nullable @BindView(R.id.btni_switch_channel) ButtonItem mSwitchChannelBtnI;
    @Nullable @BindView(R.id.btni_logout) ButtonItem mLogoutBtnI;

    private @MenuType int mMenuType;

    // --------------------------------------------------------------------------------------------
    @Override
    protected ChatPresenter createPresenter() {
        ChatClientApplication application = (ChatClientApplication) getApplication();
        long id = getIntent().getLongExtra(EXTRA_USER_ID, Status.UNKNOWN_ID);
        String name = getIntent().getStringExtra(EXTRA_USER_NAME);
        String email = getIntent().getStringExtra(EXTRA_USER_EMAIL);
        return new ChatPresenter(application.getDataManager(), id, name, email);
    }

    @Override
    @FrameworkUtility.RequestCode.Code
    protected int getActivityRequestCode() {
        return REQUEST_CODE;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsBackPressed = false;
        @IntentStatus int status = processNotificationIntent(getIntent());
        if (status == PROCESS_NOTIF_INTENT_STATUS_ERROR) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        FrameworkUtility.setActive(REQUEST_CODE);
        FrameworkUtility.diagnostic();

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        initResources();
        initToolbar();
        initProgressDialog();
        if (WindowUtility.isTablet(this)) {
//            initSideMenu();
            initToolbarMenu();
            initPeersList();
        } else {
            initDrawer();
        }

        WRONG_CHANNEL_MESSAGE = getResources().getString(R.string.error_wrong_channel);
        SAME_CHANNEL_MESSAGE = getResources().getString(R.string.error_same_channel);
        CHAT_CHANNEL_MESSAGE = getResources().getString(R.string.chat_channel_label);
        DEDICATED_MESSAGE = getResources().getString(R.string.menu_chat_dedicated_message_to);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(mLayoutManager);
        mMessagesView.setAdapter(mPresenter.getChatAdapter());

        if (savedInstanceState != null) {
            mMessagesListState = savedInstanceState.getParcelable(BUNDLE_KEY_MESSAGES_LIST_STATE);
            if (mMessagesListState != null) {
                mLayoutManager.onRestoreInstanceState(mMessagesListState);
            }
            mPresenter.onRestoreInstanceState(savedInstanceState);
        }

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onRetry();
            }
        });
        mClearMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessagesEditView.setText("");  // clean up
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
        mIsPaused = false;
        mPresenter.onStart();
        mMessagesEditView.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        FrameworkUtility.setFinishing(REQUEST_CODE, isFinishing());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMessagesListState = mLayoutManager.onSaveInstanceState();
        mPresenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
        mPresenter.onBackPressed();
        mPresenter.logout();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter.isBackPressed()) {
            mPresenter.removeDirectConnectionCallback();
        }
        FrameworkUtility.setInactive(REQUEST_CODE);
        FrameworkUtility.diagnostic();
        if (isFinishing() && FrameworkUtility.getActiveCount() == 0) {
            mPresenter.closeDirectConnection();
        }
        super.onDestroy();
    }

    /* Presentation layer */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSuccess() {
        Timber.d("onSuccess");
        if (mMessagesView.getVisibility() != View.VISIBLE) {
            mActionContainer.setVisibility(View.VISIBLE);
            mMessagesView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTerminate() {
        Timber.d("onTerminate");
        if (!isFinishing()) {
            @NetworkUtility.ConnectionError String error = ServerBridge.getLastNetworkError();
            if (!TextUtils.isEmpty(error)) {
                mOptionalText.setVisibility(View.VISIBLE);
                onError();
            }
        }
    }

    @Override
    public void onLoading() {
        Timber.d("onLoading");
        mActionContainer.setVisibility(View.GONE);
        mMessagesView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete() {
        Timber.d("onComplete");
        onSuccess();
    }

    @Override
    public void onError() {
        if (!mIsBackPressed) {
            Timber.e("onError");
            mActionContainer.setVisibility(View.GONE);
            mMessagesView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
        }
        WindowUtility.showSoftKeyboard(this, false);
    }

    @Override
    public void onNetworkError(@NetworkUtility.ConnectionError String error) {
        super.onNetworkError(error);
        mPresenter.onNetworkError(error);
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
    public void onForbiddenMessage(String message) {
        mMessagesEditView.setText(message);
        mMessagesEditView.setSelection(mMessagesEditView.getText().length());
        mMessagesEditView.setError(getResources().getString(R.string.error_forbidden_message));
    }

    @Override
    public String getMessage() {
        return mMessagesEditView.getText().toString();
    }

    @Override
    public boolean isPaused() {
        return mIsPaused;
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    @Override
    public void scrollListTo(int position) {
        mLayoutManager.scrollToPosition(position);
    }

    @Override
    public void setTitleWithChannel(int channel, int peersOnChannel) {
        mPhotoItem.setVisibility(View.GONE);
        mToolbar.setTitle(String.format(CHAT_CHANNEL_MESSAGE, channel, peersOnChannel));
        switch (mMenuType) {
            case MENU_TYPE_DRAWER:
                mDrawer.getActionBarDrawerToggle().setDrawerArrowDrawable(mDrawerToggle);
                break;
            case MENU_TYPE_POPUP:
            case MENU_TYPE_LIST:
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                break;
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mMenuType) {
                    case MENU_TYPE_DRAWER:
                        mDrawer.openDrawer();
                        break;
                    case MENU_TYPE_POPUP:
                    case MENU_TYPE_LIST:
                        mPresenter.logout();
                        break;
                }
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

        decorate(ChatStyle.STYLE_DEDICATED);
    }

    /* Toolbar */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        mPhotoItem.setVisibility(View.GONE);
        mToolbar.setTitle(R.string.chat_label);
    }

    private void initPopupMenu() {
        mMenuType = MENU_TYPE_POPUP;

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
                        mPresenter.logout();
                        return true;
                    default:
                        item.setChecked(true);
                        mPresenter.onMenuItemClick(item.getItemId());
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

        mPresenter.setChatPeersList(new PopupMenuChatPeersList(mPopupMenu));
    }

    /* Resources & decoration */
    // --------------------------------------------------------------------------------------------
    private @ColorInt int mNormalStatusBarColor;
    private @ColorInt int mNormalToolbarColor;
    private @ColorInt int mDedicatedStatusBarColor;
    private @ColorInt int mDedicatedToolbarColor;

    private ValueAnimator mNormalToDedicatedColorAnimator;
    private ValueAnimator mDedicatedToNormalColorAnimator;

    private void initResources() {
        mNormalStatusBarColor = getResources().getColor(R.color.colorPrimaryDark);
        mNormalToolbarColor = getResources().getColor(R.color.colorPrimary);
        mDedicatedStatusBarColor = getResources().getColor(R.color.chat_statusbar_dedicated_color);
        mDedicatedToolbarColor = getResources().getColor(R.color.chat_toolbar_dedicated_color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNormalToDedicatedColorAnimator = ValueAnimator.ofArgb(mNormalToolbarColor, mDedicatedToolbarColor);
            mDedicatedToNormalColorAnimator = ValueAnimator.ofArgb(mDedicatedToolbarColor, mNormalToolbarColor);
        } else {
            mNormalToDedicatedColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mNormalToolbarColor, mDedicatedToolbarColor);
            mDedicatedToNormalColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mDedicatedToolbarColor, mNormalToolbarColor);
        }
        mNormalToDedicatedColorAnimator.setDuration(250);
        mDedicatedToNormalColorAnimator.setDuration(250);

        mNormalToDedicatedColorAnimator.addUpdateListener(createViewColorUpdateListener(mToolbar));
        mDedicatedToNormalColorAnimator.addUpdateListener(createViewColorUpdateListener(mToolbar));
    }

    private ValueAnimator.AnimatorUpdateListener createViewColorUpdateListener(final View view) {
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setBackgroundColor((int) animation.getAnimatedValue());
            }
        };
    }

    @Override
    public void decorate(@ChatStyle.Style int style) {
        mDecorateMode = style;
        switch (style) {
            case ChatStyle.STYLE_NORMAL:
                WindowUtility.tintStatusBarAnimated(this, mDedicatedStatusBarColor, mNormalStatusBarColor);
                mDedicatedToNormalColorAnimator.start();
                break;
            case ChatStyle.STYLE_DEDICATED:
                WindowUtility.tintStatusBarAnimated(this, mNormalStatusBarColor, mDedicatedStatusBarColor);
                mNormalToDedicatedColorAnimator.start();
                break;
        }
    }

    /* Dialogs */
    // --------------------------------------------------------------------------------------------
    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_progress_reconnect));
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void showSwitchChannelDialog(int channel) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_switch_channel, null);
        final EditText channelEditText = (EditText) dialogView.findViewById(R.id.et_channel);
        if (channel != Status.WRONG_CHANNEL && channel != Status.DEFAULT_CHANNEL) {
            channelEditText.setText(Integer.toString(channel));
            channelEditText.setSelection(channelEditText.getText().length());
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

    @Override
    public void showReconnectProgress(boolean isShow) {
        if (isShow) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /* Drawer */
    // --------------------------------------------------------------------------------------------
    public static final int DRAWER_ITEM_ID_SWITCH_CHANNEL = 0;
    public static final int DRAWER_ITEM_ID_LOGOUT = 1;
    public static final int DRAWER_ITEM_ID_CUSTOM = 2;

    private void initDrawer() {
        mMenuType = MENU_TYPE_DRAWER;

        long id = getIntent().getLongExtra(EXTRA_USER_ID, Status.UNKNOWN_ID);
        String name = getIntent().getStringExtra(EXTRA_USER_NAME);
        String email = getIntent().getStringExtra(EXTRA_USER_EMAIL);
        String url = new Gravatar().getUrl(email);

        /* header */
        // --------------------------------------
        IProfile profile = new ProfileDrawerItem()
                .withName(name)
                .withEmail(email)
                .withIcon(url)
                .withIdentifier(DRAWER_ITEM_ID_CUSTOM + id);

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.green_rectangle)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .build();

        /* items */
        // --------------------------------------
        PrimaryDrawerItem switchChannelItem = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_exit_to_app_black_24dp)
                .withIdentifier(DRAWER_ITEM_ID_SWITCH_CHANNEL)
                .withName(R.string.menu_chat_item_switch_channel);
        PrimaryDrawerItem logoutItem = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_close_black_24dp)
                .withIdentifier(DRAWER_ITEM_ID_LOGOUT)
                .withName(R.string.menu_chat_item_logout);

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)  // no default selection
                .withAccountHeader(accountHeader)
                .addDrawerItems(switchChannelItem, logoutItem, new DividerDrawerItem())
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        long id = drawerItem.getIdentifier();
                        switch ((int) id) {
                            case DRAWER_ITEM_ID_SWITCH_CHANNEL:
                                mPresenter.onMenuSwitchChannel();
                                break;
                            case DRAWER_ITEM_ID_LOGOUT:
                                mPresenter.logout();
                                break;
                            default:
                                mPresenter.onMenuItemClick(id - DRAWER_ITEM_ID_CUSTOM);
                                break;
                        }
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        mMessagesEditView.requestFocus();
                        WindowUtility.showSoftKeyboard(ChatActivity.this, false);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        mMessagesEditView.requestFocus();
                        WindowUtility.showSoftKeyboard(ChatActivity.this, true);
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                    }
                })
                .build();

        ActionBarDrawerToggle toggle = mDrawer.getActionBarDrawerToggle();
        toggle.setDrawerIndicatorEnabled(true);  // hamburger
        mDrawerToggle = toggle.getDrawerArrowDrawable();

        mPresenter.setChatPeersList(new DrawerChatPeersList(mDrawer));
    }

    /* Toolbar & Side menu */
    // --------------------------------------------------------------------------------------------
    private void initSideMenu() {
        mSideMenuContainer.setVisibility(View.VISIBLE);

        mSwitchChannelBtnI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onMenuSwitchChannel();
            }
        });

        mLogoutBtnI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.logout();
            }
        });
    }

    private void initToolbarMenu() {
        mToolbar.inflateMenu(R.menu.chat_actions_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_switch_channel:
                        mPresenter.onMenuSwitchChannel();
                        return true;
                    case R.id.menu_logout:
                        onBackPressed();
                        return true;
                }
                return false;
            }
        });
    }

    /* Peers list */
    // --------------------------------------------------------------------------------------------
    private void initPeersList() {
        mMenuType = MENU_TYPE_LIST;

        mAdapter = new SideChatPeersList.PeersAdapter(new SideChatPeersList.PeersAdapter.OnPeerSelect() {
            @Override
            public void onSelect(long id, boolean selected) {
                if (selected) {
                    mPresenter.onMenuItemClick(id);
                } else {
                    mPresenter.dropDedicatedMessageMode();
                }
            }
        });
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mPresenter.setChatPeersList(new SideChatPeersList(mAdapter));
    }

    /* Notifications */
    // --------------------------------------------------------------------------------------------
    private static final int PROCESS_NOTIF_INTENT_STATUS_OK = 0;
    private static final int PROCESS_NOTIF_INTENT_STATUS_ERROR = 1;
    @IntDef({PROCESS_NOTIF_INTENT_STATUS_OK, PROCESS_NOTIF_INTENT_STATUS_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    private @interface IntentStatus {}

    @IntentStatus
    private int processNotificationIntent(Intent intent) {
        if (intent != null) {
            boolean extra = intent.getBooleanExtra(NotificationMaster.EXTRA_OPEN_BY_NOTIFICATION, false);
            if (extra && !FrameworkUtility.isActive(FrameworkUtility.RequestCode.CHAT_ACTIVITY)) {
                Timber.w("Attempt to open ChatActivity via notification, but it had been destoyed");
                return PROCESS_NOTIF_INTENT_STATUS_ERROR;
            } else if (extra) {
                Timber.d("Opened by notification");
            } else {
                Timber.d("Opened by direct intent");
            }
        }
        return PROCESS_NOTIF_INTENT_STATUS_OK;
    }
}
