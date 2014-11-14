package by.muna.http.core;

import by.muna.io.IAsyncByteOutputStream;
import by.muna.io.InputStreamWithReturnableInput;
import by.muna.monads.AsyncFutureUtil;
import by.muna.monads.IAsyncFuture;

import java.util.Arrays;
import java.util.function.Consumer;

class HTTPMessageRaw implements IHTTPMessageRaw {
    private int messageNo;

    private HTTPConnectionInputStreamController httpInputStream;
    private HTTPConnectionOutputStreamController httpOutputStream;

    HTTPMessageRaw(HTTPConnection httpConnection, int messageNo) {
        this.messageNo = messageNo;

        this.httpInputStream = httpConnection.httpInputStream;
        this.httpOutputStream = httpConnection.httpOutputStream;
    }

    @Override
    public void onHeaderLine(Consumer<String> consumer) {
        this.httpInputStream.onHeaderLine(this.messageNo, consumer);
    }

    @Override
    public IAsyncFuture<Object> onHeadersEnd() {
        return this.httpInputStream.onHeadersEnd(this.messageNo);
    }

    @Override
    public IAsyncFuture<Object> sendLine(String line) {
        return this.httpOutputStream.sendLine(this.messageNo, line);
    }

    @Override
    public IAsyncFuture<Object> onError() {
        return AsyncFutureUtil.parallel(Arrays.asList(
            this.httpInputStream.onError(this.messageNo),
            this.httpOutputStream.onError(this.messageNo)
        ));
    }

    @Override
    public InputStreamWithReturnableInput getBodyInputStream() {
        return this.httpInputStream.getBodyInputStream(this.messageNo);
    }

    @Override
    public IAsyncByteOutputStream getBodyOutputStream() {
        return this.httpOutputStream.getBodyOutputStream(this.messageNo);
    }
}
