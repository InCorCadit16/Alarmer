package com.incorcadit.alarmer;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmPagerActivity extends AppCompatActivity {
    private AlarmLab mAlarmLab;
    private ArrayList<Alarm> mAlarms;
    private ActionBar mActionBar;

    ViewPager mAlarmPager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        mAlarmLab = AlarmLab.getAlarmLab(this);
        int curIndex = getIntent().getExtras().getInt(MainActivity.INDEX,-1);
        if (curIndex == -1) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR,24);
            Alarm alarm = new Alarm(calendar.getTime(), this);
            mAlarmLab.addAlarm(alarm);
            curIndex = mAlarmLab.getAlarms().size() - 1;
        }

        mAlarms = mAlarmLab.getAlarms();

        FragmentManager fm = getSupportFragmentManager();
        mAlarmPager = findViewById(R.id.alarm_pager);
        mAlarmPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return AlarmFragment.createFragment(mAlarms.get(position));
            }

            @Override
            public int getCount() {
                return mAlarmLab.getAlarms().size();
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                AlarmFragment fragment = (AlarmFragment) object;
                ft.remove(fragment).commit();
            }
        });

        mAlarmPager.setCurrentItem(curIndex);
        mActionBar.setTitle("#" + (curIndex+1));

        mAlarmPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mActionBar.setTitle("#" + (position+1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
