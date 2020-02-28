package net.i2p.android.router.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.darkweb.appManager.activityContextManager;
import com.darkweb.appManager.homeManager.homeController;
import com.darkweb.constants.status;

import com.example.myapplication.R;

import static org.mozilla.gecko.GeckoAppShell.getApplicationContext;

public class StatusBar {

    private Context mCtx;
    private static NotificationManager mNotificationManager = null;
    private static NotificationCompat.Builder mNotifyBuilder;
    private static Notification mNotif;
    private static int mNotifStatus = 0;

    private static String mBackupTitle="";
    private static String mBackupText="";

    private static final int ID = 1337;

    static final int ICON_STARTING = R.drawable.ic_stat_router_starting;
    static final int ICON_RUNNING = R.drawable.ic_stat_router_running;
    static final int ICON_ACTIVE = R.drawable.ic_stat_router_active;
    static final int ICON_STOPPING = R.drawable.ic_stat_router_stopping;
    static final int ICON_SHUTTING_DOWN = R.drawable.ic_stat_router_shutting_down;
    static final int ICON_WAITING_NETWORK = R.drawable.ic_stat_router_waiting_network;

    String channelId = "default_channel_id";
    String channelDescription = "Default Channel";
    final int NOTIFY_ID = 0; // ID of notification
    String id = "1001"; // default_channel_id
    Intent intent;

    StatusBar(Context ctx) {
        mNotifStatus = status.sNotificationStatus;
        mCtx = ctx;
        String text = ctx.getString(R.string.notification_status_starting);
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, text, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);
            }
            mNotifyBuilder = new NotificationCompat.Builder(mCtx, id);
            intent = new Intent(getApplicationContext(), homeController.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(activityContextManager.getInstance().getHomeController(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            int icon = ICON_STARTING;
            mNotifyBuilder.setContentTitle(text)                            // required
                    .setContentText(text)
                    .setSmallIcon(icon)
                    .setColor(mCtx.getResources().getColor(R.color.primary_light))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);

            mNotif = mNotifyBuilder.build();
            mNotifyBuilder.setContentIntent(pendingIntent);

            //mNotificationManager.notify(NOTIFY_ID, mNotif);
        }
        else {
            Thread.currentThread().setUncaughtExceptionHandler(
                    new CrashHandler(mNotificationManager));

            int icon = ICON_STARTING;

            mNotifyBuilder = new NotificationCompat.Builder(ctx)
                    .setContentText(text)
                    .setSmallIcon(icon)
                    .setColor(mCtx.getResources().getColor(R.color.primary_light))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);

            Intent intent = new Intent(getApplicationContext(), homeController.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(activityContextManager.getInstance().getHomeController(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyBuilder.setContentIntent(pi);
        }
    }

    public void replace(int icon, int textResource) {
        replace(icon, mCtx.getString(textResource));
    }

    public void replace(int icon, String title) {
        mNotifyBuilder.setSmallIcon(icon)
            .setStyle(null)
            .setTicker(title);
        update(title);
    }

    public void update(String title) {
        if(mNotifStatus!=1){
            mBackupTitle = title;
            update(title, null);
        }
    }

    public void update(String title, String text, String bigText) {
        if(mNotifStatus!=1) {
            mBackupTitle = title;
            mBackupText = text;

            mNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(bigText));
            update(title, text);
        }
    }

    public void update(String title, String text) {
        if(mNotifStatus!=1) {
            mBackupTitle = title;
            mBackupText = text;
            mNotifyBuilder.setContentTitle(title)
                    .setContentText(text);
            mNotif = mNotifyBuilder.build();
            mNotificationManager.notify(ID, mNotif);
        }
    }

    public void remove() {
        mNotificationManager.cancel(ID);
    }

    public static void toogleNotification(int status){
        mNotifStatus = status;
        if(status==0)
        {
            mNotifyBuilder.setContentTitle(mBackupTitle)
                    .setContentText(mBackupText);
            mNotif = mNotifyBuilder.build();
            mNotificationManager.notify(ID, mNotif);
        }
        else if(status==1)
        {
            mNotificationManager.cancelAll();
        }
    }

    /**
     * http://stackoverflow.com/questions/4028742/how-to-clear-a-notification-if-activity-crashes
     */
    private static class CrashHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler defaultUEH;
        private final NotificationManager mgr;

        public CrashHandler(NotificationManager nMgr) {
            defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            mgr = nMgr;
        }

        public void uncaughtException(Thread t, Throwable e) {
            if (mgr != null) {
                try {
                    mgr.cancel(ID);
                } catch (Throwable ex) {}
            }
            System.err.println("In CrashHandler " + e);
            e.printStackTrace(System.err);
            defaultUEH.uncaughtException(t, e);
        }
    }

    public Notification getNote() {
        return mNotif;
    }
}
