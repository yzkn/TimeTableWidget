package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static jp.gr.java_conf.ya.timetablewidget.AsyncDlTask.buildQueryString;

public class OdptUtil {
    private static final int timetableItemCount = 5;
    private static final int startHourOfDay = 4;
    private static final int marginMinute = 10;
    public static final String BASE_URI = "https://api-tokyochallenge.odpt.org/api/v4/";
    String HEADER = "{\"data\" : ";
    String FOOTER = "}";

    public static Calendar getT0day() {
        return getT0day(0);
    }

    public static Calendar getT0day(int altMargin) {
        final Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, marginMinute + altMargin);
        return calendar;
    }

    public static String getDateString(Date date) {
        java.sql.Date t0daySql = new java.sql.Date(date.getTime());
        return t0daySql.toString();
    }

    public void acquirePlaces(final Context context, Map<String, String> querys) {
        String endPoint = "places";

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
                        JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            String uriStation = dataObject.getString("owl:sameAs");

                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                            querysAcquireStation.put("owl:sameAs", uriStation);

                            // TODO: 現在地と駅との距離も取得

                            (new OdptUtil()).acquireStationTimetable(context, querysAcquireStation);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

    public void acquireStation(final Context context, Map<String, String> querys) {
        String endPoint = "odpt:Station";

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
                        JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        // for (int i = 0; i < dataArray.length(); i++) {
                        int i = 0;
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            String uriStation = dataObject.getString("owl:sameAs");
                            Log.v("TTW", "uriStation: "+uriStation);

                            /*
                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                            querysAcquireStation.put("owl:sameAs", uriStation);
                            (new OdptUtil()).acquireStationTimetable(context, querysAcquireStation);
                            */
                        // }
                    } catch (JSONException e) {
                    }
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

    private TreeMap<Integer, Map<String, String>> stationTimetableObjectArrayToTreeMap(JSONArray stationTimetableObjectArray) {
        TreeMap<Integer, Map<String, String>> table = new TreeMap<>();
        for (int j = 0; j < stationTimetableObjectArray.length(); j++) {
            try {
                JSONObject stationTimetableObject = stationTimetableObjectArray.getJSONObject(j);
                String departureTime = stationTimetableObject.getString("odpt:departureTime"); // 時刻
                String destinationStation = stationTimetableObject.getString("odpt:destinationStation"); // 目的地
                String isLast;
                try {
                    isLast = stationTimetableObject.getString("odpt:isLast"); // 終電
                } catch (JSONException e) {
                    isLast = "";
                }
                String trainType = stationTimetableObject.getString("odpt:trainType"); // 種別
                String note = departureTime + " " + trainType.split("[.]")[trainType.split("[.]").length-1] + " " + destinationStation.split("[.]")[destinationStation.split("[.]").length-1] + (isLast.equals("") ? "" : " Last");

                // 24時以降対策
                int time28 = ((Integer.parseInt(departureTime.substring(0, 2)) < startHourOfDay) ? 2400 : 0) + Integer.parseInt(departureTime.replace(":", ""));
                int timeT0day = (((getT0day().get(Calendar.HOUR_OF_DAY) < startHourOfDay) ? 2400 : 0) + getT0day().get(Calendar.HOUR_OF_DAY)) * 100 + getT0day().get(Calendar.MINUTE);

                // 現在時刻よりも後のものだけ格納
                if (time28 > timeT0day) {
                    Map<String, String> item = new TreeMap<>();
                    item.put(departureTime, note);
                    table.put(time28, item);
                    // Log.v("TTW", "28:"+ time28 +" t0day:"+ timeT0day +" "+ departureTime + " " + note);
                }
            } catch (Exception e) {
            }
        }
        return table;
    }

    public void acquireStationTimetable(final Context context, Map<String, String> querys) {
        String endPoint = "odpt:StationTimetable";

        try {
            final URL url = new URL(buildQueryString(BASE_URI + endPoint,
                    querys));
            AsyncDlTask asyncDlTask = new AsyncDlTask(new AsyncDlTask.AsyncCallback() {
                public void onPreExecute() {
                }

                public void onProgressUpdate(int progress) {
                }

                public void onCancelled() {
                }

                public void onPostExecute(String[] result) {
                    Log.v("TTW", result[0]);

                    StringBuilder sb = new StringBuilder();

                    Date dateAdd = getT0day().getTime();
                    final SimpleDateFormat df = new SimpleDateFormat("HH:mm");

                    try {
                        Date date0400 = df.parse("04:00");

                        JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);

                            String railway = dataObject.getString("odpt:railway");
                            String station = dataObject.getString("odpt:station");
                            String calendar = dataObject.getString("odpt:calendar");
                            String operator = dataObject.getString("odpt:operator");
                            String railDirection = dataObject.getString("odpt:railDirection");
                            String stationTimetableObject_ = dataObject.getString("odpt:stationTimetableObject");
                            JSONArray stationTimetableObjectArray = dataObject.getJSONArray("odpt:stationTimetableObject");

                            TreeMap<Integer, Map<String, String>> table = stationTimetableObjectArrayToTreeMap(stationTimetableObjectArray);

                            int j = 0;
                            for (Map.Entry<Integer, Map<String, String>> e : table.entrySet()) {
                                if(j < timetableItemCount) {
                                    int key1 = e.getKey();
                                    Map<String, String> val1 = e.getValue();

                                    for (Map.Entry<String, String> e2 : val1.entrySet()) {
                                        String key2 = e2.getKey();
                                        String val2 = e2.getValue();

                                        sb.append(key2 + ": " + val2).append("\n");
                                    }

                                    j++;
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                    Log.v("TTW", sb.toString());

                    TimeTableWidget.updateAppWidget(context, sb.toString());
                }
            });
            asyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }
}