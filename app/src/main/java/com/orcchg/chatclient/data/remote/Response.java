package com.orcchg.chatclient.data.remote;

import timber.log.Timber;

public class Response {

    static Response obtain(char[] buffer) {
        Timber.v(new String(buffer));
        return new Response();
    }
}
