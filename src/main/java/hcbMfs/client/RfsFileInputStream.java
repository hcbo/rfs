package hcbMfs.client;

import com.lambdaworks.redis.RedisConnection;
import org.apache.hadoop.fs.FSInputStream;

import java.io.IOException;

public class RfsFileInputStream extends FSInputStream {
    private byte[] byteBuffer ;
    private int pointer;
    private RedisConnection<String, byte[]> dataConnection;

    public RfsFileInputStream( RedisConnection dataConnection,
                              String path) {
        MfsFileSystem.LOG.error("RfsFileInputStream.构造函数调用");
        this.dataConnection = dataConnection;
        byteBuffer = (byte[]) dataConnection.get(RfsUnderFileSystem.FILEDATA + path);
        MfsFileSystem.LOG.error("RfsFileInputStream.构造函数调用结束"+ " "+byteBuffer.length);

    }

    @Override
    public int read() throws IOException {
        if(pointer < byteBuffer.length){
            int res = (int)byteBuffer[pointer];
            pointer++;
            return res&(0xff);
        }
        return -1;
    }

    @Override
    public void seek(long pos) throws IOException {

    }

    @Override
    public long getPos() throws IOException {
        return 0;
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        return false;
    }
}
