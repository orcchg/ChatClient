package com.orcchg.chatclient;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.remote.RestAdapter;
import com.orcchg.chatclient.data.remote.ServerBridge;

import timber.log.Timber;

public class ChatClientApplication extends Application {

    private RestAdapter mRestAdapter;
    private ServerBridge mServer;
    private DataManager mDataManager;

    public static String PACKAGE_NAME;

    @Override
    public void onCreate() {
        super.onCreate();
        PACKAGE_NAME = getPackageName();
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return PACKAGE_NAME + ":" + super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });

        mRestAdapter = RestAdapter.Creator.create();
        mServer = new ServerBridge();
        mDataManager = new DataManager(mRestAdapter, mServer);

        initDrawerImageLoader();
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    private void initDrawerImageLoader() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                return super.placeholder(ctx, tag);
            }
        });
    }
}
