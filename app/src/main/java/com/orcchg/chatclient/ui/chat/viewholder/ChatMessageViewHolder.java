package com.orcchg.chatclient.ui.chat.viewholder;

import android.os.Build;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orcchg.chatclient.ChatClientApplication;
import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.resources.MessageDrawable;
import com.orcchg.chatclient.resources.MessageView;
import com.orcchg.chatclient.resources.PhotoItem;
import com.orcchg.chatclient.util.CommonUtility;
import com.orcchg.jgravatar.Gravatar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatMessageViewHolder extends ChatBaseViewHolder<MessageVO> {

    @Bind(R.id.photo) PhotoItem mPhotoView;
    @Bind(R.id.message) MessageView mMessageView;
    @Bind(R.id.left_container) ViewGroup mLeftContainer;
    @Bind(R.id.right_container) ViewGroup mRightContainer;
    @Bind(R.id.left_text) TextView mLeftText;
    @Bind(R.id.right_text) TextView mRightText;

    private final long mUserId;

    public static final @LayoutRes int sLayoutId = R.layout.chat_message_rv_item;

    public ChatMessageViewHolder(View view, long id) {
        super(view);
        ButterKnife.bind(this, view);
        mUserId = id;
    }

    @Override
    public void bind(MessageVO viewObject) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        String time = CommonUtility.getTime(viewObject.getTimestamp(), ChatClientApplication.getTimeZone());
        mLeftText.setText(time);
        mRightText.setText(time);

        long destId = viewObject.getDestId();
        boolean fromSelf = viewObject.getId() == mUserId;
        if (fromSelf) {  // self message
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
            }
            @MessageDrawable.Side int side = destId != Status.UNKNOWN_ID ? MessageDrawable.SPECIAL_TOP_RIGHT : MessageDrawable.TOP_RIGHT;
            mMessageView.setSide(side);
            mMessageView.getTitle().setVisibility(View.GONE);
            mPhotoView.setVisibility(View.GONE);
            mLeftContainer.setVisibility(View.VISIBLE);
            mRightContainer.setVisibility(View.GONE);
        } else if (viewObject.getId() == Status.SYSTEM_ID) {  // system message
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mMessageView.setSide(MessageDrawable.NO_SIDE);
            mMessageView.getTitle().setVisibility(View.GONE);
            mPhotoView.setVisibility(View.GONE);
            mLeftContainer.setVisibility(View.GONE);
            mRightContainer.setVisibility(View.GONE);
        } else {  // another peer's message
            params.addRule(RelativeLayout.RIGHT_OF, R.id.space);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.END_OF, R.id.space);
            }
            Gravatar gravatar = new Gravatar();
            String url = gravatar.getUrl(viewObject.getEmail());
            @MessageDrawable.Side int side = destId != Status.UNKNOWN_ID ? MessageDrawable.SPECIAL_TOP_LEFT : MessageDrawable.TOP_LEFT;
            mMessageView.setSide(side);
            mMessageView.getTitle().setVisibility(View.VISIBLE);
            mPhotoView.setVisibility(View.VISIBLE);
            mPhotoView.setPhoto(url, true);
            mLeftContainer.setVisibility(View.GONE);
            mRightContainer.setVisibility(View.VISIBLE);
        }

        mMessageView.getTitle().setText(viewObject.getLogin());
        mMessageView.getDescription().setText(viewObject.getMessage());
        mMessageView.setLayoutParams(params);
        if (mOnMessageItemClickListener != null) {
            mMessageView.setOnClickListener(mOnMessageItemClickListener.getClickListener());
            mMessageView.setOnLongClickListener(mOnMessageItemClickListener.getLongClickListener());
        }

        mPhotoView.setItemClickListener(mOnPhotoItemClickListener);
    }

    @Override
    public void setTimestampVisibility(boolean isVisible) {
        super.setTimestampVisibility(isVisible);
        if (mLeftText != null) {
            mLeftText.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
        if (mRightText != null) {
            mRightText.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
