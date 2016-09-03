package com.orcchg.chatclient.resources;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcchg.chatclient.R;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public abstract class IconItem extends FrameLayout {

    protected FrameLayout mContainer;
    protected ImageView mImageView;
    protected ProgressBar mProgressView;
    protected View mStubView;

    protected OnClickListener mClickListener;
    protected OnLongClickListener mLongClickListener;
    protected RequestListener<String, GlideDrawable> mProgressListener;

    public IconItem(Context context) {
        this(context, null, 0);
    }

    public IconItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.icon_item, this, true);
        mContainer = (FrameLayout) rootView.findViewById(R.id.ii_icon_container);
        mStubView = findViewById(R.id.ii_stub);
        mImageView = (ImageView) rootView.findViewById(R.id.ii_icon);
        mProgressView = (ProgressBar) rootView.findViewById(R.id.ii_progress);
        mProgressListener = new ProgressListener(this);
        setOnClickListener(mClickListener);
        setOnLongClickListener(mLongClickListener);
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    public void setOnClickListener(final OnClickListener listener) {
        mClickListener = listener;
        if (mContainer != null) {
            mContainer.setOnClickListener(mClickListener);
        }
    }

    public void setOnLongClickListener(final OnLongClickListener listener) {
        mLongClickListener = listener;
        if (mContainer != null) {
            mContainer.setOnLongClickListener(mLongClickListener);
        }
    }

    public void setItemClickListener(final ItemClickListener listener) {
        if (listener != null) {
            setOnClickListener(listener.getClickListener());
            setOnLongClickListener(listener.getLongClickListener());
        }
    }

    /* View appearance */
    // --------------------------------------------------------------------------------------------
    protected void showProgress() {
        mProgressView.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
        mStubView.setVisibility(View.INVISIBLE);
    }

    protected void showContent() {
        mProgressView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
        mStubView.setVisibility(View.INVISIBLE);
    }

    protected void showStub() {
        mProgressView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
        mStubView.setVisibility(View.VISIBLE);
    }

    /* Progress listener */
    // --------------------------------------------------------------------------------------------
    protected static class ProgressListener implements RequestListener<String, GlideDrawable> {
        protected WeakReference<IconItem> mIconItemRef;

        protected ProgressListener(@NonNull IconItem iconItem) {
            mIconItemRef = new WeakReference<>(iconItem);
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            Timber.e("Exception during loading image: " + e);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Timber.v("Image has been loaded");
            IconItem iconItem = mIconItemRef.get();
            if (iconItem != null) {
                iconItem.showContent();
            }
            return false;
        }
    }
}
