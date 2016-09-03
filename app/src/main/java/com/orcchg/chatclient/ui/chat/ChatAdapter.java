package com.orcchg.chatclient.ui.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.chatclient.data.viewobject.MessageVO;
import com.orcchg.chatclient.resources.ItemClickListener;
import com.orcchg.chatclient.ui.chat.viewholder.ChatBaseViewHolder;
import com.orcchg.chatclient.ui.chat.viewholder.ChatMessageViewHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatBaseViewHolder> {

    private final long mUserId;
    private List<MessageVO> mMessagesList;

    private ItemClickListener mOnMessageItemClickListener;
    private ItemClickListener mOnPhotoItemClickListener;

    private List<WeakReference<ChatBaseViewHolder>> mHolderList;

    private boolean mShowTimestamp = false;

    public ChatAdapter(long id, @NonNull List<MessageVO> messagesList) {
        mUserId = id;
        mMessagesList = messagesList;
        mHolderList = new ArrayList<>();
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
        holder.setOnItemClickListener(
                wrapMessageItemClickListener(mOnMessageItemClickListener),
                mOnPhotoItemClickListener);
        holder.setTimestampVisibility(mShowTimestamp);
        mHolderList.add(new WeakReference<>(holder));
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

    private void setHoldersTimestampVisibility(boolean isVisible) {
        List<WeakReference<ChatBaseViewHolder>> copyList = new ArrayList<>();
        for (WeakReference<ChatBaseViewHolder> holderRef : mHolderList) {
            if (holderRef != null) {
                ChatBaseViewHolder holder = holderRef.get();
                if (holder != null) {
                    holder.setTimestampVisibility(isVisible);
                    copyList.add(holderRef);
                }
            }
        }
        mHolderList = copyList;
        mShowTimestamp = isVisible;
    }

    private ItemClickListener wrapMessageItemClickListener(final ItemClickListener messageListener) {
        ItemClickListener listener = new ItemClickListener();
        listener.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHoldersTimestampVisibility(!mShowTimestamp);
                if (messageListener != null) {
                    messageListener.getClickListener().onClick(v);
                }
            }
        });
        listener.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (messageListener != null) {
                    return messageListener.getLongClickListener().onLongClick(v);
                }
                return false;
            }
        });
        return listener;
    }
}
