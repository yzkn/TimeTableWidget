package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static jp.gr.java_conf.ya.timetablewidget.OdptUtil.getDateString;

public class TimeTableWidgetService extends Service {
    private static final String PREF_TODAY = "PREF_TODAY";
    private static final String PREF_TODAY_IS_HOLIDAY = "PREF_TODAY_IS_HOLIDAY";
    private static final SimpleDateFormat sdFormatLoad = new SimpleDateFormat("yyyy-MM-dd");
    private static final String URL_HOLIDAYS = "http://www8.cao.go.jp/chosei/shukujitsu/syukujitsu_kyujitsu.csv";
    private Boolean requestingLocationUpdates;
    private static SharedPreferences pref_app;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private SettingsClient settingsClient;

    public TimeTableWidgetService() {
    }

    private static boolean parseCsv(final Date t0day, final String csvString) {
        Boolean result = false;

        String t0dayString = getDateString(t0day);

        // 休日判定
        try {
            final InputStream inputStream = new ByteArrayInputStream(csvString.getBytes("UTF-8"));
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            String line;
            int i = -1;
            while ((line = bufferReader.readLine()) != null) {
                i++;

                if (i == 0)
                    continue;

                final String[] lineArray = line.split(",");

                try {
                    final Date date = sdFormatLoad.parse(lineArray[0]);

                    java.sql.Date dateSql = new java.sql.Date(date.getTime());
                    if (dateSql.toString().equals(t0dayString)) {
                        result = true;
                        break;
                    }
                } catch (Exception e) {
                }
            }
            bufferReader.close();
        } catch (Exception e) {
        }

        // 週末判定
        Calendar cal = Calendar.getInstance();
        cal.setTime(t0day);

        int dow = cal.get(Calendar.DAY_OF_WEEK);
        if (dow == 1 || dow == 7)
            result = true;

        return result;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("TTWS", "onStartCommand()");
        super.onStartCommand(intent, flags, startId);

        startForeground(startId, new Notification());

        init(intent);

        return START_STICKY;
    }

    private String loadPref(String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(this);

        String value;
        try {
            value = pref_app.getString(key, "");
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    private void savePref(String key, String value) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            SharedPreferences.Editor editor = pref_app.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
        }
    }

    private void init(Intent intent) {
        int appWidgetId = -1;
        try {
            appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        } catch (Exception e) {
        }
        Log.v("TTW", "appWidgetId:" + appWidgetId);

        // 休日判定
        setHoliday();

        // Temp
        Location location=new Location("");
        location.setLatitude(35.658);
        location.setLongitude(139.745);
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        OdptUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LAT, lat);
        OdptUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LON, lon);

        Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
        queryAcquirePlaces.put("x", lon);
        queryAcquirePlaces.put("y", lat);
        queryAcquirePlaces.put("method", "getStations");
        (new HeartRailsUtil()).acquirePlaces(getApplicationContext(), queryAcquirePlaces);

        /*
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
        */
    }

    @Override
    public void onDestroy() {
        Log.v("TTWS", "onDestroy()");

        try {
            stopLocationUpdates();
        } catch (Exception e) {
        }

        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        Log.v("TTWS", "onStart()");
        super.onStart(intent, startId);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                Log.v("TTWS", "location getLatitude: " + location.getLatitude());
                Log.v("TTWS", "location getLongitude: " + location.getLongitude());

                String lat = Double.toString(location.getLatitude());
                String lon = Double.toString(location.getLongitude());
                OdptUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LAT, lat);
                OdptUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LON, lon);

                // Temp
                Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
                queryAcquirePlaces.put("x", lon);
                queryAcquirePlaces.put("y", lat);
                queryAcquirePlaces.put("method", "getStations");
                (new HeartRailsUtil()).acquirePlaces(getApplicationContext(), queryAcquirePlaces);
                /*
                Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
                queryAcquirePlaces.put("rdf:type", "odpt:Station");
                queryAcquirePlaces.put("lon", lon);
                queryAcquirePlaces.put("lat", lat);
                queryAcquirePlaces.put("radius", "1000");
                (new OdptUtil()).acquirePlaces(queryAcquirePlaces);
                */
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(30 * 1000);
        locationRequest.setInterval(60 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
//        PRIORITY_LOW_POWER
//        PRIORITY_NO_POWER
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        try {
            settingsClient.checkLocationSettings(locationSettingsRequest);
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.myLooper());
            requestingLocationUpdates = true;
        } catch (SecurityException e) {
        }
    }

    private void stopLocationUpdates() {
        if (!requestingLocationUpdates)
            return;

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            requestingLocationUpdates = false;
        } catch (Exception e) {
        }
    }

    public String checkIfTodayIsHoliday() {
        String checkedDay = loadPref(PREF_TODAY);
        String now = getDateString(new Date());
        if (checkedDay.equals(now)) {
            return loadPref(PREF_TODAY_IS_HOLIDAY);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setHoliday();
                }
            }).start();
            return "";
        }

    }

    public void setHoliday() {
        try {
            final URL url = new URL(URL_HOLIDAYS);
            AsyncDlTask aAsyncDlTask = new AsyncDlTask(new AsyncDlTask.AsyncCallback() {

                public void onPreExecute() {
                }

                public void onProgressUpdate(int progress) {
                }

                public void onCancelled() {
                }

                public void onPostExecute(String[] result) {
                    Date t0day = OdptUtil.getT0day().getTime();
                    boolean isHoliday = parseCsv(t0day, result[0]);
                    Log.v("TTW", "checkIfTodayIsHoliday:" + Boolean.toString(isHoliday));

                    savePref(PREF_TODAY, getDateString(t0day));
                    savePref(PREF_TODAY_IS_HOLIDAY, (isHoliday?"odpt.Calendar:Holiday":"odpt.Calendar:Weekday"));
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

}