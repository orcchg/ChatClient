package com.orcchg.chatclient.ui.chat.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ChatBaseViewHolder<ViewObject> extends RecyclerView.ViewHolder {

    public ChatBaseViewHolder(View view) {
        super(view);
    }

    public abstract void bind(ViewObject viewObject);
}
