package by.muna.io;

import by.muna.io.returnable.IReturnableInput;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

// TODO: implement more efficiently with mixed buffer types/etc
public class StoringAsyncInputStream implements IAsyncByteInputStream {
    private static final int BUFFER_SIZE = 1024;

    //private Deque<IByteReader> returned = new LinkedList<>();

    private Queue<byte[]> buffers = new LinkedList<>();

    private byte[] lastBuffer = new byte[BUFFER_SIZE];
    { this.buffers.add(this.lastBuffer); }

    private int writingOffset = 0;
    private int readingOffset = 0;

    private boolean ended = false;
    private boolean outputClosed = false;

    private boolean readingRequested = false;
    private Function<IByteReader, Boolean> listener;

    private OneTimeEventAsyncFuture<Object> endEvent = new OneTimeEventAsyncFuture<>();

    public StoringAsyncInputStream() {
        //
    }

    private IAsyncByteOutputStream outputStream = new IAsyncByteOutputStream() {
        private OneTimeEventAsyncFuture<Object> outputEndEvent = new OneTimeEventAsyncFuture<>();
        private Function<IByteWriter, Boolean> listener;

        @Override
        public void requestWriting() {
            if (StoringAsyncInputStream.this.outputClosed) {
                this.listener.apply(ByteWriterUtil.empty());
            } else {
                // oh
                final IAsyncByteOutputStream self = this;

                this.listener.apply(new IByteWriter() {
                    // TODO: implement other methods

                    @Override
                    public int write(byte[] buff, int offset, int length) {
                        int writingOffset = StoringAsyncInputStream.this.writingOffset;
                        byte[] buffer = StoringAsyncInputStream.this.lastBuffer;

                        int totalWritten = 0;

                        while (length > 0) {
                            int canWrite = Math.min(BUFFER_SIZE - writingOffset, length);
                            if (canWrite == 0) {
                                buffer = new byte[BUFFER_SIZE];
                                synchronized (StoringAsyncInputStream.this) {
                                    StoringAsyncInputStream.this.buffers.add(buffer);
                                    StoringAsyncInputStream.this.lastBuffer = buffer;
                                    StoringAsyncInputStream.this.writingOffset = 0;
                                }

                                writingOffset = 0;
                            }

                            System.arraycopy(buff, offset, buffer, writingOffset, canWrite);

                            offset += canWrite;
                            length -= canWrite;
                            writingOffset += canWrite;

                            totalWritten += canWrite;
                        }

                        StoringAsyncInputStream.this.writingOffset = writingOffset;

                        if (StoringAsyncInputStream.this.readingRequested) {
                            StoringAsyncInputStream.this.doReading(false);
                        }

                        return totalWritten;
                    }

                    @Override
                    public void end() {
                        self.end().run(x -> {});
                    }

                    @Override
                    public boolean isEnded() {
                        return StoringAsyncInputStream.this.outputClosed;
                    }
                });
            }
        }

        @Override
        public void onCanWrite(Function<IByteWriter, Boolean> listener) {
            this.listener = listener;
        }

        @Override
        public boolean isEnded() {
            return StoringAsyncInputStream.this.ended || StoringAsyncInputStream.this.outputClosed;
        }

        @Override
        public IAsyncFuture<Object> end() {
            return callback -> {
                if (StoringAsyncInputStream.this.outputClosed) {
                    callback.accept(null);
                } else {
                    StoringAsyncInputStream.this.outputClosed = true;

                    if (StoringAsyncInputStream.this.readingRequested) {
                        StoringAsyncInputStream.this.doReading(false);
                    }

                    this.outputEndEvent.event(null);

                    callback.accept(null);
                }
            };
        }

        @Override
        public IAsyncFuture<Object> onEnd() {
            return this.outputEndEvent;
        }
    };
    public IAsyncByteOutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void onCanRead(Function<IByteReader, Boolean> listener) {
        this.listener = listener;
    }

    /*@Override
    public void returnInput(IByteReader reader) {
        this.returned.addFirst(reader);
    }*/

    @Override
    public IAsyncFuture<Object> end() {
        return callback -> {
            this._end();
            callback.accept(null);
        };
    }

    private void _end() {
        StoringAsyncInputStream.this.ended = true;
        this.endEvent.event(null);

        this.listener.apply(ByteReaderUtil.empty());
    }

    @Override
    public void requestReading() {
        this.readingRequested = false;

        if (this.ended) {
            this.listener.apply(ByteReaderUtil.empty());
        } else {
            this.doReading(true);
        }
    }

    private void doReading(boolean requestReading) {
        synchronized (this) {
            if (this.ended) return;

            boolean readingRequested = this.readingRequested || requestReading;
            // this is needed to sometimes not call doReading from writing, if doReading already executes
            this.readingRequested = false;

            reading: {
                /*returnedReading: while (!this.returned.isEmpty()) {
                    IByteReader rest;// = this.returned.getFirst();

                    while (true) {
                        rest = this.returned.getFirst();

                        if (rest.isEnded()) this.returned.pollFirst();
                        else break;

                        if (this.returned.isEmpty()) break returnedReading;
                    }

                    readingRequested = this.listener.apply(rest);

                    if (rest.isEnded()) this.returned.pollFirst();

                    if (!readingRequested) break reading;
                }*/

                while (this.buffers.size() > 1 || this.readingOffset < this.writingOffset) {
                    readingRequested = this.listener.apply(new IByteReader() {
                        @Override
                        public int read(byte[] buffer, int offset, int length) {
                            // TODO: we can check for next buffer, if canRead < length

                            Queue<byte[]> buffers = StoringAsyncInputStream.this.buffers;
                            byte[] firstBuffer = buffers.peek();
                            //if (firstBuffer == null) return 0;

                            int readingOffset = StoringAsyncInputStream.this.readingOffset;
                            int canRead = Math.min(BUFFER_SIZE - readingOffset, length);

                            int writingOffset = StoringAsyncInputStream.this.writingOffset;
                            if (firstBuffer == StoringAsyncInputStream.this.lastBuffer) {
                                // this is thread-safe, because we are cached writingOffset
                                canRead = Math.min(canRead, writingOffset - readingOffset);
                            }

                            System.arraycopy(firstBuffer, readingOffset, buffer, offset, canRead);

                            readingOffset += canRead;
                            if (readingOffset == BUFFER_SIZE) {
                                buffers.poll();
                                readingOffset = 0;
                            }

                            StoringAsyncInputStream.this.readingOffset = readingOffset;

                            return canRead;
                        }

                        @Override
                        public void end() {
                            StoringAsyncInputStream.this._end();
                        }

                        @Override
                        public boolean isEnded() {
                            return StoringAsyncInputStream.this.ended;
                        }
                    });

                    if (!readingRequested) break reading;
                }
            }

            if (this.outputClosed && this.buffers.size() == 1 && this.readingOffset == this.writingOffset) {
                this._end();
            }

            this.readingRequested = readingRequested;
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
}
