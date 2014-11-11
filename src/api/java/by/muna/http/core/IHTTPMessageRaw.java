package by.muna.http.core;

import by.muna.io.IAsyncByteInputStream;
import by.muna.io.IAsyncByteOutputStream;
import by.muna.io.returnable.IAsyncReturnableInputStream;
import by.muna.monads.AsyncFutureUtil;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.IAsyncMonad;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface IHTTPMessageRaw {
    void onHeaderLine(Consumer<String> consumer);
    IAsyncFuture<Object> onHeadersEnd();

    IAsyncFuture<Object> sendLine(String line);
    default IAsyncFuture<Object> sendLines(List<String> lines) {
        return AsyncFutureUtil.parallel(lines.stream().map(this::sendLine).collect(Collectors.toList()));
    }

    default IAsyncFuture<Object> endHeaders() {
        return this.sendLine("");
    }

    IAsyncFuture<Object> onError();

    /**
     * @return all returned bytes will be recognized as next HTTP-message.
     */
    IAsyncReturnableInputStream getBodyInputStream();
    IAsyncByteOutputStream getBodyOutputStream();
}
