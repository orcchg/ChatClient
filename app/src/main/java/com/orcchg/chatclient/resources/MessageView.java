package com.orcchg.chatclient.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orcchg.chatclient.R;

public class MessageView extends FrameLayout {
    protected ViewGroup mContainer;
    protected ViewGroup mSubContainer;
    protected TextView mTitleView;
    protected TextView mDescriptionView;

    private int[] mLeftSideRootPaddings, mRightSideRootPaddings;
    private LayoutParams mLeftSideSubParams, mRightSideSubParams;

    private @ColorInt int mTitleTextColor;
    private @ColorInt int mDescriptionTextColor;
    private @ColorInt int mSelfTextColor;

    protected OnClickListener mClickListener;

    public MessageView(Context context) {
        this(context, null, 0);
    }

    public MessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initResources(context);
        init(context);
        initLayoutParams();
    }

    /* Getters & Setters */
    // --------------------------------------------------------------------------------------------
    public TextView getTitle() {
        return mTitleView;
    }

    public TextView getDescription() {
        return mDescriptionView;
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
        mContainer.setOnClickListener(listener);
    }

    public void setSide(@MessageDrawable.Side int side) {
        @ColorInt int color = Color.WHITE;
        switch (side) {
            case MessageDrawable.NO_SIDE:
                color = ViewUtils.getAttributeColor(getContext(), R.attr.refSystemMessageBlobBackgroundColor);
            case MessageDrawable.TOP_LEFT:
                mContainer.setPadding(mLeftSideRootPaddings[0], mLeftSideRootPaddings[2], mLeftSideRootPaddings[3], mLeftSideRootPaddings[5]);
                mSubContainer.setLayoutParams(mLeftSideSubParams);
                mDescriptionView.setTextColor(mDescriptionTextColor);
                break;
            case MessageDrawable.TOP_RIGHT:
                mContainer.setPadding(mRightSideRootPaddings[0], mRightSideRootPaddings[2], mRightSideRootPaddings[3], mRightSideRootPaddings[5]);
                mSubContainer.setLayoutParams(mRightSideSubParams);
                mDescriptionView.setTextColor(mSelfTextColor);
                color = ViewUtils.getAttributeColor(getContext(), R.attr.refSelfMessageBlobBackgroundColor);
                break;
        }

        MessageDrawable drawable = new MessageDrawable(color);
        drawable.setSide(side);
        mContainer.setBackgroundDrawable(drawable);
    }

    /* Private methods */
    // --------------------------------------------------------------------------------------------
    protected void initResources(Context context) {
        Resources resources = context.getResources();
        mTitleTextColor = resources.getColor(R.color.text_primary_dark);
        mDescriptionTextColor = resources.getColor(R.color.text_description_dark);
        mSelfTextColor = resources.getColor(R.color.text_white);
    }

    protected void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.message_item, this, true);
        mContainer = (ViewGroup) rootView.findViewById(R.id.message_container);
        mSubContainer = (ViewGroup) rootView.findViewById(R.id.sub_container);
        mTitleView = (TextView) rootView.findViewById(R.id.title);
        mDescriptionView = (TextView) rootView.findViewById(R.id.description);

        MessageDrawable drawable = new MessageDrawable();
        drawable.setSide(MessageDrawable.TOP_LEFT);
        mContainer.setBackgroundDrawable(drawable);

        mTitleView.setTextColor(mTitleTextColor);
        setOnClickListener(mClickListener);
    }

    protected void initLayoutParams() {
        mLeftSideRootPaddings = new int[] {
                mContainer.getPaddingLeft(),
                mContainer.getPaddingStart(),
                mContainer.getPaddingTop(),
                mContainer.getPaddingRight(),
                mContainer.getPaddingEnd(),
                mContainer.getPaddingBottom()};

        mRightSideRootPaddings = new int [] {
                mContainer.getPaddingRight(),
                mContainer.getPaddingEnd(),
                mContainer.getPaddingTop(),
                mContainer.getPaddingLeft(),
                mContainer.getPaddingStart(),
                mContainer.getPaddingBottom()};

        mLeftSideSubParams = (LayoutParams) mSubContainer.getLayoutParams();
        mRightSideSubParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRightSideSubParams.leftMargin = mLeftSideSubParams.rightMargin;
        mRightSideSubParams.setMarginStart(mLeftSideSubParams.getMarginEnd());
        mRightSideSubParams.rightMargin = mLeftSideSubParams.leftMargin;
        mRightSideSubParams.setMarginEnd(mLeftSideSubParams.getMarginStart());
    }
}
