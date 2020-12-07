package hcbMfs.client;

import com.google.gson.Gson;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

public class RfsUnderFileSystem {

    public static final String METADATA = "metadata::";
    public static final String FILEDATA = "data::";
    public static final String INDEX = "index::";
    private RedisConnection<String, String> connection;
    private RedisConnection<String, byte[]> dataConnection;
    private String rootPath; //china
    private String protocol;
    private Gson gson = new Gson();

    public RfsUnderFileSystem(URI uri, Configuration conf) {
        MfsFileSystem.LOG.error("RfsUnderFileSystem 构造方法开始");
        this.rootPath = getRootPath(uri);
        this.protocol = getProtocolStr(uri);
        String ip = PropertyUtils.getIp();
        int redisPort = Integer.parseInt(PropertyUtils.getRedisPort());
        String password = PropertyUtils.getPassword();
        RedisClient redisClient = new RedisClient(
                RedisURI.create("redis://" + password + "@"+ ip + ":" + redisPort));
        this.connection = redisClient.connect();
        this.dataConnection = redisClient.connect(new StringByteCodec());
    }

    private String getProtocolStr(URI uri) {
        String fullPath = uri.toString();
        String port = PropertyUtils.getPort();
        // mfs://219.216.65.161:8888/
        return fullPath.substring(0,fullPath.indexOf(port) + port.length() + 1);
    }

    private String getRootPath(URI uri) {
        //mfs://localhost:8888/china
        String fullPath = uri.toString();
        String port = PropertyUtils.getPort();
        return fullPath.substring(fullPath.indexOf(port)+port.length()+1);
    }

    public InputStream open(String path) throws IOException {
        MfsFileSystem.LOG.error("open()方法执行 path="+path);
        path = trimPath(path);
        if (!connection.exists(METADATA + path)) {
            throw new FileNotFoundException("read non-exist file " + path);
        }
        return new RfsFileInputStream(dataConnection, path);
    }


    public OutputStream create(String path) throws IOException {
        MfsFileSystem.LOG.error("create()方法执行 path=" + path);
        path = trimPath(path);
        return new RfsFileOutputStream(connection, dataConnection, path);
    }

    public boolean renameFile(String src, String dst) {
        MfsFileSystem.LOG.error("renameFile()方法执行 src="+src+" dst"+dst);
        dst = trimPath(dst);
        String pathInfoJson = connection.get(RfsUnderFileSystem.METADATA + dst);
        PathInfo pathInfo = gson.fromJson(pathInfoJson, PathInfo.class);
        pathInfo.getFileInfo().setRenamed(true);
        connection.set(RfsUnderFileSystem.METADATA + dst, gson.toJson(pathInfo));

//        connection.rename(RfsUnderFileSystem.METADATA + src,
//                RfsUnderFileSystem.METADATA + dst);
//        dataConnection.rename((RfsUnderFileSystem.FILEDATA + src),
//                (RfsUnderFileSystem.FILEDATA + dst));
//        connection.srem(RfsUnderFileSystem.INDEX + getParentPath(src), src);
//        connection.sadd(RfsUnderFileSystem.INDEX + getParentPath(src), dst);
        return true;
    }

    public FileStatus[] listStatus(String path) throws FileNotFoundException {
        MfsFileSystem.LOG.error("listStatus()方法执行 path="+path);
        path = trimPath(path);
        if (!connection.exists(RfsUnderFileSystem.METADATA + path)) {
            throw new FileNotFoundException();
        }
        if (!connection.exists(RfsUnderFileSystem.INDEX + path)) {
            return new FileStatus[0];
        }
        Set<String> subPaths = connection.smembers(RfsUnderFileSystem.INDEX + path);
        FileStatus[] fileStatuses = new FileStatus[subPaths.size()];
        int i = 0;
        for (String subPath : subPaths) {
            String jsonPathInfo = connection.get(RfsUnderFileSystem.METADATA + subPath);
            PathInfo pathInfo = gson.fromJson(jsonPathInfo, PathInfo.class);
            FileStatus fileStatus;
            if (pathInfo.isDirectory()) {
                fileStatus = new FileStatus(0L, true,
                        0, 0L, pathInfo.getLastModified(),
                        new Path(this.protocol + subPath));
            } else {
                fileStatus = new FileStatus(pathInfo.getFileInfo().getContentLength(),
                        false, 1, 512L,
                        pathInfo.getLastModified(),new Path(this.protocol + subPath));
            }
            fileStatuses[i++] = fileStatus;
        }
        return fileStatuses;
    }

    public boolean mkdirs(String path) {
        MfsFileSystem.LOG.error("mkdirs()方法执行 path="+path);
        path = trimPath(path);
        if (connection.exists(RfsUnderFileSystem.METADATA + path)) {
            return false;
        } else {
            mkParentDirsRecv(path);
            return true;
        }
    }

    private void mkParentDirsRecv(String path) {
        PathInfo pathInfo = new PathInfo(true, System.currentTimeMillis());
        FileInfo fileInfo = new FileInfo(0);
        pathInfo.setFileInfo(fileInfo);
        connection.set(RfsUnderFileSystem.METADATA + path, gson.toJson(pathInfo));
        String parentPath = getParentPath(path);
        if (parentPath == null || parentPath == "") {
            return;
        } else {
            mkParentDirsRecv(parentPath);
            connection.sadd(RfsUnderFileSystem.INDEX + parentPath, path);
        }
    }

    // checkpointRoot/state/3/199
    private String getParentPath(String path) {
        if (!path.contains("/")) {
            return null;
        }
        String parentPath = path.substring(0,path.lastIndexOf('/'));
        return parentPath;
    }

    public FileStatus getFileStatus(String path) throws FileNotFoundException {
        MfsFileSystem.LOG.error("getFileStatus()方法执行 path="+path);
        path = trimPath(path);
        if (!connection.exists(RfsUnderFileSystem.METADATA + path)) {
            throw new FileNotFoundException();
        }
        String pathInfoJson = connection.get(RfsUnderFileSystem.METADATA + path);
        PathInfo pathInfo = gson.fromJson(pathInfoJson, PathInfo.class);
        if (pathInfo.isDirectory()) {
            return new FileStatus();
        } else if (pathInfo.getFileInfo().isRenamed()) {
            return new FileStatus(pathInfo.getFileInfo().getContentLength(), false,
                    0, 2048, pathInfo.getLastModified(),
                    new Path(this.protocol + path));
        } else {
            return null;
        }
    }

    private String trimPath(String path) {
        int start = path.indexOf(rootPath);
        return path.substring(start);
    }
    




}
