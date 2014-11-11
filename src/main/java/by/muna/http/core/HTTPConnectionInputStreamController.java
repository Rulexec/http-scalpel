package by.muna.http.core;

import by.muna.io.IAsyncByteInputStream;
import by.muna.io.IByteReader;
import by.muna.io.returnable.IAsyncReturnableInputStream;
import by.muna.monads.IAsyncFuture;
import by.muna.monads.OneTimeEventAsyncFuture;
import by.muna.util.BytesUtil;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class HTTPConnectionInputStreamController {
    private static enum MessagePhase {
        HEADERS, BODY
    }

    // TODO: move this to contructor of HTTPConnection
    private static final int HEADERS_MAX_SIZE = 16 * 1024;

    private int currentMessageNo = 0;
    private MessagePhase messagePhase = MessagePhase.HEADERS;
    private byte[] headersBuffer = new byte[HTTPConnectionInputStreamController.HEADERS_MAX_SIZE];
    private int headersBufferOffset = 0;
    private boolean headersPrevCR = false;

    private int headersStartFrom = 0;

    private IAsyncByteInputStream inputStream;

    private Map<Integer, Consumer<String>> headersLineConsumers = new HashMap<>();
    private Map<Integer, OneTimeEventAsyncFuture<Object>> headersEndEvents = new HashMap<>();
    //private Map<Integer, OneTimeEventAsyncFuture<Object>> errorEvents = new HashMap<>();
    private OneTimeEventAsyncFuture errorEvent = new OneTimeEventAsyncFuture();

    HTTPConnectionInputStreamController(IAsyncByteInputStream inputStream) {
        this.inputStream = inputStream;

        this.inputStream.onCanRead(this::inputReading);
    }
    private boolean inputReading(IByteReader reader) {
        switch (this.messagePhase) {
        case HEADERS:
            int readed = reader.read(this.headersBuffer, this.headersBufferOffset);
            int newOffset = this.headersBufferOffset + readed;

            int pos;
            headersLoop: for (pos = this.headersBufferOffset; pos < newOffset; pos++) {
                switch (this.headersBuffer[pos]) {
                case '\r': this.headersPrevCR = true; break;
                case '\n':
                    if (this.headersPrevCR) {
                        int length = pos - 2 - this.headersStartFrom;

                        if (length > 0) {
                            this.headersLineConsumers.get(this.currentMessageNo).accept(
                                new String(BytesUtil.slice(
                                    this.headersBuffer,
                                    this.headersStartFrom,
                                    length
                                ), Charset.forName("ISO-8859-1"))
                            );

                            this.headersStartFrom = pos + 1;
                        } else {
                            this.headersEndEvents.get(this.currentMessageNo).event(null);
                            this.messagePhase = MessagePhase.BODY;

                            this.headersBufferOffset = 0;
                            this.headersPrevCR = false;
                            this.headersStartFrom = 0;

                            break headersLoop;
                        }
                    } else {
                        this.headersPrevCR = false;
                    }
                default:
                    this.headersPrevCR = false;
                }
            }

            if (this.messagePhase == MessagePhase.BODY && pos != newOffset - 1) {
                // TODO: get rest, put to body input stream
            }
            break;
        case BODY:
            break;
        default: throw new RuntimeException("Impossible: " + this.messagePhase);
        }

        return true;
    }

    void onHeaderLine(int messageNo, Consumer<String> consumer) {
        this.headersLineConsumers.put(messageNo, consumer);

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
        return this.errorEvent;
    }

    IAsyncReturnableInputStream getBodyInputStream(int messageNo) {
        return null;
    }
}
