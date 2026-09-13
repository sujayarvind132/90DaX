package com.sujayarvind.dax90;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DaXWidget extends AppWidgetProvider {
    public static final String PREFS = "dax";
    public static final String STATE_KEY = "state";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) updateWidget(context, manager, id);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, DaXWidget.class);
        int[] ids = manager.getAppWidgetIds(name);
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(STATE_KEY, "");
        String done = match(raw, "\\\"doneCount\\\"\\s*:\\s*(\\d+)", "0");
        String total = match(raw, "\\\"total\\\"\\s*:\\s*(\\d+)", "13");
        String current = match(raw, "\\\"current\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"", "Open 90DaX");
        int percent = 0;
        try {
            int d = Integer.parseInt(done), t = Math.max(1, Integer.parseInt(total));
            percent = Math.round(d * 100f / t);
        } catch (Exception ignored) { }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dax_widget);
        views.setTextViewText(R.id.widget_title, "90DaX");
        views.setTextViewText(R.id.widget_progress, done + " / " + total + " activities");
        views.setTextViewText(R.id.widget_current, current);
        views.setTextViewText(R.id.widget_percent, percent + "% complete");
        Intent launch = new Intent(context, MainActivity.class);
        views.setOnClickPendingIntent(R.id.widget_root,
                android.app.PendingIntent.getActivity(context, 0, launch,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE));
        manager.updateAppWidget(id, views);
    }

    private static String match(String input, String regex, String fallback) {
        Matcher m = Pattern.compile(regex).matcher(input == null ? "" : input);
        return m.find() ? m.group(1) : fallback;
    }
}
