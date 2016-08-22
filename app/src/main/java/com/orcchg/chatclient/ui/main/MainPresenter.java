package com.orcchg.chatclient.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.model.Check;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.model.SystemMessage;
import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;
import com.orcchg.chatclient.ui.authorization.LoginActivity;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.base.SimpleConnectionCallback;
import com.orcchg.chatclient.util.SharedUtility;
import com.orcchg.chatclient.util.crypting.SecurityUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import timber.log.Timber;

public class MainPresenter extends BasePresenter<MainMvpView> {

    private DataManager mDataManager;

    private long mUserId;
    private String mUserName;
    private String mUserEmail;

    MainPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    void init() {
        Activity activity = (Activity) getMvpView();
        Resources resources = activity.getResources();
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        mUserId = sp.getLong(resources.getString(R.string.shared_prefs_user_id_key), Status.UNKNOWN_ID);
        mUserName = sp.getString(resources.getString(R.string.shared_prefs_user_login_key), null);
        mUserEmail = sp.getString(resources.getString(R.string.shared_prefs_user_email_key), null);
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

    // --------------------------------------------------------------------------------------------
    private void checkForLogin() {
        onLoading();
        mDataManager.isLoggedInDirect(mUserEmail);
    }

    private void checkRegistered() {
        onLoading();
        mDataManager.isRegisteredDirect(mUserEmail);
    }

    private void processCheck(Check check) {
        @Status.Action int action = check.getAction();
        Timber.v("Processing check with user [id=%s, name=%s, email=%s]", mUserId, mUserName, mUserEmail);
        switch (action) {
            case Status.ACTION_LOGIN:
            case Status.ACTION_REGISTER:
            case Status.ACTION_MESSAGE:
            case Status.ACTION_LOGOUT:
            case Status.ACTION_SWITCH_CHANNEL:
                Timber.d("Action not processed: %s", Integer.toString(action));
                break;
            case Status.ACTION_IS_LOGGED_IN:
                openLoginActivity();
                break;
            case Status.ACTION_IS_REGISTERED:
                if (check.getCheck() == 0) {
                    Timber.e("User is not registered, but previously had logged in. This is magic error...");
                }
                break;
            case Status.ACTION_UNKNOWN:
            default:
                Timber.d("Unknown action on Success");
                break;
        }
    }

    /* Direct connection */
    // --------------------------------------------------------------------------------------------
    private ServerBridge.ConnectionCallback createConnectionCallback() {
        return new SimpleConnectionCallback<MainPresenter>(this) {
            @Override
            public void onSuccess() {
                super.onSuccess();
                checkForLogin();
                checkRegistered();
            }

            @Override
            public void onNext(Response response) {
                super.onNext(response);
                MainPresenter presenter = getPresenterRef().get();
                if (presenter != null) {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        if (json.has("check")) {
                            Timber.d("Check response: %s", response.getBody());
                            Check check = Check.fromJson(response.getBody());
                            presenter.onComplete();
                            presenter.processCheck(check);
                            return;
                        }

                        if (json.has("system")) {
                            Timber.d("System message: %s", response.getBody());
                            SystemMessage systemMessage = SystemMessage.fromJson(response.getBody());
                            Activity activity = (Activity) presenter.getMvpView();

                            Map<String, String> map = SharedUtility.splitPayload(systemMessage.getPayload());
                            if (map.size() > 0) {
                                if (SecurityUtility.isSecurityEnabled(activity) && map.containsKey("private_pubkey")) {
                                    Timber.d("Server's hello with public key has been received");
                                    String pem = map.get("private_pubkey");
                                    SecurityUtility.storeServerPublicKey(activity, pem);
                                    return;
                                }
                            }
                        }

                        Timber.w("Something doesn't like a check or system has been received. Skip");

                    } catch (JSONException e) {
                        Timber.e("Server has responded with malformed json body: %s", response.getBody());
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

    /* Open activities */
    // --------------------------------------------------------------------------------------------
    void openLoginActivity() {
        Activity activity = (Activity) getMvpView();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra(MainActivity.SHARED_PREFS_KEY_USER_EMAIL, mUserEmail);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(0, 0);
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

    void onRetry() {
        setDirectConnectionCallback();
        openDirectConnection();
    }
}
