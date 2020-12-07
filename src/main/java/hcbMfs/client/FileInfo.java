package hcbMfs.client;

public class FileInfo {

    private String contentHash;
    private long contentLength;
    private boolean renamed;

    public FileInfo(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public void setRenamed(boolean renamed) {
        this.renamed = renamed;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "contentHash='" + contentHash + '\'' +
                ", contentLength=" + contentLength +
                ", renamed=" + renamed +
                '}';
    }
}
