package by.muna.io;

import java.nio.ByteBuffer;

public class RawBytesByteReader implements IByteReader {
    private byte[] bytes;
    private int offset = 0;

    public RawBytesByteReader(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        if (this.checkEnd()) return 0;
        int canRead = Math.min(this.bytes.length - this.offset, length);
        System.arraycopy(this.bytes, this.offset, buffer, offset, canRead);
        this.offset += canRead;
        this.checkEnd();
        return canRead;
    }

    @Override
    public int read(ByteBuffer buffer) {
        if (this.checkEnd()) return 0;
        int canRead = Math.min(this.bytes.length - this.offset, buffer.remaining());
        buffer.put(this.bytes, this.offset, canRead);
        this.offset += canRead;
        this.checkEnd();
        return canRead;
    }

    @Override
    public int read(IByteWriter writer) {
        if (this.checkEnd()) return 0;
        int written = writer.write(this.bytes, this.offset, this.bytes.length - this.offset);
        this.offset += written;
        this.checkEnd();
        return written;
    }

    private boolean checkEnd() {
        if (this.bytes != null) {
            if (this.offset == this.bytes.length) {
                this.bytes = null;
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void end() {
        this.bytes = null;
    }

    @Override
    public boolean isEnded() {
        return this.bytes == null;
    }
}
