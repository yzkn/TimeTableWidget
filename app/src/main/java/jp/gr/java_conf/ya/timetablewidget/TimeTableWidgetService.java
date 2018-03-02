package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
    public static final SimpleDateFormat sdFormatLoad = new SimpleDateFormat("yyyy-MM-dd");
    public static final String ACTION_TIMER_TICK = "ACTION_TIMER_TICK";
    public static final String URL_HOLIDAYS = "http://www8.cao.go.jp/chosei/shukujitsu/syukujitsu_kyujitsu.csv";
    private Boolean requestingLocationUpdates = false;
    private long intervall_fast = 1 * 60 * 1000;
    private long intervall_norm = 3 * 60 * 1000;
    private long intervall_slow = 5 * 60 * 1000;
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

    public static void setHoliday(final Context context) {
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
                    if (OdptKey.IS_DEBUG)
                        Log.v("TTW", "checkIfTodayIsHoliday:" + Boolean.toString(isHoliday));

                    PrefUtil.savePref(context, PrefUtil.PREF_TODAY, getDateString(t0day));
                    PrefUtil.savePref(context, PrefUtil.PREF_TODAY_IS_HOLIDAY, (isHoliday ? "odpt.Calendar:Holiday" : "odpt.Calendar:Weekday"));
                }
            });
            aAsyncDlTask.execute(url);
        } catch (Exception e) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "onDestroy()");

        try {
            stopLocationUpdates();
        } catch (Exception e) {
        }

        try {
            stopAlarmService();
        } catch (Exception e) {
        }

        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "onStart()");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "onStartCommand()");
        super.onStartCommand(intent, flags, startId);

        // Oreo対策
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "default";
            String title = this.getString(R.string.app_name);

            NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(title);
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.setLightColor(Color.WHITE);
            channel.enableVibration(false);

            if (notificationManager != null) {
                PendingIntent pendingIntent = getPendingIntent();

                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(this, channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();
                startForeground(1, notification);
            }
        }

        if (intent != null) {
            if (OdptKey.IS_DEBUG) Log.v("TTWS", "onStartCommand() (intent != null)");
            if (intent.getAction() != null) {
                if (OdptKey.IS_DEBUG)
                    Log.v("TTWS", "onStartCommand() (intent.getAction() != null)");
                if (ACTION_TIMER_TICK.equals(intent.getAction())) {
                    int appWidgetId = -1;
                    try {
                        appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                    } catch (Exception e) {
                    }
                    timer_tick(appWidgetId);
                    return START_STICKY;
                } else {
                    if (OdptKey.IS_DEBUG) Log.v("TTWS", intent.getAction());
                }
            }
        }
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "onStartCommand() init()");
        init(intent);

        return START_STICKY;
    }

    private LocationSettingsRequest createLocationSettingsRequest() {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "createLocationSettingsRequest()");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(createLocationRequest());
        LocationSettingsRequest locationSettingsRequest = builder.build();
        return locationSettingsRequest;
    }

    private LocationCallback createLocationCallback() {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "createLocationCallback()");
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                ComponentName thisWidget = new ComponentName(TimeTableWidgetService.this, TimeTableWidget.class);
                int[] appWidgetIds = AppWidgetManager.getInstance(TimeTableWidgetService.this).getAppWidgetIds(thisWidget);
                for (int appWidgetId : appWidgetIds) {
                    String title = PrefUtil.loadTitlePref(TimeTableWidgetService.this, appWidgetId);
                    if (OdptKey.IS_DEBUG) Log.v("TTWS", "createLocationCallback()　appWidgetId: "+appWidgetId + " title: "+title);
                    if (PrefUtil.checkIfWantToUseGps(title)) {
                        Location location = locationResult.getLastLocation();
                        if (OdptKey.IS_DEBUG)
                            Log.v("TTWS", "location getLatitude: " + location.getLatitude());
                        if (OdptKey.IS_DEBUG)
                            Log.v("TTWS", "location getLongitude: " + location.getLongitude());
                        String lat = Double.toString(location.getLatitude());
                        String lon = Double.toString(location.getLongitude());
                        PrefUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LAT, lat);
                        PrefUtil.saveOdptPref(getApplicationContext(), OdptUtil.PREF_CURRENT_LON, lon);
                        callApi(lat, lon);
                    } else {
                        callApi(title);
                    }
                }
            }
        };
        return locationCallback;
    }

    private LocationRequest createLocationRequest() {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "createLocationRequest()");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(intervall_fast);
        locationRequest.setInterval(intervall_norm);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        PRIORITY_HIGH_ACCURACY
