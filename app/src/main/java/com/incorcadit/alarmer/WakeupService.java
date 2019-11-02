package com.incorcadit.alarmer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.incorcadit.alarmer.database.AlarmDBScheme;

import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class WakeupService extends Service {
    private AlarmLab mAlarmLab;
    private Alarm mAlarm;

    NotificationChannel mChannel;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        mAlarmLab = AlarmLab.getAlarmLab(this);
        UUID uuid = UUID.fromString(intent.getStringExtra(AlarmDBScheme.cols.UUID));
        mAlarm = mAlarmLab.getAlarm(uuid);

        if (mAlarm == null) {
            Toast.makeText(this, R.string.error_message,Toast.LENGTH_LONG).show();
            stopSelf();
            super.onStart(intent, startId);
            return;
        }

        // Create Intent to Open Activity
        Intent openAct = new Intent(this, WakeupActivity.class);
        openAct.putExtra(AlarmDBScheme.cols.UUID,mAlarm.getId().toString());
        openAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openActivity = PendingIntent.getActivity(this,0,openAct,PendingIntent.FLAG_UPDATE_CURRENT);

        // Create notification channel
        NotificationCompat.Builder builder;
        createNotificationChannel();
        if (mChannel == null) {
            builder = new NotificationCompat.Builder(this,"notify_001");
        } else {
            builder = new NotificationCompat.Builder(this, "Alarmer");
        }

        builder = builder
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(mAlarm.getLabel())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(openActivity,true)
                .setContentIntent(openActivity)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher_round,"Open",openActivity);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        //notificationManager.notify(mAlarm.getId().hashCode(), builder.build());
        notificationManager.notify("Alarm",mAlarm.getId().hashCode(),builder.build());

        super.onStart(intent, startId);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel("Alarmer", name, importance);
            mChannel.setDescription("Show alarms");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mChannel);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }
    }

}
