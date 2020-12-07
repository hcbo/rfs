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
        MfsFileSystem.LOG.error("RfsFileOutputStream.close()调用:"+" pathInfo.name " + path);
        if (connection.exists(RfsUnderFileSystem.METADATA + getDstPath(path))) {
            MfsFileSystem.LOG.error("RfsFileOutputStream.close()调用提前结束:"+" pathInfo.name " + path + " pointer： " + pointer);
            return;
        }
        FileInfo fileInfo = new FileInfo(pointer);
        pathInfo.setFileInfo(fileInfo);
        String parentPath = getParentPath(path);
        connection.sadd(RfsUnderFileSystem.INDEX + parentPath, path);
        //data
        dataConnection.set(RfsUnderFileSystem.FILEDATA + path, Arrays.copyOf(byteBuffer, pointer));
        //metadata
        connection.set(RfsUnderFileSystem.METADATA + path, gson.toJson(pathInfo));
        String dstPath = getDstPath(path);
        renameFile(path, dstPath);
        MfsFileSystem.LOG.error("RfsFileOutputStream.close()调用结束:"+" pathInfo.name " + path + " pointer： " + pointer);
    }

    private void renameFile(String src, String dst) {
        connection.rename(RfsUnderFileSystem.METADATA + src,
                RfsUnderFileSystem.METADATA + dst);
        dataConnection.rename((RfsUnderFileSystem.FILEDATA + src),
                (RfsUnderFileSystem.FILEDATA + dst));
        connection.srem(RfsUnderFileSystem.INDEX + getParentPath(src), src);
        connection.sadd(RfsUnderFileSystem.INDEX + getParentPath(src), dst);
    }


    // checkRoot/.metadata.4ff267d1-d652-4f59-9bc7-e58f73de5101.tmp
    // checkRoot/sources/0/.0.df0a85b0-119f-4328-8d03-4c577f8a4bb4.tmp
    // checkRoot/offsets/.0.284558db-d413-4ef4-a8dd-47c76e0206cf.tmp
    // checkRoot/state/0/0/.1.delta.da59c601-944d-498e-af7a-3ed5be2d0c49.TID1.tmp
    // checkRoot/commits/.0.c0a39307-ba49-4e95-b80e-609be9a6b2f8.tmp
    private String getDstPath(String path) {
        String prefix = path.substring(0, path.lastIndexOf('/') + 1);
        String postPart = path.substring(path.lastIndexOf('/') + 2, path.length());
        String currentPath = postPart.substring(0, postPart.indexOf('.'));
        if (path.contains(".delta")) {
            currentPath = currentPath + ".delta";
        } else if (path.contains(".snapshot")) {
            currentPath = currentPath + ".snapshot";
        }
        return prefix + currentPath;
    }

    // checkpointRoot/state/3/199
    private String getParentPath(String path) {
        String parentPath = path.substring(0,path.lastIndexOf('/'));
        return parentPath;
    }
}
