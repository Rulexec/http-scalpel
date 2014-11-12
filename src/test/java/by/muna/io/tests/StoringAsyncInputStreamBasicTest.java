package by.muna.io.tests;

import by.muna.io.StoringAsyncInputStream;
import by.muna.io.IByteReader;
import by.muna.io.RawBytesByteReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

public class StoringAsyncInputStreamBasicTest {
    @Test
    public void basicTest() {
        StoringAsyncInputStream stream = new StoringAsyncInputStream();

        byte[] expected = new byte[] { 1, 2, 3 };
        IByteReader bytesToWrite = new RawBytesByteReader(expected);

        stream.getOutputStream().onCanWrite(writer -> {
            writer.write(bytesToWrite);
            return !bytesToWrite.isEnded();
        });
        stream.getOutputStream().requestWriting();

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
