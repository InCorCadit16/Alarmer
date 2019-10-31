package com.incorcadit.alarmer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static String INDEX = "index";

    public AlarmLab mAlarmLab;
    public ArrayList<Alarm> mAlarms;
    private ActionBar mActionBar;
    private RecyclerView mRecycler;
    private FloatingActionButton mFloatingButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAlarmLab = AlarmLab.getAlarmLab(this);
        mAlarms = mAlarmLab.getAlarms();
        mAlarmLab.fixAlarmTime(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        mRecycler = findViewById(R.id.alarm_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(new AlarmAdapter(mAlarms));

        mFloatingButton = findViewById(R.id.add_alarm);
        mFloatingButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, AlarmPagerActivity.class);
            intent.putExtra(INDEX,-1);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    private class AlarmAdapter extends RecyclerView.Adapter<AlarmHolder> {
        private ArrayList<Alarm> mAlarms;

        AlarmAdapter(ArrayList<Alarm> alarms) {
            mAlarms = alarms;
        }

        @NonNull
        @Override
        public AlarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            return new AlarmHolder(inflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AlarmHolder holder, int position) {
            holder.bind(mAlarms.get(position));
        }

        @Override
        public int getItemCount() {
            return mAlarms.size();
        }
    }

    private class AlarmHolder extends RecyclerView.ViewHolder {
        private TextView mTimeView;
        private TextView mLabelView;
        private TextView mRemaining;
        private Switch mSwitcher;

        AlarmHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.alarm,parent,false));

            mTimeView = itemView.findViewById(R.id.time);
            mLabelView = itemView.findViewById(R.id.label);
            mRemaining = itemView.findViewById(R.id.remains);
            mSwitcher = itemView.findViewById(R.id.switcher);
        }

        void bind(Alarm alarm) {
            mTimeView.setText(alarm.getTimeString());
            mLabelView.setText(alarm.getLabel());
            mRemaining.setText(alarm.getRemainingString(MainActivity.this));
            mSwitcher.setChecked(alarm.isOn());

            if (alarm.isOn()) mRemaining.setVisibility(View.VISIBLE);
            else mRemaining.setVisibility(View.GONE);

            mSwitcher.setOnCheckedChangeListener((buttonView,isChecked) -> {
                alarm.setOn(isChecked, MainActivity.this);
                mAlarmLab.updateAlarm(alarm);
                if (alarm.isOn()) {
                    mRemaining.setText(alarm.getRemainingString(MainActivity.this));
                    mRemaining.setVisibility(View.VISIBLE);
                }
                else mRemaining.setVisibility(View.GONE);
            });

            mTimeView.getRootView().setOnClickListener((view) -> {
                Intent intent = new Intent(MainActivity.this, AlarmPagerActivity.class);
                intent.putExtra(INDEX,mAlarms.indexOf(alarm));
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.rate_app:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.incorcadit.alarmer"));
                startActivity(intent);
                break;
            case R.id.source_code:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/InCorCadit16/Alarmer.git"));
                startActivity(intent);
                break;
            case R.id.contribute:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://my.qiwi.com/Nykolai-RvAPJh0jOb"));
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
