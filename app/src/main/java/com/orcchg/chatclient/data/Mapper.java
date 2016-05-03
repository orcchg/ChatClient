package com.orcchg.chatclient.data;

public interface Mapper<From, To> {
    To map(From object);
}
