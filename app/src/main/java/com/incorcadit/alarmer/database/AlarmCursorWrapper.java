package com.incorcadit.alarmer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;

import com.incorcadit.alarmer.Alarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AlarmCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public AlarmCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Alarm getAlarm() {
        String uuidString = getString(getColumnIndex(AlarmDBScheme.cols.UUID));
        String label = getString(getColumnIndex(AlarmDBScheme.cols.LABEL));
        long date = getLong(getColumnIndex(AlarmDBScheme.cols.DATE));
        int repeatMode = getInt(getColumnIndex(AlarmDBScheme.cols.REPEATMODE));
        int isOn = getInt(getColumnIndex(AlarmDBScheme.cols.ISON));
        int isRandom = getInt(getColumnIndex(AlarmDBScheme.cols.ISRANDOM));
        int isVibrating = getInt(getColumnIndex(AlarmDBScheme.cols.ISVIBRATING));
        int curSong = getInt(getColumnIndex(AlarmDBScheme.cols.CURSONG));
        String sourcesString = getString(getColumnIndex(AlarmDBScheme.cols.SOURCES));
        String weekdaysString = getString(getColumnIndex(AlarmDBScheme.cols.WEEKDAYS));

        Alarm alarm = new Alarm(new Date(date), UUID.fromString(uuidString));

        switch (repeatMode) {
            case 1: repeatMode = Alarm.REPEAT_ONCE; break;
            case 2: repeatMode = Alarm.REPEAT_EVERYDAY; break;
            case 3: repeatMode = Alarm.REPEAT_WORKDAYS; break;
            case 4: repeatMode = Alarm.REPEAT_WEEKENDS; break;
            case 5: repeatMode = Alarm.REPEAT_CUSTOM; break;
        }
        alarm.setRepeatMode(repeatMode);

        alarm.setLabel(label);
        alarm.setOnWithoutPi(isOn != 0);
        alarm.setRandom(isRandom != 0);
        alarm.setVibrating(isVibrating != 0);
        alarm.setRepeatMode(repeatMode);
        alarm.setCurSong(curSong);

        ArrayList<String> sources = new ArrayList<>();
        if (!sourcesString.isEmpty())
        sources = new ArrayList<>( Arrays.asList(sourcesString.split(", ")));
        alarm.setSource(sources);
        ArrayList<Integer> weekdays = new ArrayList<>();
        for (String stringDay : weekdaysString.split(", ")) {
            if (weekdaysString.isEmpty()) break;
            switch (Integer.valueOf(stringDay)) {
                case Calendar.MONDAY : weekdays.add(Calendar.MONDAY); break;
                case Calendar.TUESDAY : weekdays.add(Calendar.TUESDAY); break;
                case Calendar.WEDNESDAY : weekdays.add(Calendar.WEDNESDAY); break;
                case Calendar.THURSDAY : weekdays.add(Calendar.THURSDAY); break;
                case Calendar.FRIDAY : weekdays.add(Calendar.FRIDAY); break;
                case Calendar.SATURDAY : weekdays.add(Calendar.SATURDAY); break;
                default : weekdays.add(Calendar.SUNDAY);
            }
        }
        alarm.setRepeatDays(weekdays);
        return alarm;
    }
}
