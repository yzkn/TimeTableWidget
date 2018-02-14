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
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.util.HashMap;
import java.util.Map;

public class TimeTableWidgetService extends Service {
    private Boolean requestingLocationUpdates;

    private SharedPreferences pref_app;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private SettingsClient settingsClient;

    public TimeTableWidgetService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand()");
        super.onStartCommand(intent, flags, startId);

        startForeground(startId, new Notification());

        init(intent);

        return START_STICKY;
    }

    private void init(Intent intent){
        BroadcastReceiver roadcastReceiver = new OnClickReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TimeTableWidget.ON_CLICK);
        registerReceiver(roadcastReceiver, filter);

        int appWidgetId = -1;
        try{
            appWidgetId= intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        }catch(Exception e){}
        Log.d("TTW", "appWidgetId:" + appWidgetId);

        // Temp
        Map<String, String> querysAcquireStation = new HashMap<String, String>();
        querysAcquireStation.put("odpt:station", "odpt.Station:TokyoMetro.Chiyoda.Otemachi");
        (new OdptUtil()).acquireStationTimetable(querysAcquireStation);

        /*
        pref_app = PreferenceManager.getDefaultSharedPreferences(this);
        String pref_place;
        try {
            pref_place = pref_app.getString("pref_place", "here");
        } catch (Exception e) {
            pref_place = "here";
        }

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
        log("onDestroy()");

        try {
            stopLocationUpdates();
        } catch (Exception e) {
        }

        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        log("onStart()");
        super.onStart(intent, startId);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                // location.getLatitude() location.getLongitude() location.getAccuracy()
                // location.getAltitude() location.getSpeed() location.getBearing()
                log("location getLatitude: " + location.getLatitude());
                log("location getLongitude: " + location.getLongitude());

                // Temp
                /*
                String lat = Double.toString(location.getLatitude());
                String lon = Double.toString(location.getLongitude());

                Map<String, String> queryAcquirePlaces = new HashMap<String, String>();
                queryAcquirePlaces.put("rdf:type", "odpt:Station");
                queryAcquirePlaces.put("lon", lon);
                queryAcquirePlaces.put("lat", lat);
                queryAcquirePlaces.put("radius", "1000");
                (new OdptUtil()).acquirePlaces(queryAcquirePlaces);

                Map<String, String> querysAcquireStation = new HashMap<String, String>();
                querysAcquireStation.put("owl:sameAs", "odpt.Station:TokyoMetro.Chiyoda.Otemachi");
                (new OdptUtil()).acquireStationTimetable(querysAcquireStation);
                */
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void log(String str) {
        Log.v("TTWS", str);
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

    public class OnClickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("jp.co.casareal.oreobroadcastsample.ORIGINAL".equals(intent.getAction())) {
                String massage = intent.getStringExtra("message");
                Toast.makeText(context, massage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
