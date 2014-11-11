package by.muna.http.core;

import by.muna.io.IAsyncByteInputStream;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.IAsyncMonad;
import by.muna.monads.OneTimeEventAsyncFuture;
import by.muna.monads.OneTimeEventAsyncMonad;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

class HTTPConnectionInputStreamController {
    private int currentMessageNo = 0;

    private IAsyncByteInputStream inputStream;

    private Map<Integer, OneTimeEventAsyncMonad<String, Object>> startLineEvents = new HashMap<>();
    private Map<Integer, BiConsumer<String, Object>> headerLineConsumers = new HashMap<>();
    private Map<Integer, OneTimeEventAsyncFuture<Object>> headersEndEvents = new HashMap<>();
    private Map<Integer, OneTimeEventAsyncFuture<Object>> errorEvents = new HashMap<>();

    HTTPConnectionInputStreamController(IAsyncByteInputStream inputStream) {
        this.inputStream = inputStream;
    }

    IAsyncMonad<String, Object> onStartLine(int messageNo) {
        return this.startLineEvents.computeIfAbsent(messageNo, x -> {
            if (HTTPConnectionInputStreamController.this.currentMessageNo == messageNo) {
                HTTPConnectionInputStreamController.this.inputStream.requestReading();
            }

            return new OneTimeEventAsyncMonad<>();
        });
    }

    void onHeaderLine(int messageNo, BiConsumer<String, Object> consumer) {
        this.headerLineConsumers.put(messageNo, consumer);

        if (this.currentMessageNo == messageNo) this.inputStream.requestReading();
    }

    IAsyncFuture<Object> onHeadersEnd(int messageNo) {
        return this.headersEndEvents.computeIfAbsent(messageNo, x -> {
            if (HTTPConnectionInputStreamController.this.currentMessageNo == messageNo) {
                HTTPConnectionInputStreamController.this.inputStream.requestReading();
            }

            return new OneTimeEventAsyncFuture<>();
        });
    }

    IAsyncFuture<Object> onError(int messageNo) {
        return this.errorEvents.computeIfAbsent(messageNo, x -> new OneTimeEventAsyncFuture<>());
    }

    IAsyncByteInputStream getBodyInputStream(int messageNo) {
        return null;
    }
}
