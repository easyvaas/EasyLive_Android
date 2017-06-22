package com.easyvaas.sdk.demo.bean;

import java.io.Serializable;

/**
 * Created by liya on 16/8/3.
 */
public class PlayOption implements Serializable {
    private String videoPath;
    private boolean isLive;
    private boolean isWide;
    private boolean isFlv;
    private int playSupport;
    private String lid;
    private String fid;

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public boolean isWide() {
        return isWide;
    }

    public void setIsWide(boolean isWide) {
        this.isWide = isWide;
    }

    public boolean isFlv() {
        return isFlv;
    }

    public void setIsFlv(boolean isFlv) {
        this.isFlv = isFlv;
    }

    public int getPlaySupport() {
        return playSupport;
    }

    public void setPlaySupport(int playSupport) {
        this.playSupport = playSupport;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }
}
