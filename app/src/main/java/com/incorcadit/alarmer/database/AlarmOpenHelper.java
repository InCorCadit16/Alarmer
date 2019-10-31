package com.incorcadit.alarmer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String NAME = "alarmBase.db";

    public AlarmOpenHelper(Context context) {
        super(context,NAME,null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AlarmDBScheme.NAME + "(" +
                "_id integer primary key autoincrement," +
                AlarmDBScheme.cols.UUID + ", " +
                AlarmDBScheme.cols.DATE + ", " +
                AlarmDBScheme.cols.LABEL + ", " +
                AlarmDBScheme.cols.ISON + ", " +
                AlarmDBScheme.cols.CURSONG + " INT, " +
                AlarmDBScheme.cols.ISRANDOM + ", " +
                AlarmDBScheme.cols.ISVIBRATING + ", " +
                AlarmDBScheme.cols.REPEATMODE + " INT, " +
                AlarmDBScheme.cols.SOURCES + " TEXT, " +
                AlarmDBScheme.cols.WEEKDAYS +
                ")");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
