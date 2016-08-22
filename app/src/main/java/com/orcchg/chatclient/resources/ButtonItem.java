package com.orcchg.chatclient.resources;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.orcchg.chatclient.R;

public class ButtonItem extends FrameLayout {
    protected final String TAG = this.getClass().getSimpleName();

    protected View mRootView;
    protected FrameLayout mContainer;
    protected ImageView mImageView;

    protected OnClickListener mClickListener;

    protected @DrawableRes int mIconResId;
    protected int mIconSize;
    private boolean mIsToggled;

    private @ColorInt int DEFAULT_COLOR;
    private @ColorInt int TOGGLED_COLOR;
    private Drawable TOGGLED_BG_DRAWABLE;

    public ButtonItem(Context context) {
        this(context, null, 0);
    }

    public ButtonItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.side_menu_button_size);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonItem, defStyle, 0);
        mIconResId = a.getResourceId(R.styleable.ButtonItem_btn_icon, 0);
        mIconSize = a.getDimensionPixelSize(R.styleable.ButtonItem_btn_size, iconSize);
        a.recycle();

        init(context);
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    public void setOnClickListener(final OnClickListener listener) {
        mClickListener = listener;
        if (mContainer != null) {
            mContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    setToggled(!mIsToggled);
                    if (listener != null) {
                        listener.onClick(view);
                    }
                }
            });
        }
    }

    /* View content */
    // --------------------------------------------------------------------------------------------
    public void setToggled(boolean isToggled) {
        mIsToggled = isToggled;
        decorateButton();
    }

    /* By drawable resource id */
    // ------------------------------------------
    public void setImage(@DrawableRes int resId) {
        Glide.with(getContext()).load(resId).override(mIconSize, mIconSize).into(mImageView);
    }

    /* By reference attribute id */
    // ------------------------------------------
    public void setAttributeDrawable(@AttrRes int resId) {
        Drawable drawable = ViewUtils.getAttributeDrawable(getContext(), resId);
        mImageView.setBackground(drawable);
    }

    /* Private methods */
    // --------------------------------------------------------------------------------------------
    protected void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mRootView = inflater.inflate(R.layout.side_menu_button_item, this, true);
        mContainer = (FrameLayout) mRootView.findViewById(R.id.icon_container);
        mImageView = (ImageView) mRootView.findViewById(R.id.icon);
        setOnClickListener(mClickListener);
        if (mIconResId > 0) {
            setImage(mIconResId);
        }

        DEFAULT_COLOR = context.getResources().getColor(R.color.side_menu_button_color_default);
        TOGGLED_COLOR = ViewUtils.getAttributeColor(context, R.attr.colorPrimary);
        TOGGLED_BG_DRAWABLE = ViewUtils.getAttributeDrawable(context, R.attr.refSideMenuButtonToggleBgColor);

        mImageView.setColorFilter(DEFAULT_COLOR);
    }

    private void decorateButton() {
        if (mIsToggled) {
            mContainer.setBackgroundDrawable(TOGGLED_BG_DRAWABLE);
            mImageView.setColorFilter(TOGGLED_COLOR);
        } else {
            mContainer.setBackgroundColor(Color.TRANSPARENT);
            mImageView.setColorFilter(DEFAULT_COLOR);
        }
    }
}
