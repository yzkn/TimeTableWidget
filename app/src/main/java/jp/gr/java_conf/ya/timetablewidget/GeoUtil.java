package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.location.Location;

public class GeoUtil {
    public static final Location createLocation(String lat, String lon) {
        Location location = new Location("");

        double latD;
        double lonD;
        try {
            latD = Double.parseDouble(lat);
            lonD = Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            return null;
        }

        location.setLatitude(latD);
        location.setLongitude(lonD);

        return location;
    }

    public static final int getMinutes(final String meters) {
        return getMinutes(Float.parseFloat(meters.replace("m", "")));
    }

    public static final int getMinutes(final float meters) {
        return (int) Math.ceil(meters / 60);
    }
}