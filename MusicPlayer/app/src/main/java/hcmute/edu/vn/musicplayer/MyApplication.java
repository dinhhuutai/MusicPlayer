package hcmute.edu.vn.musicplayer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {
    public static final String CHANNEL_ID_1 = "channel1";
    public static final String CHANNEL_ID_2 = "channel2";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_NEXT = "actionnext";
    public static final String ACTION_PLAY = "actionplay";

    public static final String ACTION_CLEAR = "actionclear";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChanel();
    }

    private void createNotificationChanel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_ID_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            channel1.setSound(null, null);
            channel2.setSound(null, null);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel1);
                notificationManager.createNotificationChannel(channel2);
            }
        }
    }
}
