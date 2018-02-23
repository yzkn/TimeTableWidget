package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static jp.gr.java_conf.ya.timetablewidget.AsyncDlTask.buildQueryString;

public class OdptUtil {
    private static final int timetableItemCount = 1;
    private static final int startHourOfDay = 4;
    private static final int commonMarginMinute = 10;
    private static SharedPreferences pref_app;
    public static final String BASE_URI = "https://api-tokyochallenge.odpt.org/api/v4/";
    public static final String HEADER = "{\"data\" : ";
    public static final String FOOTER = "}";
    public static final String PREF_CURRENT_LAT = "PREF_CURRENT_LAT";
    public static final String PREF_CURRENT_LON = "PREF_CURRENT_LON";
    private static final String PREF_PREFIX_KEY_ODPT = "odpt_";

    public static boolean containsKeyOdptPref(Context context, String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            return pref_app.contains(PREF_PREFIX_KEY_ODPT + key);
        } catch (Exception e) {
            return false;
        }
    }

    public static String loadOdptPref(Context context, String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        String value;
        try {
            value = pref_app.getString(PREF_PREFIX_KEY_ODPT + key, "");
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    public static void saveOdptPref(Context context, String key, String value) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            SharedPreferences.Editor editor = pref_app.edit();
            editor.putString(PREF_PREFIX_KEY_ODPT + key, value);
            editor.apply();
        } catch (Exception e) {
        }
    }

    public static String getTitle(Context context, final String key) {
        // 問い合わせ不要
        // if (containsKeyOdptPref(context, key)) {
        //    return loadOdptPref(context, key); // ローカルにキャッシュがある場合
        //} else
        if (key.equals("True")) {
            return "*"; // 終電
        } else if (key.startsWith("odpt.Calendar:")) {
            if (key.equals("odpt.Calendar:Weekday"))
                return "平日";
            else if (key.equals("odpt.Calendar:Holiday"))
                return "土休日";
        } else if (key.startsWith("odpt.Operator:")) {
            if (key.equals("odpt.Operator:JR-East"))
                return "JR東日本";
            else if (key.equals("odpt.Operator:Keikyu"))
                return "京浜急行";
            else if (key.equals("odpt.Operator:Keio"))
                return "京王電鉄";
            else if (key.equals("odpt.Operator:Keisei"))
                return "京成電鉄";
            else if (key.equals("odpt.Operator:Odakyu"))
                return "小田急";
            else if (key.equals("odpt.Operator:Seibu"))
                return "西武鉄道";
            else if (key.equals("odpt.Operator:TWR"))
                return "東京臨海高速鉄道";
            else if (key.equals("odpt.Operator:Tobu"))
                return "東武鉄道";
            else if (key.equals("odpt.Operator:Toei"))
                return "東京都交通局";
            else if (key.equals("odpt.Operator:TokyoMetro"))
                return "東京メトロ";
            else if (key.equals("odpt.Operator:Tokyu"))
                return "東急電鉄";
            else if (key.equals("odpt.Operator:Yurikamome"))
                return "ゆりかもめ";
            else
                return "";
        } else if (key.startsWith("odpt.TrainType:")) {
            return key.split("[.]")[key.split("[.]").length - 1];
        }

        // return key.split("[.]")[key.split("[.]").length-1];

        // odpt問い合わせ
        String title = "";
        Map<String, String> querysSameas = new HashMap<String, String>();
        URL url = null;
        try {
            if (key.startsWith("odpt.Railway:")) {
                querysSameas.put("owl:sameAs", key);
                url = new URL(buildQueryString(BASE_URI + "odpt:Railway", querysSameas));
            } else if (key.startsWith("odpt.Station:")) {
                querysSameas.put("owl:sameAs", key);
                url = new URL(buildQueryString(BASE_URI + "odpt:Station", querysSameas));
            } else if (key.startsWith("odpt.RailDirection:")) {
                querysSameas.put("owl:sameAs", key.replace("odpt.RailDirection", "odpt.Station"));
                url = new URL(buildQueryString(BASE_URI + "odpt:Station", querysSameas));
            }
            if (url != null) {
                String result = AsyncDlTask.downloadText(url, "UTF-8");
                Log.v("TTW", "getTitle result:" + result);
                JSONObject json = new JSONObject(HEADER + result + FOOTER);
                JSONObject dataObject = json.getJSONArray("data").getJSONObject(0);
                title = dataObject.getString("dc:title");
            }

            // キャッシュに保存
            saveOdptPref(context, key, title);
        } catch (MalformedURLException e) {
        } catch (JSONException e) {
        }

        return title;
    }

    public static Calendar getT0day() {
        return getT0day(0);
    }

    public static Calendar getT0day(int altMargin) {
        final Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, commonMarginMinute + altMargin);
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
                            // TODO: 現在地と駅との距離も取得
                            Location currentLocation = GeoUtil.createLocation( loadOdptPref(context, PREF_CURRENT_LAT), loadOdptPref(context, PREF_CURRENT_LON));
                            Location stationLocation = GeoUtil.createLocation( dataObject.getString("lat"), dataObject.getString("lon"));
                            int marginMinute = GeoUtil.getMinutes( currentLocation.distanceTo(stationLocation));

                            String uriStation = dataObject.getString("owl:sameAs");
                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                            querysAcquireStation.put("odpt:station", uriStation);
                            (new OdptUtil()).acquireStationTimetable(context, querysAcquireStation, marginMinute);
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
                    Log.v("TTW", "acquireStation: " + result[0]);

                    try {
                        JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            Location currentLocation = GeoUtil.createLocation( loadOdptPref(context, PREF_CURRENT_LAT), loadOdptPref(context, PREF_CURRENT_LON));
                            Location stationLocation = GeoUtil.createLocation( dataObject.getString("geo:lat"), dataObject.getString("geo:long"));
                            int marginMinute = GeoUtil.getMinutes( currentLocation.distanceTo(stationLocation));

                            String uriStation = dataObject.getString("owl:sameAs");
                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                            querysAcquireStation.put("odpt:station", uriStation);
                            (new OdptUtil()).acquireStationTimetable(context, querysAcquireStation, marginMinute);
                        }
                    } catch (JSONException e) {
                    }
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

    private TreeMap<Integer, String> stationTimetableObjectArrayToTreeMap(final Context context, JSONArray stationTimetableObjectArray, int marginMinute) {
        TreeMap<Integer, String> table = new TreeMap<>();
        for (int j = 0; j < stationTimetableObjectArray.length(); j++) {
            try {
                JSONObject stationTimetableObject = stationTimetableObjectArray.getJSONObject(j);
                String departureTime = stationTimetableObject.getString("odpt:departureTime"); // 時刻

                String destinationStation; // 目的地
                try {
                    if (stationTimetableObject.has("odpt:destinationStationTitle"))
                        destinationStation = new String(stationTimetableObject.getString("odpt:destinationStationTitle").getBytes("Shift_JIS"), "UTF-8");
                    else
                        destinationStation = getTitle(context, stationTimetableObject.getString("odpt:destinationStation"));
                } catch (Exception e) {
                    destinationStation = getTitle(context, stationTimetableObject.getString("odpt:destinationStation"));
                }

                String isLast;
                try {
                    isLast = getTitle(context, stationTimetableObject.getString("odpt:isLast")); // 終電
                } catch (JSONException e) {
                    isLast = "";
                }

                String trainType; // 種別
                try {
                    if (stationTimetableObject.has("odpt:trainTypeTitle"))
                        trainType = new String(stationTimetableObject.getString("odpt:trainTypeTitle").getBytes("Shift_JIS"), "UTF-8");
                    else
                        trainType = getTitle(context, stationTimetableObject.getString("odpt:trainType"));
                } catch (Exception e) {
                    trainType = getTitle(context, stationTimetableObject.getString("odpt:trainType"));
                }

                // String note = departureTime + " " + trainType.split("[.]")[trainType.split("[.]").length-1] + " " + destinationStation.split("[.]")[destinationStation.split("[.]").length-1] + (isLast.equals("") ? "" : " Last");
                String note = departureTime + " " + trainType + " " + destinationStation + "駅行" + isLast;

                // 24時以降対策
                int time28 = ((Integer.parseInt(departureTime.substring(0, 2)) < startHourOfDay) ? 2400 : 0) + Integer.parseInt(departureTime.replace(":", ""));
                int timeT0day = (((getT0day(marginMinute).get(Calendar.HOUR_OF_DAY) < startHourOfDay) ? 2400 : 0) + getT0day(marginMinute).get(Calendar.HOUR_OF_DAY)) * 100 + getT0day(marginMinute).get(Calendar.MINUTE);

                // 現在時刻よりも後のものだけ格納
                if (time28 > timeT0day) {
                    table.put(time28, note);
                    // Log.v("TTW", "28:"+ time28 +" t0day:"+ timeT0day +" "+ departureTime + " " + note);
                }
            } catch (Exception e) {
            }
        }
        return table;
    }

    public void acquireStationTimetable(final Context context, Map<String, String> querys, final int marginMinute) {
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

                public void onPostExecute(final String[] result) {
                    Log.v("TTW", "acquireStationTimetable: " + result[0]);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            StringBuilder sb = new StringBuilder();

                            // Date dateAdd = getT0day(marginMinute).getTime();
                            final SimpleDateFormat df = new SimpleDateFormat("HH:mm");

                            try {
                                Date date0400 = df.parse("04:00");

                                JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                                JSONArray dataArray = json.getJSONArray("data");

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject dataObject = dataArray.getJSONObject(i);


                                    String railway;
                                    try {
                                        if (dataObject.has("odpt:railwayTitle"))
                                            railway = new String(dataObject.getString("odpt:railwayTitle").getBytes("Shift_JIS"), "UTF-8");
                                        else
                                            railway = getTitle(context, dataObject.getString("odpt:railway"));
                                    } catch (Exception e) {
                                        railway = getTitle(context, dataObject.getString("odpt:railway"));
                                    }

                                    String station;
                                    try {
                                        if (dataObject.has("odpt:stationTitle"))
                                            station = new String(dataObject.getString("odpt:stationTitle").getBytes("Shift_JIS"), "UTF-8");
                                        else
                                            station = getTitle(context, dataObject.getString("odpt:station"));
                                    } catch (Exception e) {
                                        station = getTitle(context, dataObject.getString("odpt:station"));
                                    }

                                    String calendar = dataObject.getString("odpt:calendar");
                                    String operator = dataObject.getString("odpt:operator");

                                    String railDirection;
                                    try {
                                        if (dataObject.has("odpt:railDirectionTitle"))
                                            railDirection = new String(dataObject.getString("odpt:railDirectionTitle").getBytes("Shift_JIS"), "UTF-8");
                                        else
                                            railDirection = getTitle(context, dataObject.getString("odpt:railDirection"));
                                    } catch (Exception e) {
                                        railDirection = getTitle(context, dataObject.getString("odpt:railDirection"));
                                    }

                                    String note = station + "駅 " + railway + "線";
                                    Log.v("TTW", "note: " + note + " " + railDirection);

                                    // String stationTimetableObject_ = dataObject.getString("odpt:stationTimetableObject");
                                    JSONArray stationTimetableObjectArray = dataObject.getJSONArray("odpt:stationTimetableObject");

                                    TreeMap<Integer, String> table = stationTimetableObjectArrayToTreeMap(context, stationTimetableObjectArray, marginMinute);

                                    int j = 0;
                                    for (Map.Entry<Integer, String> e : table.entrySet()) {
                                        if (j < timetableItemCount) {
                                            int key = e.getKey();
                                            String val = e.getValue();
                                            sb.append(note + ": " + val).append("\n");
                                            j++;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                            }

                            Log.v("TTW", "sb: " + sb.toString());

                            TimeTableWidget.updateAppWidget(context, sb.toString());
                        }
                    }).start();
                }
            });
            asyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }
}