package com.incorcadit.alarmer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.incorcadit.alarmer.database.AlarmDBScheme;

import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.incorcadit.alarmer.ALARM")) {
            // start Alarm
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "app:AlarmerLock");
            wl.acquire(500);
            String uuid = intent.getStringExtra(AlarmDBScheme.cols.UUID);

            Intent i = new Intent(context, WakeupService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(AlarmDBScheme.cols.UUID, uuid);
            context.startService(i);
        } else {
            AlarmLab alarmLab = AlarmLab.getAlarmLab(context);
            ArrayList<Alarm> alarms = alarmLab.getAlarms();
            for (Alarm alarm : alarms)
                alarm.setPi(context);
        }
    }
}
