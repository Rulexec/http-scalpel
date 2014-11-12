package by.muna.io;

import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.util.function.Function;

public class RawBytesAsyncInputStream implements IAsyncByteInputStream {
    private Function<IByteReader, Boolean> listener;

    private ByteReaderWithListenableEnd reader;

    public RawBytesAsyncInputStream(byte[] bytes) {
        this.reader = new ByteReaderWithListenableEnd(new RawBytesByteReader(bytes));
    }

    @Override
    public void requestReading() {
        this.listener.apply(this.reader);
    }

    @Override
    public void onCanRead(Function<IByteReader, Boolean> listener) {
        this.listener = listener;
    }

    @Override
    public boolean isEnded() {
        return this.reader.isEnded();
    }

    @Override
    public IAsyncFuture<Object> end() {
        return callback -> {
            this.reader.end();
            callback.accept(null);
        };
    }

    @Override
    public IAsyncFuture<Object> onEnd() {
        return this.reader.onEnd();
    }
}
