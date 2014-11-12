package by.muna.io.tests;

import by.muna.io.AsyncReturnableInputStream;
import by.muna.io.IByteReader;
import by.muna.io.RawBytesAsyncInputStream;
import by.muna.io.RawBytesByteReader;
import by.muna.util.BytesUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

public class AsyncReturnableInputStreamBasicTest {
    @Test
    public void basicTest() {
        byte[] expected = new byte[] { 1, 2, 3 };

        AsyncReturnableInputStream stream = new AsyncReturnableInputStream(
            new RawBytesAsyncInputStream(BytesUtil.slice(expected, 1, 2))
        );

        stream.returnInput(new RawBytesByteReader(new byte[] { 1 }));

        byte[] actual = new byte[expected.length];

        stream.onCanRead(new Function<IByteReader, Boolean>() {
            private int offset = 0;

            @Override
            public Boolean apply(IByteReader reader) {
                this.offset += reader.read(actual, this.offset, actual.length - this.offset);

                return offset != expected.length;
            }
        });
        stream.requestReading();

        Assert.assertArrayEquals(expected, actual);
    }
}
