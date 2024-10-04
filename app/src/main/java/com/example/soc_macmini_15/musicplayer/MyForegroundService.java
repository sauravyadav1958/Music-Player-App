//package com.example.soc_macmini_15.musicplayer;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//
//import androidx.core.app.NotificationCompat;
//
//public class MyForegroundService extends Service {
//    private static final int NOTIFICATION_ID = 1;
//    private static final String CHANNEL_ID = "ForegroundServiceChannel";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        createNotificationChannel();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Notification notification = createNotification();
//        startForeground(NOTIFICATION_ID, notification);
//
//        // Do your foreground service tasks here
//
//        return START_STICKY;
//    }
//
//    private Notification createNotification() {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Foreground Service")
//                .setContentText("The service is running...")
//                .setSmallIcon(R.drawable.ic_music_player)
//                .setPriority(NotificationCompat.PRIORITY_LOW);
//
//        return builder.build();
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel serviceChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Foreground Service Channel",
//                    NotificationManager.IMPORTANCE_LOW
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(serviceChannel);
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
//
