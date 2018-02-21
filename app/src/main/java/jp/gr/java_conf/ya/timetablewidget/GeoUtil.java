package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

public class GeoUtil {
    public static final int getMinutes(final String meters){
        return getMinutes(Integer.parseInt(meters.replace("m", "")));
    }

    public static final int getMinutes(final int meters){
        return meters / 60;
    }
}
