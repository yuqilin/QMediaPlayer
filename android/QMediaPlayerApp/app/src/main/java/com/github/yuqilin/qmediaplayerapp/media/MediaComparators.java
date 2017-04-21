package com.github.yuqilin.qmediaplayerapp.media;

import java.util.Comparator;

/**
 * Created by yuqilin on 17/4/21.
 */

public class MediaComparators {

    private static int nullInsensitiveStringCompare(final String s1, final String s2) {
        if (s1 == null ^ s2 == null)
            return s1 == null ? -1 : 1;

        if (s1 == null && s2 == null)
            return 0;

        return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
    }

    // 名称字母升序
    public static final Comparator<MediaWrapper> byName = new Comparator<MediaWrapper>() {
        @Override
        public int compare(MediaWrapper m1, MediaWrapper m2) {
            return nullInsensitiveStringCompare(m1.getTitle(), m2.getTitle());
        };
    };

    // 时长由小及大
    public static final Comparator<MediaWrapper> byLength = new Comparator<MediaWrapper>() {
        @Override
        public int compare(MediaWrapper m1, MediaWrapper m2) {
            if(m1.getLength() > m2.getLength()) return 1;
            if(m1.getLength() < m2.getLength()) return -1;
            else return 0;
        };
    };

    // 拍摄日期由近及远
    public static final Comparator<MediaWrapper> byDate = new Comparator<MediaWrapper>() {
        @Override
        public int compare(MediaWrapper m1, MediaWrapper m2) {
            if(m1.getDateTaken() > m2.getDateTaken()) return -1;
            if(m1.getDateTaken() < m2.getDateTaken()) return 1;
            else return 0;
        };
    };

}
