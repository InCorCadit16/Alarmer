package com.incorcadit.alarmer;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.incorcadit.alarmer.database.AlarmCursorWrapper;
import com.incorcadit.alarmer.database.AlarmDBScheme;
import com.incorcadit.alarmer.database.AlarmOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class AlarmLab {
    private Context mContext;
    private static AlarmLab sAlarmLab;
    private ArrayList<Alarm> mAlarms;
    private SQLiteDatabase mDatabase;

    private AlarmLab(Context context) {
        mContext = context;
        mAlarms = new ArrayList<>();
        mDatabase = new AlarmOpenHelper(context).getWritableDatabase();
        mAlarms = getAlarmsFromDB();
        fixAlarmTime(context);
    }

    static AlarmLab getAlarmLab(Context context) {
        if (sAlarmLab == null) {
            sAlarmLab = new AlarmLab(context);
        }
        return sAlarmLab;
    }

    ArrayList<Alarm> getAlarms() {
        if (mAlarms != null)
            return mAlarms;
        else
            return getAlarmsFromDB();
    }

    public void fixAlarmTime(Context context) {
       for (Alarm alarm : mAlarms) {
           if (alarm.getTime().getTime() < new Date().getTime())
               alarm.setOn(false,context);
       }
    }

    void updateAlarm(Alarm alarm) {
        if (alarm == null) return;
        String uuid = alarm.getId().toString();
        ContentValues values = getContentValues(alarm);
        mDatabase.update(AlarmDBScheme.NAME,values,
                AlarmDBScheme.cols.UUID + "= ?", new String[]{uuid});
    }

    void addAlarm(Alarm alarm) {
        ContentValues values = getContentValues(alarm);
        mDatabase.insert(AlarmDBScheme.NAME,null,values);
        mAlarms.add(alarm);
    }

    void deleteAlarm(Alarm alarm) {
        alarm.setOn(false, mContext);
        mAlarms.remove(alarm);
        String uuid = alarm.getId().toString();
        mDatabase.delete(AlarmDBScheme.NAME,AlarmDBScheme.cols.UUID + "= ?", new String[]{uuid});
    }

    Alarm getAlarm(UUID id) {
        try (AlarmCursorWrapper cursor = queryAlarms(AlarmDBScheme.cols.UUID + "=? ", new String[]{id.toString()})) {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            return cursor.getAlarm();
        }
    }

    AlarmManager getAM() { return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE); }

    private static ContentValues getContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(AlarmDBScheme.cols.UUID, alarm.getId().toString());
        values.put(AlarmDBScheme.cols.DATE, alarm.getTime().getTime());
        values.put(AlarmDBScheme.cols.LABEL, alarm.getLabel());
        values.put(AlarmDBScheme.cols.ISON, alarm.isOn()?1:0);
        values.put(AlarmDBScheme.cols.ISRANDOM, alarm.isRandom()?1:0);
        values.put(AlarmDBScheme.cols.ISVIBRATING, alarm.isVibrating()?1:0);
        values.put(AlarmDBScheme.cols.REPEATMODE, alarm.getRepeatMode());
        values.put(AlarmDBScheme.cols.CURSONG, alarm.getCurSong());
        values.put(AlarmDBScheme.cols.SOURCES, Arrays.toString(alarm.getSource().toArray()).replace("[","").replace("]",""));
        values.put(AlarmDBScheme.cols.WEEKDAYS, Arrays.toString(alarm.getRepeatDays().toArray()).replace("[","").replace("]",""));
        return values;
    }

    private AlarmCursorWrapper queryAlarms(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                AlarmDBScheme.NAME,
                null,
                whereClause,
                whereArgs,
                null, null, null);

        return new AlarmCursorWrapper(cursor);
    }

    private ArrayList<Alarm> getAlarmsFromDB() {
        ArrayList<Alarm> list = new ArrayList<>();

        try (AlarmCursorWrapper cursor = queryAlarms(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(cursor.getAlarm());
                cursor.moveToNext();
            }
        }

        return list;
    }
}
