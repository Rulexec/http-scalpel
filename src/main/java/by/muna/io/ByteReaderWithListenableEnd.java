package by.muna.io;

import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.nio.ByteBuffer;

public class ByteReaderWithListenableEnd implements IByteReader {
    private IByteReader reader;
    private OneTimeEventAsyncFuture<Object> endEvent = new OneTimeEventAsyncFuture<>();

    public ByteReaderWithListenableEnd(IByteReader reader) {
        this.reader = reader;
    }

    @Override
    public int read(byte[] buffer) {
        int readed = this.reader.read(buffer);
        if (this.reader.isEnded()) this.endEvent.event(null);
        return readed;
    }

    @Override
    public int read(byte[] buffer, int offset) {
        int readed = this.reader.read(buffer, offset);
        if (this.reader.isEnded()) this.endEvent.event(null);
        return readed;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        int readed = this.reader.read(buffer, offset, length);
        if (this.reader.isEnded()) this.endEvent.event(null);
        return readed;
    }

    @Override
    public int read(ByteBuffer buffer) {
        int readed = this.reader.read(buffer);
        if (this.reader.isEnded()) this.endEvent.event(null);
        return readed;
    }

    @Override
    public int read(IByteWriter writer) {
        int readed = this.reader.read(writer);
        if (this.reader.isEnded()) this.endEvent.event(null);
        return readed;
    }

    @Override
    public void end() {
        this.reader.end();
        this.endEvent.event(null);
    }

    @Override
    public boolean isEnded() {
        return this.reader.isEnded();
    }

    public IAsyncFuture<Object> onEnd() {
        return this.endEvent;
    }
}
