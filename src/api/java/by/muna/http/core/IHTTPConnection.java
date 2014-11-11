package by.muna.http.core;

import by.muna.monads.IAsyncFuture;

import java.util.function.Consumer;

public interface IHTTPConnection {
    /**
     * Instantly calls consumer, providing current message. Then, when body input or output is closed, provides next.
     * If input/output closes due connection close, it can be not called.
     * @param consumer
     */
    void onMessage(Consumer<IHTTPMessageRaw> consumer);

    IAsyncFuture<Object> onEnd();
    IAsyncFuture<Object> end();
}
