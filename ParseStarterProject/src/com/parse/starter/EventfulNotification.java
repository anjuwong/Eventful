package com.parse.starter;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

/**
 * Created by neil on 5/24/15.
 */
public class EventfulNotification {
    private static int requestCode = 0;

    /**
     * Returns a notification object in the context of the app
     *
     * @param context the Context of the application
     * @param title String:  title of the notification in the status bar
     * @param msg String: content of the notification to be displayed
     * @return Notification: the constructed notification object
     */
    public static Notification createNotification(Context context, String title, String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setContentText(msg);

        Intent notifyIntent =
                new Intent(context, ParseStarterProjectActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(notifyPendingIntent);
        return mBuilder.build();
    }

    /**
     * Schedules the supplied notification object to occur in *delay* milliseconds
     * Uses the alarm manager service to handle the delay
     *
     * @param context the Context of the application
     * @param notification  the notificaiton to be displayed
     * @param eventHash ID of the event, used to reschedule the notification if needed
     * @param delay the time to delay the notification in milliseconds
     */
    public static void scheduleNotification(Context context, Notification notification, int eventHash, int delay) {

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, eventHash);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, eventHash, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = Math.max((long)0.0, delay);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, futureInMillis, pendingIntent);
    }
}
