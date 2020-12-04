package hcbMfs.client;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.LettuceCharsets;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static java.nio.charset.CoderResult.OVERFLOW;

public class StringByteCodec extends RedisCodec<String, byte[]> {

    private Charset charset;
    private CharsetDecoder decoder;
    private CharBuffer chars;

    /**
     * Initialize a new instance that encodes and decodes strings using the UTF-8 charset;
     */
    public StringByteCodec() {
        charset = LettuceCharsets.UTF8;
        decoder = charset.newDecoder();
        chars = CharBuffer.allocate(1024);
    }
    @Override
    public String decodeKey(ByteBuffer bytes) {
        return decode(bytes);
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
        byte[] arr = new byte[bytes.remaining()];
        bytes.get(arr);
        return arr;
    }

    @Override
    public byte[] encodeKey(String key) {
        return encode(key);
    }

    @Override
    public byte[] encodeValue(byte[] value) {
        return value;
    }

    private synchronized String decode(ByteBuffer bytes) {
        chars.clear();
        bytes.mark();

        decoder.reset();
        while (decoder.decode(bytes, chars, true) == OVERFLOW || decoder.flush(chars) == OVERFLOW) {
            chars = CharBuffer.allocate(chars.capacity() * 2);
            bytes.reset();
        }

        return chars.flip().toString();
    }

    private byte[] encode(String string) {
        return string.getBytes(charset);
    }

    private static byte[] getBytes(final ByteBuffer buffer) {
        final byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return b;
    }

}
