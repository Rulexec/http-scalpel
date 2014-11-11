package by.muna.util;

public class BytesUtil {
    public static byte[] slice(byte[] bytes) {
        return BytesUtil.slice(bytes, 0, bytes.length);
    }
    public static byte[] slice(byte[] bytes, int offset) {
        return BytesUtil.slice(bytes, offset, bytes.length - offset);
    }
    public static byte[] slice(byte[] bytes, int offset, int length) {
        byte[] result = new byte[length];

        System.arraycopy(bytes, offset, result, 0, length);

        return result;
    }
}
