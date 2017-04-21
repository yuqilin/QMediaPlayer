package com.github.yuqilin.qmediaplayerapp.media;

import java.util.Date;

/**
 * Created by yuqilin on 17/2/11.
 */

public class MediaWrapper {
    private long videoId;
    private String filePath;
    private String mimeType;
//    public String thumbPath;
    private String title;
    private long dateTaken;

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    private long length;
    private long fileSize;

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

}
