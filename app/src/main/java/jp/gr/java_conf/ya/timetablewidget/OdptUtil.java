package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static jp.gr.java_conf.ya.timetablewidget.AsyncDlTask.buildQueryString;

public class OdptUtil {
    String BASE_URI = "https://api-tokyochallenge.odpt.org/api/v4/";
    String HEADER = "{\"data\" : ";
    String FOOTER = "}";

    private static final int startHourOfDay = 4;
    private static final int marginMinute = 10;

    public void acquirePlaces(Map<String, String> querys) {
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
                            (new OdptUtil()).acquireStationTimetable(querysAcquireStation);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

    private Calendar getDateWithMargin(){
        final Date now = new Date();
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(now);
        calendarNow.add(Calendar.MINUTE, marginMinute);
        return calendarNow;
    }

    private TreeMap<Integer, Map<String, String>> stationTimetableObjectArrayToTreeMap(JSONArray stationTimetableObjectArray){
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
                String note =  departureTime + " " + trainType.substring(0, 1) + " " + destinationStation + (isLast.equals("") ? "" : " Last");

                // 24時以降対策
                int time28 =   ((Integer.parseInt(departureTime.substring(0,2))<startHourOfDay)?2400:0) + Integer.parseInt(departureTime.replace(":", ""));
                if(time28 > getDateWithMargin().get(Calendar.HOUR_OF_DAY)*100+getDateWithMargin().get(Calendar.MINUTE)){
                    Map<String, String> item = new TreeMap<>();
                    item.put( departureTime, note);
                    table.put( time28, item);
                }
                Log.v("TTW", departureTime + " " +note);
            } catch (Exception e) {
            }
        }
        return table;
    }

    public void acquireStationTimetable(Map<String, String> querys) {
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
                    Map<String, String> resultMap = new HashMap<String, String>();

                    Date dateAdd = getDateWithMargin().getTime();
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

                            for (Map.Entry<Integer, Map<String, String>> e : table.entrySet()) {
                                int key1 = e.getKey();
                                Map<String, String> val1 = e.getValue();
                                for (Map.Entry<String, String> e2 : val1.entrySet()) {
                                    String key2 = e2.getKey();
                                    String val2 = e2.getValue();

                                    Log.v("TTW", "key1: " + Integer.toString(key1)+ " key2: "+ key2 +  " val2: "+val2);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
            asyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }
}