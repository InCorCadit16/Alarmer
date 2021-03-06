package com.incorcadit.alarmer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import com.incorcadit.alarmer.database.AlarmDBScheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Alarm {
    // repeat modes
    public static final int REPEAT_ONCE = R.string.only_once;
    public static final int REPEAT_EVERYDAY = R.string.everyday;
    public static final int REPEAT_WORKDAYS = R.string.only_on_workdays;
    public static final int REPEAT_WEEKENDS = R.string.only_on_weekends;
    public static final int REPEAT_CUSTOM = R.string.some_days;

    private final UUID id;
    private PendingIntent pi;
    Date time;
    private String label = "";
    private ArrayList<String> sources;
    private boolean isOn;
    private boolean isVibrating;
    private boolean isRandom;
    private int repeatMode;
    private int curSong;
    private List<Integer> repeatDays;

    // Recreate Alarm from database
    public Alarm (Date time, UUID id) {
        this.id = id;
        this.time = time;
        this.sources = new ArrayList<>();
        this.repeatMode = REPEAT_ONCE;
        this.isOn = true;
        this.isVibrating = false;
        this.isRandom = false;
        this.repeatDays = new ArrayList<>();
        this.curSong = 0;
    }

    // Create Alarm for the first time
    Alarm(Date time, Context context) {
        id = UUID.randomUUID();
        this.time = time;
        this.sources = new ArrayList<>();
        // Add default signal
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        sources.add(ringtone.getPath());

        this.repeatMode = REPEAT_ONCE;
        this.isOn = true;
        this.isVibrating = false;
        this.isRandom = false;
        this.repeatDays = new ArrayList<>();
        this.curSong = 0;

        // TODO: uncomment
        //setPi(context);
    }

    String getTimeString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(time);
    }

    String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM, EEEE");
        return format.format(time);
    }

    String getRemainingString(Context context) {
        long diff = time.getTime() - new Date().getTime();
        long days = TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
        long hours = TimeUnit.HOURS.convert(diff,TimeUnit.MILLISECONDS) - (days * 24);
        long minutes = TimeUnit.MINUTES.convert(diff,TimeUnit.MILLISECONDS) - (days * 24 * 60 + hours * 60);
        Resources res = context.getResources();
        StringBuilder str = new StringBuilder(res.getString(R.string.remains) + " ");
        if (days != 0) str.append(res.getString(R.string.d,days)).append(" ");
        if (hours != 0) str.append(res.getString(R.string.h, hours)).append(" ");
        if (minutes != 0) str.append(res.getString(R.string.m, minutes));
        return str.toString();
    }

    boolean isRepeating() {
        return repeatMode == REPEAT_EVERYDAY | repeatMode == REPEAT_WORKDAYS
                | repeatMode == REPEAT_WEEKENDS | repeatMode == REPEAT_CUSTOM;
    }

    String getSourceShortName(String source) {
        int lastSlash = source.lastIndexOf('/');
        return source.substring(lastSlash+1);
    }

    String getSourceShortName(int index) {
        return getSourceShortName(sources.get(index));
    }

    public UUID getId() {
        return id;
    }

    Date getTime() {
        return time;
    }

    void setTime(Date time, Context context) {
        this.time = time;
        if (pi != null) AlarmLab.getAlarmLab(context).getAM().cancel(pi);
        setPi(context);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    ArrayList<String> getSource() {
        return sources;
    }

    public void setSource(ArrayList<String> sources) { this.sources = sources; }

    boolean isOn() {
        return isOn;
    }

    void setOn(boolean on, Context context) {
        isOn = on;
        if (isOn) {
            if (time.getTime() < new Date().getTime()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
                calendar.set(Calendar.MINUTE, time.getMinutes());
                calendar.add(Calendar.HOUR_OF_DAY, 24);
                time = calendar.getTime();
            }
            AlarmLab.getAlarmLab(context).getAM().setExact(AlarmManager.RTC_WAKEUP,time.getTime(),pi);
            Toast.makeText(context,getRemainingString(context),Toast.LENGTH_LONG).show();
        } else {
            if (pi != null) AlarmLab.getAlarmLab(context).getAM().cancel(pi);
        }
    }

    public void setOnWithoutPi(boolean on) {
        isOn = on;
    }

    boolean isVibrating() {
        return isVibrating;
    }

    public void setVibrating(boolean vibrating) {
        isVibrating = vibrating;
    }

    boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }

    int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    List<Integer> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<Integer> repeatDays) {
        this.repeatDays = repeatDays;
    }

    void setPi(Context context) {
        Intent i = new Intent(context,AlarmReceiver.class);
        i.setAction("com.incorcadit.alarmer.ALARM");
        i.putExtra(AlarmDBScheme.cols.UUID, this.id.toString());
        pi = PendingIntent.getBroadcast(context,0,i,0);
        AlarmLab.getAlarmLab(context).getAM().setExact(AlarmManager.RTC_WAKEUP,time.getTime(),pi);
    }

    int getCurSong() { return curSong; }

    public void setCurSong(int curSong) { this.curSong = curSong; }

    void setRightTime(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int alarmDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (getRepeatMode() != Alarm.REPEAT_ONCE &
                (!getRepeatDays().contains(alarmDay) | calendar.getTime().getTime() < new Date().getTime())) {
            boolean needCycle = true;
            for (int i = alarmDay + 1; i < 8; i++) {
                if (getRepeatDays().contains(i)) {
                    alarmDay = i;
                    needCycle = false;
                    break;
                }
            }

            if (needCycle) {
                for (int i = 1; i <= alarmDay; i++) {
                    if (getRepeatDays().contains(i)) {
                        alarmDay = i;
                        break;
                    }
                }
            }

            int today = calendar.get(Calendar.DAY_OF_WEEK);
            if (today == 1) today = 7;
            else today--;

            if (alarmDay <= today)
                calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);

            calendar.set(Calendar.DAY_OF_WEEK, alarmDay+1);
            setTime(calendar.getTime(),context);
        }
    }

}
