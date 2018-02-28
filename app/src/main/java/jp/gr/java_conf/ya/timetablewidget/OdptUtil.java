package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String BASE_URI = "https://api-tokyochallenge.odpt.org/api/v4/";
    public static final String HEADER = "{\"data\" : ";
    public static final String FOOTER = "}";
    public static final String PREF_CURRENT_LAT = "PREF_CURRENT_LAT";
    public static final String PREF_CURRENT_LON = "PREF_CURRENT_LON";
    private static final int timetableItemCount = 3;
    private static final int startHourOfDay = 4;
    private static final int commonMarginMinute = 10;

    public static String getDateString(Date date) {
        java.sql.Date t0daySql = new java.sql.Date(date.getTime());
        return t0daySql.toString();
    }

    public static String getTitle(Context context, final String key) {
        // 問い合わせ不要
        if (PrefUtil.containsKeyOdptPref(context, key)) {
            return PrefUtil.loadOdptPref(context, key); // ローカルにキャッシュがある場合
        } else if (key.equals("True")) {
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
        } else if (key.equals("odpt.RailDirection:Inbound")) {
            return "上り";
        } else if (key.equals("odpt.RailDirection:Outbound")) {
            return "下り";
        } else if (key.equals("odpt.RailDirection:InnerLoop")) {
            return "内回り";
        } else if (key.equals("odpt.RailDirection:OuterLoop")) {
            return "外回り";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Asakusa")) {
            return "浅草方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Shibuya")) {
            return "渋谷方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Honancho")) {
            return "方南町方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Ikebukuro")) {
            return "池袋方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Ogikubo")) {
            return "荻窪方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.NakaMeguro")) {
            return "中目黒方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.KitaSenju")) {
            return "北千住方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Nakano")) {
            return "中野方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.NishiFunabashi")) {
            return "西船橋方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Ayase")) {
            return "綾瀬方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.YoyogiUehara")) {
            return "代々木上原方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.ShinKiba")) {
            return "新木場方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Wakoshi")) {
            return "和光市方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Oshiage")) {
            return "押上方面";
            // } else if (key.equals("odpt.RailDirection:TokyoMetro.Shibuya")) {
            // return "渋谷方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.AkabaneIwabuchi")) {
            return "赤羽岩淵方面";
        } else if (key.equals("odpt.RailDirection:TokyoMetro.Meguro")) {
            return "目黒方面";
            // } else if (key.equals("odpt.RailDirection:TokyoMetro.Shibuya")) {
            // return "渋谷方面";
            // } else if (key.equals("odpt.RailDirection:TokyoMetro.Wakoshi")) {
            // return "和光市方面";

        } else if (key.equals("odpt.TrainType:")) {

            //

            if (key.equals("odpt.TrainType:JR-East.Local"))
                return "普通";
            else if (key.equals("odpt.TrainType:JR-East.Rapid"))
                return "快速";

            //

            if (key.equals("odpt.TrainType:Keikyu.AccessLimitedExpress"))
                return "アクセス特急";
            else if (key.equals("odpt.TrainType:Keikyu.AirportExpress"))
                return "エアポート急行";
            else if (key.equals("odpt.TrainType:Keikyu.AirportRapidLimitedExpress"))
                return "エアポート快特";
            else if (key.equals("odpt.TrainType:Keikyu.CommuterLimitedExpress"))
                return "通勤特急";
            else if (key.equals("odpt.TrainType:Keikyu.LimitedExpress"))
                return "特急";
            else if (key.equals("odpt.TrainType:Keikyu.Local"))
                return "普通";
            else if (key.equals("odpt.TrainType:Keikyu.MorningWing"))
                return "モーニング・ウィング号";
            else if (key.equals("odpt.TrainType:Keikyu.Rapid"))
                return "快速";
            else if (key.equals("odpt.TrainType:Keikyu.RapidLimitedExpress"))
                return "快特";
            else if (key.equals("odpt.TrainType:Keikyu.Wing"))
                return "京急ウィング号";

                //

            else if (key.equals("odpt.TrainType:Keio.Express"))
                return "急行";
            else if (key.equals("odpt.TrainType:Keio.LimitedExpress"))
                return "特急";
            else if (key.equals("odpt.TrainType:Keio.Local"))
                return "各駅停車";
            else if (key.equals("odpt.TrainType:Keio.Rapid"))
                return "快速";
            else if (key.equals("odpt.TrainType:Keio.SemiExpress"))
                return "区間急行";
            else if (key.equals("odpt.TrainType:Keio.SemiLimitedExpress"))
                return "準特急";

            //

            if (key.equals("odpt.TrainType:Odakyu.Local"))
                return "普通";

            //

            if (key.equals("odpt.TrainType:Seibu.Local"))
                return "普通";

            //

            if (key.equals("odpt.TrainType:TWR.Local"))
                return "普通";

            //

            if (key.equals("odpt.TrainType:Tobu.Express"))
                return "急行";
            else if (key.equals("odpt.TrainType:Tobu.F-Liner"))
                return "Fライナー";
            else if (key.equals("odpt.TrainType:Tobu.Local"))
                return "普通";
            else if (key.equals("odpt.TrainType:Tobu.Rapid"))
                return "快速";
            else if (key.equals("odpt.TrainType:Tobu.RapidExpress"))
                return "快速急行";
            else if (key.equals("odpt.TrainType:Tobu.SemiExpress"))
                return "準急";
            else if (key.equals("odpt.TrainType:Tobu.TJ-Liner"))
                return "TJライナー";

            //

            if (key.equals("odpt.TrainType:Toei.AccessLimitedExpress"))
                return "アクセス特急";
            else if (key.equals("odpt.TrainType:Toei.AirportRapidLimitedExpress"))
                return "エアポート快特";
            else if (key.equals("odpt.TrainType:Toei.CommuterLimitedExpress"))
                return "通勤特急";
            else if (key.equals("odpt.TrainType:Toei.Express"))
                return "急行";
            else if (key.equals("odpt.TrainType:Toei.LimitedExpress"))
                return "特急";
            else if (key.equals("odpt.TrainType:Toei.Local"))
                return "各駅停車";
            else if (key.equals("odpt.TrainType:Toei.Rapid"))
                return "快速";
            else if (key.equals("odpt.TrainType:Toei.RapidLimitedExpress"))
                return "快特";


            //

            if (key.equals("odpt.TrainType:TokyoMetro.CommuterExpress"))
                return "通勤急行";
            else if (key.equals("odpt.TrainType:TokyoMetro.Express"))
                return "急行";
            else if (key.equals("odpt.TrainType:TokyoMetro.F-Liner"))
                return "Fライナー";
            else if (key.equals("odpt.TrainType:TokyoMetro.LimitedExpress"))
                return "特急ロマンスカー";
            else if (key.equals("odpt.TrainType:TokyoMetro.Local"))
                return "各駅停車";
            else if (key.equals("odpt.TrainType:TokyoMetro.Rapid"))
                return "快速";
            else if (key.equals("odpt.TrainType:TokyoMetro.S-TRAIN"))
                return "S-TRAIN";
            else if (key.equals("odpt.TrainType:TokyoMetro.SemiExpress"))
                return "準急";
            else if (key.equals("odpt.TrainType:TokyoMetro.TamaExpress"))
                return "多摩急行";

                //

            else if (key.equals("odpt.TrainType:Tokyu.Local"))
                return "各駅停車";

                //

            else if (key.equals("odpt.TrainType:Yurikamome.Local"))
                return "各駅停車";

            //

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
                return key.split("[.]")[key.split("[.]").length - 1];
            }
            if (url != null) {
                String result = AsyncDlTask.downloadText(url, "UTF-8");
                if (OdptKey.IS_DEBUG) Log.v("TTW", "getTitle result:" + result);
                JSONObject json = new JSONObject(HEADER + result + FOOTER);
                JSONObject dataObject = json.getJSONArray("data").getJSONObject(0);
                title = dataObject.getString("dc:title");
            }

            // キャッシュに保存
            PrefUtil.saveOdptPref(context, key, title);
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
                    if (OdptKey.IS_DEBUG) Log.v("TTW", result[0]);

                    if (!result[0].equals(""))
                        try {
                            JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                            final JSONArray dataArray = json.getJSONArray("data");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder sb = new StringBuilder();

                                    for (int i = 0; i < dataArray.length(); i++) {
                                        try {
                                            JSONObject dataObject = dataArray.getJSONObject(i);
                                            Location currentLocation = GeoUtil.createLocation(PrefUtil.loadOdptPref(context, PREF_CURRENT_LAT), PrefUtil.loadOdptPref(context, PREF_CURRENT_LON));
                                            Location stationLocation = GeoUtil.createLocation(dataObject.getString("lat"), dataObject.getString("lon"));
                                            int marginMinute = GeoUtil.getMinutes(currentLocation.distanceTo(stationLocation));

                                            String uriStation = dataObject.getString("owl:sameAs");
                                            Map<String, String> querysAcquireStation = new HashMap<String, String>();
                                            querysAcquireStation.put("odpt:station", uriStation);
                                            querysAcquireStation.put("odpt:calendar", PrefUtil.checkIfTodayIsHoliday(context));
                                            sb.append(acquireStationTimetable(context, querysAcquireStation, marginMinute));
                                        } catch (JSONException e) {
                                        }
                                    }

                                    if (OdptKey.IS_DEBUG) Log.v("TTW", "sb: " + sb.toString());
                                    if (!sb.toString().equals(""))
                                        TimeTableWidget.updateAppWidget(context, sb.toString());
                                }
                            }).start();
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
        if (OdptKey.IS_DEBUG)
            Log.v("TTW", "acquireStation " + buildQueryString(BASE_URI + endPoint, querys));

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
                    if (OdptKey.IS_DEBUG) Log.v("TTW", "acquireStation: " + result[0]);

                    if (!result[0].equals(""))
                        try {
                            JSONObject json = new JSONObject(HEADER + result[0] + FOOTER);
                            final JSONArray dataArray = json.getJSONArray("data");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder sb = new StringBuilder();

                                    for (int i = 0; i < dataArray.length(); i++) {
                                        try {
                                            JSONObject dataObject = dataArray.getJSONObject(i);
                                            Location currentLocation = GeoUtil.createLocation(PrefUtil.loadOdptPref(context, PREF_CURRENT_LAT), PrefUtil.loadOdptPref(context, PREF_CURRENT_LON));
                                            Location stationLocation = GeoUtil.createLocation(dataObject.getString("geo:lat"), dataObject.getString("geo:long"));

                                            if (currentLocation != null && stationLocation != null) {
                                                int marginMinute = GeoUtil.getMinutes(currentLocation.distanceTo(stationLocation));

                                                String uriStation = dataObject.getString("owl:sameAs");
                                                Map<String, String> querysAcquireStation = new HashMap<String, String>();
                                                querysAcquireStation.put("odpt:station", uriStation);
                                                querysAcquireStation.put("odpt:calendar", PrefUtil.checkIfTodayIsHoliday(context));
                                                sb.append(acquireStationTimetable(context, querysAcquireStation, marginMinute));
                                            }
                                        } catch (JSONException e) {
                                        }
                                    }

                                    if (OdptKey.IS_DEBUG) Log.v("TTW", "sb: " + sb.toString());
                                    if (!sb.toString().equals(""))
                                        TimeTableWidget.updateAppWidget(context, sb.toString());
                                }
                            }).start();
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
                        destinationStation = stationTimetableObject.getString("odpt:destinationStationTitle");
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

                String trainType = ""; // 種別
                try {
                    if (stationTimetableObject.has("odpt:trainName")) {
                        trainType = stationTimetableObject.getString("odpt:trainName");
                    } else {
                        try {
                            if (stationTimetableObject.has("odpt:trainTypeTitle"))
                                trainType = stationTimetableObject.getString("odpt:trainTypeTitle");
                            else
                                trainType = getTitle(context, stationTimetableObject.getString("odpt:trainType"));
                        } catch (Exception e) {
                            trainType = getTitle(context, stationTimetableObject.getString("odpt:trainType"));
                        }
                    }
                } catch (Exception e) {
                }

                // String note = departureTime + " " + trainType.split("[.]")[trainType.split("[.]").length-1] + " " + destinationStation.split("[.]")[destinationStation.split("[.]").length-1] + (isLast.equals("") ? "" : " Last");
                String note = departureTime + " " + trainType + " " + destinationStation + "駅行" + isLast;

                // 24時以降対策
                int time28 = ((Integer.parseInt(departureTime.substring(0, 2)) < startHourOfDay) ? 2400 : 0) + Integer.parseInt(departureTime.replace(":", ""));
                int timeT0day = (((getT0day(marginMinute).get(Calendar.HOUR_OF_DAY) < startHourOfDay) ? 2400 : 0) + getT0day(marginMinute).get(Calendar.HOUR_OF_DAY)) * 100 + getT0day(marginMinute).get(Calendar.MINUTE);

                // 現在時刻よりも後のものだけ格納
                if (time28 > timeT0day) {
                    table.put(time28, note);
                    if (OdptKey.IS_DEBUG)
                        Log.v("TTW", "28:" + time28 + " t0day:" + timeT0day + " " + departureTime + " " + note);
                }
            } catch (Exception e) {
            }
        }
        return table;
    }

    public String acquireStationTimetable(final Context context, Map<String, String> querys, final int marginMinute) {
        String endPoint = "odpt:StationTimetable";

        try {
            if (OdptKey.IS_DEBUG)
                Log.v("TTW", "acquireStationTimetable: " + buildQueryString(BASE_URI + endPoint, querys));
            final URL url = new URL(buildQueryString(BASE_URI + endPoint,
                    querys));
            // AsyncDlTask asyncDlTask = new AsyncDlTask(new AsyncDlTask.AsyncCallback() {
            //     public void onPreExecute() {
            //     }

            //     public void onProgressUpdate(int progress) {
            //     }

            //     public void onCancelled() {
            //     }

            //     public void onPostExecute(final String[] result) {

            // Sync
            String result = AsyncDlTask.downloadText(url, "UTF-8");
            if (OdptKey.IS_DEBUG) Log.v("TTW", "getTitle result:" + result);

            if (!result.equals(""))
                try {
                    // new Thread(new Runnable() {
                    //     @Override
                    //     public void run() {

                    StringBuilder sb = new StringBuilder();

                    // Date dateAdd = getT0day(marginMinute).getTime();
                    final SimpleDateFormat df = new SimpleDateFormat("HH:mm");

                    try {
                        Date date0400 = df.parse("04:00");

                        JSONObject json = new JSONObject(HEADER + result + FOOTER);
                        JSONArray dataArray = json.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);


                            String railway;
                            try {
                                if (dataObject.has("odpt:railwayTitle"))
                                    railway = dataObject.getString("odpt:railwayTitle");
                                else
                                    railway = getTitle(context, dataObject.getString("odpt:railway"));
                            } catch (Exception e) {
                                railway = getTitle(context, dataObject.getString("odpt:railway"));
                            }

                            String station;
                            try {
                                if (dataObject.has("odpt:stationTitle"))
                                    station = dataObject.getString("odpt:stationTitle");
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
                                    railDirection = dataObject.getString("odpt:railDirectionTitle");
                                else
                                    railDirection = getTitle(context, dataObject.getString("odpt:railDirection"));
                            } catch (Exception e) {
                                railDirection = getTitle(context, dataObject.getString("odpt:railDirection"));
                            }

                            String note = station + (station.endsWith("駅") ? "" : "駅") + " " + //
                                    railway + (railway.endsWith("線") ? "" : "線") + " " + //
                                    railDirection;
                            if (OdptKey.IS_DEBUG)
                                Log.v("TTW", "note: " + note + " " + railDirection);

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

                    // if (OdptKey.IS_DEBUG) Log.v("TTW", "sb: " + sb.toString());
                    // if (!sb.toString().equals(""))
                    //     TimeTableWidget.updateAppWidget(context, sb.toString());

                    return sb.toString();

                    //     }
                    // }).start();
                } catch (Exception e) {
                }
            //     }
            // });
            // asyncDlTask.execute(url);
        } catch (Exception e) {
        }

        return "";
    }
}