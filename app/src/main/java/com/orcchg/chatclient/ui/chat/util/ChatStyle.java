package com.orcchg.chatclient.ui.chat.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ChatStyle {
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_DEDICATED = 1;
    @IntDef({STYLE_NORMAL, STYLE_DEDICATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {}
}
