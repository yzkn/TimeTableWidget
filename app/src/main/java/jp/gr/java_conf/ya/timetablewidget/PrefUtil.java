package jp.gr.java_conf.ya.timetablewidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

public class PrefUtil {
    public static final String PREF_TODAY = "PREF_TODAY";
    public static final String PREF_TODAY_IS_HOLIDAY = "PREF_TODAY_IS_HOLIDAY";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_PREFIX_KEY_ODPT = "odpt_";
    private static SharedPreferences pref_app;

    public static String checkIfTodayIsHoliday(final Context context) {
        String checkedDay = loadPref(context, PREF_TODAY);
        String now = OdptUtil.getDateString(new Date());
        if (checkedDay.equals(now)) {
            return loadPref(context, PREF_TODAY_IS_HOLIDAY);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TimeTableWidgetService.setHoliday(context);
                }
            }).start();
            return "";
        }
    }

    public static boolean containsKeyPref(Context context, String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            return pref_app.contains(key);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean containsKeyOdptPref(Context context, String key) {
        return containsKeyPref(context, PREF_PREFIX_KEY_ODPT + key);
    }

    public static void deletePref(Context context, String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            SharedPreferences.Editor editor = pref_app.edit();
            editor.remove(key);
            editor.apply();
        } catch (Exception e) {
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        deletePref(context, PREF_PREFIX_KEY + appWidgetId);
    }


    public static String loadPref(Context context, String key) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        String value;
        try {
            value = pref_app.getString(key, "");
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    public static String loadOdptPref(Context context, String key) {
        return loadPref(context, PREF_PREFIX_KEY_ODPT + key);
    }

    static String loadTitlePref(Context context, int appWidgetId) {
        return loadPref(context, PREF_PREFIX_KEY + appWidgetId);
    }

    public static void savePref(Context context, String key, String value) {
        if (pref_app == null)
            pref_app = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            SharedPreferences.Editor editor = pref_app.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
        }
    }

    public static void saveOdptPref(Context context, String key, String value) {
        savePref(context, PREF_PREFIX_KEY_ODPT + key, value);
    }

    static void saveTitlePref(Context context, int appWidgetId, String value) {
        savePref(context, PREF_PREFIX_KEY + appWidgetId, value);
    }
}
