package com.orcchg.chatclient.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    private int TRIANGLE_WIDTH = 24, TRIANGLE_HEIGHT = 24;

    private @ColorInt int mTitleTextColor;
    private @ColorInt int mDescriptionTextColor;
    private @ColorInt int mSelfTextColor;
    private @ColorInt static final int BAD_COLOR = Color.WHITE;

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
        @ColorInt int color = BAD_COLOR;
        switch (side) {
            case MessageDrawable.NO_SIDE:
                mDescriptionView.setTextColor(mDescriptionTextColor);
                color = ViewUtils.getAttributeColor(getContext(), R.attr.refSystemMessageBlobBackgroundColor);
                break;
            case MessageDrawable.SPECIAL_TOP_LEFT:
                color = ViewUtils.getAttributeColor(getContext(), R.attr.refPeerMessageDedicatedBlockBackgroundColor);
            case MessageDrawable.TOP_LEFT:
                mContainer.setPadding(mLeftSideRootPaddings[0], mLeftSideRootPaddings[2], mLeftSideRootPaddings[3], mLeftSideRootPaddings[5]);
                mSubContainer.setLayoutParams(mLeftSideSubParams);
                mDescriptionView.setTextColor(mDescriptionTextColor);
                if (color == BAD_COLOR) {
                    color = ViewUtils.getAttributeColor(getContext(), R.attr.refPeerMessageBlockBackgroundColor);
                }
                break;
            case MessageDrawable.SPECIAL_TOP_RIGHT:
                color = ViewUtils.getAttributeColor(getContext(), R.attr.refSelfMessageDedicatedBlobBackgroundColor);
            case MessageDrawable.TOP_RIGHT:
                mContainer.setPadding(mRightSideRootPaddings[0], mRightSideRootPaddings[2], mRightSideRootPaddings[3], mRightSideRootPaddings[5]);
                mSubContainer.setLayoutParams(mRightSideSubParams);
                mDescriptionView.setTextColor(mSelfTextColor);
                if (color == BAD_COLOR) {
                    color = ViewUtils.getAttributeColor(getContext(), R.attr.refSelfMessageBlobBackgroundColor);
                }
                break;
        }

        MessageDrawable drawable = new MessageDrawable(TRIANGLE_WIDTH, TRIANGLE_HEIGHT, color);
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
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_XXHIGH:
                TRIANGLE_WIDTH = 24;
                TRIANGLE_HEIGHT = 24;
                break;
            case DisplayMetrics.DENSITY_LOW:
            case DisplayMetrics.DENSITY_MEDIUM:
                TRIANGLE_WIDTH = 8;
                TRIANGLE_HEIGHT = 8;
                break;
            default:
                TRIANGLE_WIDTH = 16;
                TRIANGLE_HEIGHT = 16;
                break;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.message_item, this, true);
        mContainer = (ViewGroup) rootView.findViewById(R.id.message_container);
        mSubContainer = (ViewGroup) rootView.findViewById(R.id.sub_container);
        mTitleView = (TextView) rootView.findViewById(R.id.title);
        mDescriptionView = (TextView) rootView.findViewById(R.id.description);

        MessageDrawable drawable = new MessageDrawable(TRIANGLE_WIDTH, TRIANGLE_HEIGHT);
        drawable.setSide(MessageDrawable.TOP_LEFT);
        mContainer.setBackgroundDrawable(drawable);

        mTitleView.setTextColor(mTitleTextColor);
        setOnClickListener(mClickListener);
    }

    protected void initLayoutParams() {
        mLeftSideRootPaddings = new int[] {
                mContainer.getPaddingLeft(),
                mContainer.getPaddingLeft(),  //  mContainer.getPaddingStart()
                mContainer.getPaddingTop(),
                mContainer.getPaddingRight(),
                mContainer.getPaddingRight(),  //  mContainer.getPaddingEnd()
                mContainer.getPaddingBottom()};

        mRightSideRootPaddings = new int [] {
                mContainer.getPaddingRight(),
                mContainer.getPaddingRight(),  //  mContainer.getPaddingEnd()
                mContainer.getPaddingTop(),
                mContainer.getPaddingLeft(),
                mContainer.getPaddingLeft(),  //  mContainer.getPaddingStart()
                mContainer.getPaddingBottom()};

        mLeftSideSubParams = (LayoutParams) mSubContainer.getLayoutParams();
        mRightSideSubParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRightSideSubParams.leftMargin = mLeftSideSubParams.rightMargin;
        mRightSideSubParams.rightMargin = mLeftSideSubParams.leftMargin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mRightSideSubParams.setMarginStart(mLeftSideSubParams.getMarginEnd());
            mRightSideSubParams.setMarginEnd(mLeftSideSubParams.getMarginStart());
        }
    }
}
