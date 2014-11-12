package by.muna.io;

import by.muna.io.returnable.IAsyncReturnableInputStream;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncReturnableInputStream implements IAsyncReturnableInputStream {
    private IAsyncByteInputStream stream;

    private Deque<IByteReader> returned = new LinkedList<>();

    private Function<IByteReader, Boolean> listener;

    private OneTimeEventAsyncFuture<Object> endEvent = new OneTimeEventAsyncFuture<>();

    private boolean ended = false;
    private boolean streamEnded = false;
    private Object endedStreamError;

    private Consumer<Deque<IByteReader>> endReturnedConsumer = null;

    public AsyncReturnableInputStream(IAsyncByteInputStream stream) {
        this.stream = stream;

        this.stream.onEnd().run(this::_streamEnded);
    }

    /**
     * If used this constructor and endReturnedConsumer is not null, then passed stream will not be closed,
     * if closed this stream, but endReturnedConsumer will called.
     * @param stream
     * @param endReturnedConsumer
     */
    public AsyncReturnableInputStream(IAsyncByteInputStream stream, Consumer<Deque<IByteReader>> endReturnedConsumer) {
        this(stream);
        this.endReturnedConsumer = endReturnedConsumer;
    }

    @Override
    public void onCanRead(Function<IByteReader, Boolean> listener) {
        this.listener = listener;

        this.stream.onCanRead(r -> {
            if (!this.doReturnedReading()) return false;

            return listener.apply(r);
        });
    }

    private boolean doReturnedReading() {
        if (this.ended) return false;

        while (!this.returned.isEmpty()) {
            IByteReader rest;

            while (true) {
                rest = this.returned.getFirst();

                if (rest.isEnded()) this.returned.pollFirst();
                else break;

                if (this.returned.isEmpty()) return true;
            }

            boolean readingRequested = this.listener.apply(rest);

            if (rest.isEnded()) this.returned.pollFirst();

            if (!readingRequested) return false;
        }

        if (this.streamEnded && this.returned.isEmpty()) {
            this.ended = true;
            this.endEvent.event(this.endedStreamError);
        }

        return true;
    }

    private void _streamEnded(Object error) {
        this.endedStreamError = error;
        this.streamEnded = true;

        if (this.returned.isEmpty()) {
            this.ended = true;
            this.endEvent.event(error);
        }
    }

    @Override
    public void requestReading() {
        boolean readingRequested = this.doReturnedReading();

        if (readingRequested) {
            this.stream.requestReading();
        }
    }

    @Override
    public boolean isEnded() {
        return this.ended;
    }

    @Override
    public IAsyncFuture<Object> onEnd() {
        return this.endEvent;
    }

    @Override
    public void returnInput(IByteReader rest) {
        this.returned.addFirst(rest);
    }

    @Override
    public IAsyncFuture<Object> end() {
        return callback -> {
            this.ended = true;

            if (this.endReturnedConsumer == null) {
                this.stream.end().run(callback);
            } else {
                this.endReturnedConsumer.accept(this.returned);
                this.returned = null;

                this.endEvent.event(null);

                callback.accept(null);
            }
        };
    }
}
