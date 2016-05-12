package com.orcchg.chatclient.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.orcchg.chatclient.R;

public class PhotoItem extends IconItem {

    public PhotoItem(Context context) {
        this(context, null, 0);
    }

    public PhotoItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Drawable foreground = ViewUtils.getAttributeDrawable(context, R.attr.refForegroundSelectorColor);
        if (foreground != null) {
            mContainer.setForeground(foreground);
        }
    }

    public void setPhoto(@Nullable String photoUrl, boolean isCircle) {
        if (TextUtils.isEmpty(photoUrl)) {
            showStub();
            return;
        }
        final Resources resources = getContext().getResources();
        int iconSize = resources.getDimensionPixelSize(R.dimen.icon_size);
        showProgress();
        if (isCircle) {
            Glide.with(getContext()).load(photoUrl).override(iconSize, iconSize).listener(mProgressListener).bitmapTransform(ImageTransform.create(getContext(), ImageTransform.CIRCLE_CROP)).into(mImageView);
        } else {
            Glide.with(getContext()).load(photoUrl).override(iconSize, iconSize).listener(mProgressListener).into(mImageView);
        }
    }
}
