package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static jp.gr.java_conf.ya.timetablewidget.AsyncDlTask.buildQueryString;

public class OdptUtil {
    String BASE_URI = "https://api-tokyochallenge.odpt.org/api/v4/";
    String HEADER = "{\"data\" : ";
    String FOOTER = "}";


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


    public void acquireStationTimetable(Map<String, String> querys) {
        String endPoint = "odpt:StationTimetable";

        try {
            final URL url = new URL(buildQueryString(BASE_URI + endPoint,
                    querys));
            AsyncDlTask aAsyncDlTask = new AsyncDlTask(new AsyncDlTask.AsyncCallback() {
                public void onPreExecute() {
                }

                public void onProgressUpdate(int progress) {
                }

                public void onCancelled() {
                }

                public void onPostExecute(String[] result) {
                    Log.v("TTW", result[0]);
                    Map<String, String> resultMap = new HashMap<String, String>();

                    try {
                        JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);

                            String railway = dataObject.getString("odpt:railway");
                            // Log.v("TTW", "railway: "+railway);
                            String station = dataObject.getString("odpt:station");
                            // Log.v("TTW", "station: "+station);
                            String calendar = dataObject.getString("odpt:calendar");
                            // Log.v("TTW", "calendar: "+calendar);
                            String operator = dataObject.getString("odpt:operator");
                            // Log.v("TTW", "operator: "+operator);
                            String railDirection = dataObject.getString("odpt:railDirection");
                            String stationTimetableObject_ = dataObject.getString("odpt:stationTimetableObject");
                            // Log.v("TTW", "stationTimetableObject_: "+stationTimetableObject_.toString());
                            JSONArray stationTimetableObjectArray = dataObject.getJSONArray("odpt:stationTimetableObject");
                            for (int j = 0; j < stationTimetableObjectArray.length(); j++) {
                                JSONObject stationTimetableObject = stationTimetableObjectArray.getJSONObject(j);
                                String trainType = stationTimetableObject.getString("odpt:trainType"); // 種別
                                // Log.v("TTW", "trainType: "+trainType);
                                String departureTime = stationTimetableObject.getString("odpt:departureTime"); // 時刻
                                // Log.v("TTW", "departureTime: "+departureTime);
                                String destinationStation = stationTimetableObject.getString("odpt:destinationStation"); // 目的地
                                // Log.v("TTW", "destinationStation: "+destinationStation);
                                String isLast;
                                try {
                                    isLast = stationTimetableObject.getString("odpt:isLast"); // 終電
                                } catch (JSONException e) {
                                    isLast = "";
                                }
                                // Log.v("TTW", "isLast: "+isLast);

                                resultMap.put(departureTime, railDirection + " " + trainType.substring(0, 1) + " " + destinationStation + (isLast.equals("") ? "" : " Last"));

                                // Temp
                                Log.v("TTW", departureTime + " " + railDirection + " " + trainType.substring(0, 1) + " " + destinationStation + (isLast.equals("") ? "" : " Last"));
                            }
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