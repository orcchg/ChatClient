package com.orcchg.chatclient.ui.chat.viewholder;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.RelativeLayout;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.resources.MessageDrawable;
import com.orcchg.chatclient.resources.MessageView;
import com.orcchg.chatclient.resources.PhotoItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatMessageViewHolder extends ChatBaseViewHolder<MessageVO> {

    @Bind(R.id.photo) PhotoItem mPhotoView;
    @Bind(R.id.message) MessageView mMessageView;

    public static final @LayoutRes int sLayoutId = R.layout.chat_message_rv_item;

    public ChatMessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(MessageVO viewObject) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        boolean fromSelf = true;  // TODO: arrange flag
        if (fromSelf) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            mMessageView.setSide(MessageDrawable.TOP_RIGHT);
        } else {
            params.addRule(RelativeLayout.RIGHT_OF, R.id.space);
            params.addRule(RelativeLayout.END_OF, R.id.space);
            mMessageView.setSide(MessageDrawable.TOP_LEFT);
        }

        mMessageView.getTitle().setText(viewObject.getLogin());
        mMessageView.getDescription().setText(viewObject.getMessage());
        mMessageView.setLayoutParams(params);
    }
}
