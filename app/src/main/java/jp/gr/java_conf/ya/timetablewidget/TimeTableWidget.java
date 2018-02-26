package jp.gr.java_conf.ya.timetablewidget; // Copyright (c) 2018 YA <ya.androidapp@gmail.com> All rights reserved.

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TimeTableWidgetConfigureActivity TimeTableWidgetConfigureActivity}
 */
public class TimeTableWidget extends AppWidgetProvider {
    public static final String ON_CLICK = "jp.gr.java_conf.ya.timetablewidget.ON_CLICK";

    static void updateAppWidget(Context context, String message) {
        //if(context!=null) {
        // Log.v("TTW", "updateAppWidget: " + message);
        ComponentName thisWidget = new ComponentName(context, TimeTableWidget.class);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId, message);
        //}
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String message) {
        // CharSequence widgetText = TimeTableWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.time_table_widget);
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
            updateAppWidget(context, appWidgetManager, appWidgetId, "---");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            PrefUtil.deleteTitlePref(context, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