//        PRIORITY_BALANCED_POWER_ACCURACY
//        PRIORITY_LOW_POWER
//        PRIORITY_NO_POWER
        return locationRequest;
    }

    private void startLocationUpdates() {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "startLocationUpdates()");
        if (requestingLocationUpdates)
            return;

        try {
            if (settingsClient == null)
                settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(createLocationSettingsRequest());
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.requestLocationUpdates(
                    createLocationRequest(), createLocationCallback(), Looper.myLooper());
            requestingLocationUpdates = true;
        } catch (SecurityException e) {
        }
    }

    private void stopLocationUpdates() {
        if (OdptKey.IS_DEBUG) Log.v("TTW", "stopLocationUpdates()");
        if (!requestingLocationUpdates)
            return;

        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.removeLocationUpdates(createLocationCallback());
            requestingLocationUpdates = false;
        } catch (Exception e) {
        }
    }

    private void callApi(String lat, String lon) {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "callApi()");
        Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
        queryAcquirePlaces.put("x", lon);
        queryAcquirePlaces.put("y", lat);
        queryAcquirePlaces.put("method", "getStations");
        (new HeartRailsUtil()).acquirePlaces(this, queryAcquirePlaces);
        /*
        Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
        queryAcquirePlaces.put("rdf:type", "odpt:Station");
        queryAcquirePlaces.put("lon", lon);
        queryAcquirePlaces.put("lat", lat);
        queryAcquirePlaces.put("radius", "1000");
        (new OdptUtil()).acquirePlaces(queryAcquirePlaces);
        */
    }

    private void callApi(String uriStation) {
        Map<String, String> querysAcquireStation = new HashMap<String, String>();
        querysAcquireStation.put("dc:title", uriStation);
        if (OdptKey.IS_DEBUG)
            Log.v("TTW", "TTWS uriStation: " + uriStation);
        (new OdptUtil()).acquireStation(TimeTableWidgetService.this, querysAcquireStation, true);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, TimeTableWidgetService.class);
        intent.setAction(ACTION_TIMER_TICK);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private void init(Intent intent) {
        int appWidgetId = -1;
        try {
            appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        } catch (Exception e) {
        }
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "init() appWidgetId:" + appWidgetId);


        // 休日判定
        setHoliday(this);

        // 測位開始
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (settingsClient == null)
                settingsClient = LocationServices.getSettingsClient(this);
            createLocationSettingsRequest();
            startLocationUpdates();
        }

        String title = PrefUtil.loadTitlePref(this, appWidgetId);
        if (!PrefUtil.checkIfWantToUseGps(title)) {
            callApi(title);
        }

        // 次回起動
        setNextAlarmService(this);
    }

    private void setNextAlarmService(Context context) {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "setNextAlarmService()");
        long startMillis = System.currentTimeMillis() + intervall_slow;
        PendingIntent pendingIntent = getPendingIntent();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
    }

    private void stopAlarmService() {
        if (OdptKey.IS_DEBUG) Log.v("TTW", "stopAlarmService()");
        PendingIntent pendingIntent = getPendingIntent();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }

    public void timer_tick(int appWidgetId) {
        if (OdptKey.IS_DEBUG) Log.v("TTWS", "timer_tick");

        String title = PrefUtil.loadTitlePref(this, appWidgetId);
        if (PrefUtil.checkIfWantToUseGps(title)) {
            String lat = PrefUtil.loadOdptPref(this, OdptUtil.PREF_CURRENT_LAT);
            String lon = PrefUtil.loadOdptPref(this, OdptUtil.PREF_CURRENT_LON);
            callApi(lat, lon);
        }else{
            callApi(title);
        }

        setNextAlarmService(this);
    }

}