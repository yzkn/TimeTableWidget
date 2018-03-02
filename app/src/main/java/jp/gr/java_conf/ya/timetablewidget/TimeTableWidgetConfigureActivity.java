package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;

/**
 * The configuration screen for the {@link TimeTableWidget TimeTableWidget} AppWidget.
 */
public class TimeTableWidgetConfigureActivity extends Activity {
    private final int REQUEST_PERMISSION = 1;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TimeTableWidgetConfigureActivity.this;

            String widgetText = mAppWidgetText.getText().toString();
            PrefUtil.saveTitlePref(context, mAppWidgetId, widgetText);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TimeTableWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, "");

            Intent intent = new Intent(TimeTableWidgetConfigureActivity.this, TimeTableWidgetService.class);
            intent.setAction(TimeTableWidgetService.ACTION_TIMER_TICK);
            PendingIntent pendingIntent = PendingIntent.getService(TimeTableWidgetConfigureActivity.this, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 1000, pendingIntent);
                else
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, 1000, pendingIntent);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public TimeTableWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.time_table_widget_configure);
        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(PrefUtil.loadTitlePref(TimeTableWidgetConfigureActivity.this, mAppWidgetId));

        if (Build.VERSION.SDK_INT >= 23)
            checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                finish();
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
    }
}

