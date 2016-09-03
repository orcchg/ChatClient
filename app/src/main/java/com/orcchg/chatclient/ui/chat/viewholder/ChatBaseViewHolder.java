package com.orcchg.chatclient.ui.chat.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.orcchg.chatclient.resources.ItemClickListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public abstract class ChatBaseViewHolder<ViewObject> extends RecyclerView.ViewHolder {
    protected ItemClickListener mOnMessageItemClickListener;
    protected ItemClickListener mOnPhotoItemClickListener;

    public ChatBaseViewHolder(View view) {
        super(view);
    }

    public void setOnItemClickListener(ItemClickListener messageListener, ItemClickListener photoListener) {
        mOnMessageItemClickListener = createOnItemClickListener(new WeakReference<>(messageListener));
        mOnPhotoItemClickListener = createOnItemClickListener(new WeakReference<>(photoListener));
    }

    public abstract void bind(ViewObject viewObject);

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private ItemClickListener createOnItemClickListener(final WeakReference<ItemClickListener> ref) {
        ItemClickListener listener = new ItemClickListener();
        listener.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ref != null) {
                    ItemClickListener listener = ref.get();
                    if (listener != null && listener.getClickListener() != null) {
                        listener.getClickListener().onClick(view);
                    } else {
                        Timber.w("ItemClickListener has already been GC'ed");
                    }
                }
            }
        });
        listener.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (ref != null) {
                    ItemClickListener listener = ref.get();
                    if (listener != null && listener.getLongClickListener() != null) {
                        return listener.getLongClickListener().onLongClick(view);
                    }
                } else {
                    Timber.w("ItemClickListener has already been GC'ed");
                }
                return false;
            }
        });
        return listener;
    }
}
