package net.sourceforge.opencamera;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProviderTakePhoto extends AppWidgetProvider {
    private static final String TAG = "MyWidgetProvider";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TakePhoto.class), 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_take_photo);
            views.setOnClickPendingIntent(R.id.widget_take_photo, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
