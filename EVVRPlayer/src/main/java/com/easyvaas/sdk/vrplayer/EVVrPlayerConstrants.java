package com.easyvaas.sdk.vrplayer;

import com.player.data.panoramas.BackMusic;
import com.player.data.panoramas.Hotspot;
import com.player.data.panoramas.Image;
import com.player.data.panoramas.ImageViewData;
import com.player.data.panoramas.PanoramaData;
import com.player.data.panoramas.Preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author weizibo
 * Date 17/10/10
 * Version 1.0
 */

public class EVVrPlayerConstrants {
    public static final int VIEWMODE_DEF = 1;
    public static final int VIEWMODE_FISHEYE = 2;
    public static final int VIEWMODE_VR_HORIZONTAL = 3;
    public static final int VIEWMODE_VR_VERTICAL = 4;
    /*public static final int VIEWMODE_PLANE = 5;
    public static final int VIEWMODE_LITTLEPLANET = 6;*/
    public static final int VIEWMODEL_SPHERE = 7;
    public static final int VIEWMODEL_LINEFLAT = 8;
    /*public static final int VIEWMODEL_WIDE_ANGLE = 9;
    public static final int VIEWMODE_FRONTBACK = 10;*/

    public static class EVVrRamaData extends PanoramaData {
        public String name;
        public String title;
        public String thumbUrl;
        public Preview preview;
        public ImageViewData imageViewData;
        public Image image;
        public BackMusic backmp3;
        public List<Hotspot> hotspotList = new ArrayList();

        public EVVrRamaData(PanoramaData panoramaData) {
            name = panoramaData.name;
            title = panoramaData.title;
            thumbUrl = panoramaData.title;
            preview = panoramaData.preview;
            imageViewData = panoramaData.imageViewData;
            image = panoramaData.image;
            backmp3 = panoramaData.backmp3;
            hotspotList = panoramaData.hotspotList;
        }

        public String toString() {
            String var1 = "\n scene :　\n name = \n title = " + this.title + "\n thumbUrl = " + this.thumbUrl + "\n preview = " + this.preview == null ? "null" : (this.preview.toString() + "\n imageViewData = " + this.imageViewData == null ? "null" : (this.imageViewData.toString() + "\n image = " + this.image == null ? "null" : (this.image.toString() + "\n backgroundmusic = " + this.backmp3 == null ? "null" : this.backmp3.toString() + "\n hotsport = " + this.hotspotList.toString())));
            StringBuilder var2 = (new StringBuilder(var1)).append("\n hotsport = ").append(this.hotspotList.isEmpty() ? "null" : "");
            Iterator var3 = this.hotspotList.iterator();

            while (var3.hasNext()) {
                Hotspot var4 = (Hotspot) var3.next();
                var2.append(var4.toString());
            }

            return var2.toString();
        }
    }

    public static final int ERRORCODE_PLAY_SUCCESS = 1001;
    public static final int ERRORCODE_PLAY_MANAGER_DATA_IS_EMPTY = 1002;
    public static final int ERRORCODE_SETTING_DATA_IS_EMPTY = 1003;
    public static final int ERRORCODE_RAMALIST_IS_EMPTY = 1004;
    public static final int ERRORCODE_PLAY_URL_IS_EMPTY = 1005;
    public static final int ERRORCODE_IMAGE_LOAD_ERROR = 1006;
    public static final int ERRORCODE_LACK_CALIBRATION = 1007;

    public static final int VIDEO_STATUS_PLAYING = 2001;
    public static final int VIDEO_STATUS_PAUSE = 2002;
    public static final int VIDEO_STATUS_STOP = 2003;
    public static final int VIDEO_STATUS_FINISH = 2004;
    public static final int VIDEO_STATUS_BUFFER_EMPTY = 2005;
    public static final int VIDEO_STATUS_HW_TO_AVCODEC = 2006;
    public static final int VIDEO_STATUS_PREPARED = 2007;
    public static final int VIDEO_STATUS_UNPREPARED = 2008;

    public static final int ERRORSTATUS_NERWORK = 3001;
    public static final int ERRORSTATUS_FORMAT = 3002;

}
