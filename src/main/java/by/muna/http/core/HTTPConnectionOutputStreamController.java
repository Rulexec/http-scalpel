package by.muna.http.core;

import by.muna.io.IAsyncByteOutputStream;
import by.muna.monads.IAsyncFuture;

class HTTPConnectionOutputStreamController {
    private int currentMessageNo = 0;

    HTTPConnectionOutputStreamController(IAsyncByteOutputStream outputStream) {
        //
    }

    IAsyncFuture<Object> sendLine(int messageNo, String line) {
        return null;
    }

    IAsyncFuture<Object> onError(int messageNo) {
        return null;
    }

    IAsyncByteOutputStream getBodyOutputStream(int messageNo) {
        return null;
    }
}
