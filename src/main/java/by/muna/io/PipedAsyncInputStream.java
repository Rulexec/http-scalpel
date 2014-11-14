package by.muna.io;

import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.util.function.Function;

public class PipedAsyncInputStream implements IAsyncByteInputStream {
    private boolean ended = false;

    private boolean readingRequested = false;
    private boolean writingRequested = false;

    private Function<IByteReader, Boolean> readListener;
    private Function<IByteWriter, Boolean> writeListener;

    private OneTimeEventAsyncFuture<Object> endEvent = new OneTimeEventAsyncFuture<>();

    public PipedAsyncInputStream() {}


    private IAsyncByteOutputStream outputStream = new IAsyncByteOutputStream() {
        @Override
        public void requestWriting(boolean request) {
            PipedAsyncInputStream.this.writingRequested = request;

            PipedAsyncInputStream.this.doPiping();
        }

        @Override
        public void onCanWrite(Function<IByteWriter, Boolean> listener) {
            PipedAsyncInputStream.this.writeListener = listener;
        }

        @Override
        public boolean isEnded() {
            return false;
        }

        @Override
        public IAsyncFuture<Object> end() {
            return null;
        }

        @Override
        public IAsyncFuture<Object> onEnd() {
            return null;
        }
    };
    public IAsyncByteOutputStream getOutputStream() {
        return this.outputStream;
    }

    private void doPiping() {
        if (this.readingRequested && this.writingRequested) {
            //this.writeListener.
        }
    }

    @Override
    public void requestReading(boolean request) {
        this.readingRequested = request;

        this.doPiping();
        //this.outputStream.requestWriting(request);
    }

    @Override
    public void onCanRead(Function<IByteReader, Boolean> listener) {
        this.readListener = listener;
    }

    @Override
    public boolean isEnded() {
        return this.outputStream.isEnded();
    }

    @Override
    public IAsyncFuture<Object> end() {
        return callback -> {
            this.ended = true;
            this.endEvent.event(null);
            this.outputStream.end().run(callback);
        };
    }

    @Override
    public IAsyncFuture<Object> onEnd() {
        return this.endEvent;
    }
}
