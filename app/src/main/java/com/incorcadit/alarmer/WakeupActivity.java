package com.incorcadit.alarmer;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.incorcadit.alarmer.database.AlarmDBScheme;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class WakeupActivity extends AppCompatActivity {
    private AlarmLab mAlarmLab;
    private Alarm mAlarm;
    private Vibrator vibrator;
    private Ringtone ringtone;

    private TextView mLabel;
    private Button mDisable, mWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wakeup);

        mAlarmLab = AlarmLab.getAlarmLab(this);
        String uuid = getIntent().getStringExtra(AlarmDBScheme.cols.UUID);
        mAlarm = mAlarmLab.getAlarm(UUID.fromString(uuid));

        // Set vibration
        if (mAlarm.isVibrating()) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{700,700,700},
                        new int[]{-1,-1,-1}, 2));
            } else {
                vibrator.vibrate(new long[]{700,700,700}, 2);
            }
        }

        // Set ringtone
        int index;
        if (mAlarm.isRandom()) {
            Random random = new Random(new Date().getTime());
            index = random.nextInt(mAlarm.getSource().size());
        } else {
            index = mAlarm.getCurSong();
            mAlarm.setCurSong(mAlarm.getCurSong() + 1);
            mAlarmLab.updateAlarm(mAlarm);
        }
        ringtone = RingtoneManager.getRingtone(this,Uri.parse(mAlarm.getSource().get(index)));
        ringtone.play();

        // Set up views
        mLabel = findViewById(R.id.label);
        mDisable = findViewById(R.id.disable);
        mWait = findViewById(R.id.wait);

        mLabel.setText(mAlarm.getLabel());

        mDisable.setOnClickListener((view) -> {
            stopAlarm();

            if (mAlarm.getRepeatMode() == Alarm.REPEAT_ONCE) {
                mAlarm.setOn(false, WakeupActivity.this);
            } else {
                mAlarm.setRightTime(this);
            }
            finishAffinity();
        });

        mWait.setOnClickListener((view) -> {
            stopAlarm();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mAlarm.getTime());
            calendar.add(Calendar.MINUTE,10);
            mAlarm.setTime(calendar.getTime(), WakeupActivity.this);
            finishAffinity();
        });
    }

    private void stopAlarm() {
        ringtone.stop();

        if (mAlarm.isVibrating()) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        if (mAlarm.getRepeatMode() == Alarm.REPEAT_ONCE) {
            mAlarm.setOn(false, WakeupActivity.this);
        } else {
            mAlarm.setRightTime(this);
        }

        super.onDestroy();
    }
}


