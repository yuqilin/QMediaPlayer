package life.knowledge4.videotrimmer.utils;

/**
 * Created by liwenfeng on 17/4/30.
 */

public class MediaInfo {

    public static final String[] MEDIA_AUDIO_FORMAT = {
            "mp3",
            "aac",
            "mp4"
    };

    public static final String[] MEDIA_AAC_BITS = {
            "copy",
            "128k",
            "192k",
            "256k",
            "320k",
            "130k",
            "190k",
            "245k"
    };

    public static final String[] MEDIA_MP3_BITS = {
            "128k",
            "192k",
            "256k",
            "320k",
            "130k",
            "190k",
            "245k"
    };

    public static final String[] MEDIA_MP4_BITS = {
            "copy"
    };

    public  static  String getAACComment(int i) {
        if(i == 0) {
            return "copy (32kb/s)";
        } else  if (i <= 4) {
            return MediaInfo.MEDIA_AAC_BITS[i] + " " + "CBR";
        } else {
            return MediaInfo.MEDIA_AAC_BITS[i] + " " + "VBR(slow)";
        }
    }

    public  static  String getMp3Comment(int i) {
        if (i <= 3) {
            return MediaInfo.MEDIA_MP3_BITS[i] + " " + "CBR";
        } else {
            return MediaInfo.MEDIA_MP3_BITS[i] + " " + "VBR(slow)";
        }
    }

    public static String getVideoCommnet(int i ) {
        return "copy";
    }

    public static String getBits(String type, int position) {
        if(type.equals("aac")) {
            return MEDIA_AAC_BITS[position];
        } else if(type.equals("mp3")) {
            return  MEDIA_MP3_BITS[position];
        } else {
            return  MEDIA_MP4_BITS[position];
        }
    }

    public static int getVbr(boolean aac, int position) {
        int vbr = 0;

        if (aac) {
            switch (position) {
                case 5:
                    vbr = 4;
                    break;
                case 6:
                    vbr = 5;
                    break;
                case  7:
                    vbr = 5;
                    break;
                default:
                    vbr = 0;
            }
        } else  {
            switch (position) {
                case 4:
                    vbr = 5;
                    break;
                case 5:
                    vbr = 2;
                    break;
                case 6:
                    vbr = 0;
                    break;
                default:
                    vbr = 0;
            }
        }

        return vbr;
    }
}
