package com.orcchg.chatclient.ui.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.ui.chat.viewholder.ChatBaseViewHolder;
import com.orcchg.chatclient.ui.chat.viewholder.ChatMessageViewHolder;
import com.orcchg.chatclient.resources.ItemClickListener;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatBaseViewHolder> {

    private final long mUserId;
    private List<MessageVO> mMessagesList;

    private ItemClickListener mOnMessageItemClickListener;
    private ItemClickListener mOnPhotoItemClickListener;

    public ChatAdapter(long id, @NonNull List<MessageVO> messagesList) {
        mUserId = id;
        mMessagesList = messagesList;
    }

    public void setOnItemClickListener(ItemClickListener messageListener, ItemClickListener photoListener) {
        mOnMessageItemClickListener = messageListener;
        mOnPhotoItemClickListener = photoListener;
    }

    public void restoreMessages(List<MessageVO> messagesList) {
        mMessagesList = messagesList;
    }

    @Override
    public ChatBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(ChatMessageViewHolder.sLayoutId, parent, false);
        ChatBaseViewHolder holder = new ChatMessageViewHolder(view, mUserId);
        holder.setOnItemClickListener(mOnMessageItemClickListener, mOnPhotoItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatBaseViewHolder holder, int position) {
        holder.bind(mMessagesList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }
}
