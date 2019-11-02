package com.incorcadit.alarmer;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AlarmFragmentTest {
    private Alarm alarm;

    @Mock
    private Context context;

    private Calendar calendar;

    @Before
    public void setAlarmTime() {
        // in order to use mock you should comment methods which call Context's methods
        initMocks(this);

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR,24);

        alarm = new Alarm(calendar.getTime(),context);
        alarm.setRepeatMode(Alarm.REPEAT_CUSTOM);
    }

    @Test
    public void setRightTimeThisWeek() {
        alarm.setRepeatDays(Arrays.asList(1,3,7));
        alarm.setRightTime(context);
        Assert.assertEquals(alarm.getTime(),calendar.getTime());
    }

    @Test
    public void setRightTimeNextWeek() {
        alarm.setRepeatDays(Arrays.asList(3,5));
        alarm.setRightTime(context);

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR,24*4);
        Assert.assertEquals(alarm.getTime(),calendar.getTime());
    }

    @Test
    public void setRightTimeToday() {
        alarm.setRepeatDays(Arrays.asList(6));
        alarm.setRightTime(context);

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR,24*7);
        Assert.assertEquals(alarm.getTime(),calendar.getTime());
    }
}
