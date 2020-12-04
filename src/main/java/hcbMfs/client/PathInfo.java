package hcbMfs.client;

public class PathInfo {


    private boolean isDirectory;
    private String owner = "hcb";
    private String group = "staff";
    private short mode = (short)493;
    private long lastModified;
    private FileInfo fileInfo;

    public PathInfo() {

    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public short getMode() {
        return mode;
    }

    public void setMode(short mode) {
        this.mode = mode;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }



    public PathInfo(boolean isDirectory, String owner,
                    String group, short mode,
                    long lastModified, FileInfo fileInfo) {
        this.isDirectory = isDirectory;
        this.owner = owner;
        this.group = group;
        this.mode = mode;
        this.lastModified = lastModified;
        this.fileInfo = fileInfo;
    }

    public PathInfo(boolean isDirectory, long lastModified) {
        this.isDirectory = isDirectory;
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "PathInfo{" +
                "isDirectory=" + isDirectory +
                ", owner='" + owner + '\'' +
                ", group='" + group + '\'' +
                ", mode=" + mode +
                ", lastModified=" + lastModified +
                ", fileInfo=" + fileInfo +
                '}';
    }


}
