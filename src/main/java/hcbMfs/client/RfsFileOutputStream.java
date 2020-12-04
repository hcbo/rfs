package hcbMfs.client;

import com.google.gson.Gson;
import com.lambdaworks.redis.RedisConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class RfsFileOutputStream extends OutputStream {

    private final int BYTE_BUFFER_SIZE = 1024;
    private byte[] byteBuffer = new byte[BYTE_BUFFER_SIZE];
    private int pointer;
    private String path;
    private PathInfo pathInfo;
    private static Gson gson = new Gson();
    private RedisConnection<String, String> connection;
    private RedisConnection<String, byte[]> dataConnection;


    public RfsFileOutputStream(RedisConnection connection, RedisConnection dataConnection,
                               String path) {
        MfsFileSystem.LOG.error("RfsFileOutputStream构造方法调用 " + path);
        this.connection = connection;
        this.dataConnection = dataConnection;
        pathInfo = new PathInfo();
        this.path = path;
        pathInfo.setDirectory(false);
        MfsFileSystem.LOG.error("RfsFileOutputStream构造方法调用结束");
    }

    @Override
    public void write(int b) throws IOException {
        byteBuffer[pointer] = (byte) b;
        pointer++;
    }

    @Override
    public void close() throws IOException {
        MfsFileSystem.LOG.error("RfsFileOutputStream.close()调用:"+"pathInfo.name"+path);
        FileInfo fileInfo = new FileInfo(pointer);
        pathInfo.setFileInfo(fileInfo);
        String parentPath = getParentPath(path);
        connection.sadd(RfsUnderFileSystem.INDEX + parentPath, path);
        //data
        dataConnection.set(RfsUnderFileSystem.FILEDATA + path, Arrays.copyOf(byteBuffer, pointer));
        //metadata
        connection.set(RfsUnderFileSystem.METADATA + path, gson.toJson(pathInfo));
        super.close();
    }

    // checkpointRoot/state/3/199
    private String getParentPath(String path) {
        String parentPath = path.substring(0,path.lastIndexOf('/'));
        return parentPath;
    }
}
