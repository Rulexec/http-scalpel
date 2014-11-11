package by.muna.http.core;

import by.muna.io.IAsyncByteInputStream;
import by.muna.io.IAsyncByteOutputStream;
import by.muna.monads.AsyncFutureUtil;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.util.Arrays;
import java.util.function.Consumer;

public class HTTPConnection implements IHTTPConnection {
    private OneTimeEventAsyncFuture<Object> endEvent = new OneTimeEventAsyncFuture<>();

    private Consumer<IHTTPMessageRaw> messageConsumer;

    private IAsyncByteInputStream inputStream;
    private IAsyncByteOutputStream outputStream;

    HTTPConnectionInputStreamController httpInputStream;
    HTTPConnectionOutputStreamController httpOutputStream;

    private IHTTPMessageRaw lastMessage = null;
    private int lastMessageNo = 0;

    public HTTPConnection(IAsyncByteInputStream inputStream, IAsyncByteOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        this.httpInputStream = new HTTPConnectionInputStreamController(this.inputStream);
        this.httpOutputStream = new HTTPConnectionOutputStreamController(this.outputStream);

        /*AsyncFutureUtil.parallel(Arrays.asList(
            this.inputStream.onEnd().skip(this.outputStream.end()),
            this.outputStream.onEnd().skip(this.inputStream.end())
        )).run(x -> {});*/
    }

    @Override
    public void onMessage(Consumer<IHTTPMessageRaw> consumer) {
        this.messageConsumer = consumer;

        if (this.lastMessage == null) {
            this.lastMessage = new HTTPMessageRaw(this, this.lastMessageNo++);
        }

        this.messageConsumer.accept(this.lastMessage);
    }

    //

    @Override
    public IAsyncFuture<Object> onEnd() {
        return AsyncFutureUtil.parallel(Arrays.asList(this.inputStream.onEnd(), this.outputStream.onEnd()), x -> true);
    }

    @Override
    public IAsyncFuture<Object> end() {
        return AsyncFutureUtil.parallel(Arrays.asList(this.inputStream.end(), this.outputStream.end()), x -> true);
    }
}
