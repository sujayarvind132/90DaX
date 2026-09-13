package com.sujayarvind.dax90;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

public class DaXWidget extends AppWidgetProvider {
    public static final String PREFS = "dax";
    public static final String STATE_KEY = "state";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, DaXWidget.class);
        for (int id : manager.getAppWidgetIds(name)) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dax_widget);
        JSONObject state = null;
        try { state = new JSONObject(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(STATE_KEY, "{}")); }
        catch (Exception ignored) { state = new JSONObject(); }

        int done = state.optInt("doneCount", 0);
        int total = Math.max(1, state.optInt("total", 13));
        int percent = Math.round(done * 100f / total);
        views.setTextViewText(R.id.widget_title, "90DaX");
        views.setTextViewText(R.id.widget_subtitle, "TODAY • EXECUTION");
        views.setTextViewText(R.id.widget_progress, done + " / " + total + " activities");
        views.setTextViewText(R.id.widget_percent, percent + "%");

        int[] rowIds = {R.id.activity_row_1,R.id.activity_row_2,R.id.activity_row_3,R.id.activity_row_4,R.id.activity_row_5};
        int[] textIds = {R.id.activity_text_1,R.id.activity_text_2,R.id.activity_text_3,R.id.activity_text_4,R.id.activity_text_5};
        int[] buttonIds = {R.id.activity_button_1,R.id.activity_button_2,R.id.activity_button_3,R.id.activity_button_4,R.id.activity_button_5};
        JSONArray activities = state.optJSONArray("activities");
        int count = activities == null ? 0 : Math.min(activities.length(), 5);
        for (int i = 0; i < 5; i++) {
            if (i >= count) { views.setViewVisibility(rowIds[i], View.GONE); continue; }
            try {
                JSONObject a = activities.getJSONObject(i);
                int activityId = a.optInt("id", -1);
                String title = a.optString("title", "Activity");
                String time = a.optString("time", "");
                String status = a.optString("status", "upcoming");
                views.setViewVisibility(rowIds[i], View.VISIBLE);
                views.setTextViewText(textIds[i], time + "  " + title);
                String action;
                String label;
                if ("done".equals(status)) { action = "page:Today"; label = "DONE ✓"; }
                else if ("current".equals(status)) { action = "complete:" + activityId; label = "COMPLETE"; }
                else { action = "page:Today"; label = "OPEN"; }
                views.setTextViewText(buttonIds[i], label);
                setAction(context, views, buttonIds[i], action, 100 + i + id * 10);
            } catch (Exception e) { views.setViewVisibility(rowIds[i], View.GONE); }
        }

        setAction(context, views, R.id.widget_root, "page:Today", 1 + id * 10);
        setAction(context, views, R.id.nav_today, "page:Today", 2 + id * 10);
        setAction(context, views, R.id.nav_goals, "page:Goals", 3 + id * 10);
        setAction(context, views, R.id.nav_plan, "page:Plan", 4 + id * 10);
        setAction(context, views, R.id.nav_progress, "page:Progress", 5 + id * 10);
        manager.updateAppWidget(id, views);
    }

    private static void setAction(Context context, RemoteViews views, int viewId, String action, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("widget_action", action);
        PendingIntent pi = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(viewId, pi);
    }
}
