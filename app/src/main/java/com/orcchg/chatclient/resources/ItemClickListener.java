package com.orcchg.chatclient.resources;

import android.view.View;

public class ItemClickListener {
    private View.OnClickListener mClickListener;
    private View.OnLongClickListener mLongClickListener;

    public ItemClickListener() {
    }

    public void setClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }

    public void setLongClickListener(View.OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public View.OnClickListener getClickListener() {
        return mClickListener;
    }

    public View.OnLongClickListener getLongClickListener() {
        return mLongClickListener;
    }
}
