package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TimeTableWidgetConfigureActivity TimeTableWidgetConfigureActivity}
 */
public class TimeTableWidget extends AppWidgetProvider {
    public static final String ON_CLICK = "jp.gr.java_conf.ya.timetablewidget.ON_CLICK";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String message) {
        // CharSequence widgetText = TimeTableWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.time_table_widget);

        // ボタンイベント設定
        Intent intent = new Intent();
        intent.setAction(ON_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button, pendingIntent);

        remoteViews.setTextViewText(R.id.appwidget_text, message);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, TimeTableWidgetService.class));
        } else {
            context.startService(new Intent(context, TimeTableWidgetService.class));
        }

        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId, "");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            TimeTableWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

