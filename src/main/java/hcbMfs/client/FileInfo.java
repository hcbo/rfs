package hcbMfs.client;

public class FileInfo {

    private String contentHash;
    private long contentLength;

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

    @Override
    public String toString() {
        return "FileInfo{" +
                "contentHash='" + contentHash + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}
