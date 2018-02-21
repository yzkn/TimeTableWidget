package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static jp.gr.java_conf.ya.timetablewidget.AsyncDlTask.buildQueryString;

public class HeartRailsUtil {
    public static final String BASE_URI = "http://express.heartrails.com/api/";

    public void acquirePlaces(final Context context, Map<String, String> querys) {
        String endPoint = "json";

        try {
            final URL url = new URL(buildQueryString(BASE_URI + endPoint, querys));
            AsyncDlTask aAsyncDlTask = new AsyncDlTask(new AsyncDlTask.AsyncCallback() {
                public void onPreExecute() {
                }

                public void onProgressUpdate(int progress) {
                }

                public void onCancelled() {
                }

                public void onPostExecute(String[] result) {
                    Log.v("TTW", result[0]);

                    try {
                        JSONObject json = new JSONObject(result[0]);
                        JSONArray dataArray = json.getJSONObject("response").getJSONArray("station");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            String uriStation = dataObject.getString("name");
                            // Log.v("TTW", "uriStation: "+uriStation);

                            // TODO: 現在地と駅との距離も取得
                            String uriDistance = dataObject.getString("distance");
                            // GeoUtil.getMinutes()
                            // Log.v("TTW", "uriDistance: "+uriDistance);

                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                            querysAcquireStation.put("dc:title", uriStation);
                            (new OdptUtil()).acquireStation(context, querysAcquireStation);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }
}
