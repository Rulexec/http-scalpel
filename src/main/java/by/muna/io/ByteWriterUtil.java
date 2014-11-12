package by.muna.io;

import java.nio.ByteBuffer;

public class ByteWriterUtil {
    public static IByteWriter empty() {
        return new IByteWriter() {
            @Override
            public int write(byte[] buffer, int offset, int length) {
                return 0;
            }

            @Override
            public int write(ByteBuffer buffer) {
                return 0;
            }

            @Override
            public int write(IByteReader reader) {
                return 0;
            }

            @Override
            public void end() {}

            @Override
            public boolean isEnded() {
                return true;
            }
        };
    }
}
