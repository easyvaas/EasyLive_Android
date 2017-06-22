package com.easyvaas.sdk.demo.bean;

import java.io.Serializable;

/**
 * Created by liya on 16/7/29.
 */
public class LiveOption implements Serializable {
    private int maxVideoBitrate;
    private int initVideoBitrate;
    private int videoResolution;
    private int audioCodec;
    private int audioBitrate;
    private String lid;
    private String key;
    private String uri;
    private boolean bgmMix;
    private boolean useFrontCamera;
    private boolean isBeautyOn;
    private boolean isPortrait;

    public int getMaxVideoBitrate() {
        return maxVideoBitrate;
    }

    public void setMaxVideoBitrate(int maxVideoBitrate) {
        this.maxVideoBitrate = maxVideoBitrate;
    }

    public int getInitVideoBitrate() {
        return initVideoBitrate;
    }

    public void setInitVideoBitrate(int initVideoBitrate) {
        this.initVideoBitrate = initVideoBitrate;
    }

    public int getVideoResolution() {
        return videoResolution;
    }

    public void setVideoResolution(int videoResolution) {
        this.videoResolution = videoResolution;
    }

    public int getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(int audioCodec) {
        this.audioCodec = audioCodec;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isBgmMix() {
        return bgmMix;
    }

    public void setBgmMix(boolean bgmMix) {
        this.bgmMix = bgmMix;
    }

    public boolean isUseFrontCamera() {
        return useFrontCamera;
    }

    public void setUseFrontCamera(boolean useFrontCamera) {
        this.useFrontCamera = useFrontCamera;
    }

    public boolean isBeautyOn() {
        return isBeautyOn;
    }

    public void setIsBeautyOn(boolean isBeautyOn) {
        this.isBeautyOn = isBeautyOn;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }
}
