package com.incorcadit.alarmer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.incorcadit.alarmer.dialogs.ChoiceDialog;
import com.incorcadit.alarmer.dialogs.PickDateDialog;
import com.incorcadit.alarmer.dialogs.PickTimeDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class AlarmFragment extends Fragment {
    private static final int REQUEST_CHOICE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_DATE = 2;
    private static final int REQUEST_RINGTONE = 3;

    public static final String EXTRA_CHOICE = "extra_choice";
    public static final String EXTRA_DATE = "extra_date";
    private int newSignalPosition;

    private Resources mRes;
    private AlarmLab mAlarmLab;
    public Alarm mAlarm;

    private EditText mLabel;
    private TextView mRepeatType;
    private CheckBox mRepeat;
    private LinearLayout mWeekdays;
    private TextView mTime, mDate;
    private LinearLayout mSignalContainer;
    private CheckBox mRandomOrder;

    static AlarmFragment createFragment(Alarm alarm) {
        AlarmFragment fragment = new AlarmFragment();
        fragment.mAlarm = alarm;
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlarmLab = AlarmLab.getAlarmLab(getActivity());
        mRes = getActivity().getResources();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alarm,container,false);

        // Block with time
        mTime = v.findViewById(R.id.time);
        mDate = v.findViewById(R.id.date);

        mTime.setText(mAlarm.getTimeString());
        mTime.setOnClickListener((view) -> {
            PickTimeDialog pickTimeDialog = PickTimeDialog.newDialog(mAlarm.getTime());
            pickTimeDialog.setTargetFragment(AlarmFragment.this,REQUEST_TIME);
            pickTimeDialog.show(getFragmentManager(),EXTRA_DATE);
        });

        mDate.setText(mAlarm.getDateString());
        if (mAlarm.isRepeating())
            mDate.setVisibility(INVISIBLE);

        mDate.setOnClickListener((view) -> {
            PickDateDialog pickDateDialog = PickDateDialog.newDialog(mAlarm.getTime());
            pickDateDialog.setTargetFragment(AlarmFragment.this,REQUEST_DATE);
            pickDateDialog.show(getFragmentManager(),EXTRA_DATE);
        });

        // Block with repeat mode
        mRepeatType = v.findViewById(R.id.repeat_type);
        mRepeat = v.findViewById(R.id.repeat_checkbox);
        mWeekdays = v.findViewById(R.id.weekdays);

        if (!mAlarm.isRepeating()) {
            mWeekdays.setVisibility(GONE);
            mRepeat.setChecked(false);
        } else {
            mRepeat.setChecked(true);
        }


        mRepeatType.setText(mRes.getString(mAlarm.getRepeatMode()));
        mRepeatType.setOnClickListener((view) -> {
            ChoiceDialog choiceDialog = ChoiceDialog.newDialog(R.layout.repeat_dialog);
            choiceDialog.setTargetFragment(AlarmFragment.this,REQUEST_CHOICE);
            choiceDialog.show(getFragmentManager(),EXTRA_CHOICE);
        });

        mRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mAlarm.setRepeatMode(Alarm.REPEAT_EVERYDAY);
                mWeekdays.setVisibility(VISIBLE);
                mDate.setVisibility(INVISIBLE);
                mAlarm.setRepeatDays(new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7)));
                updateDaysView();
            } else {
                mAlarm.setRepeatMode(Alarm.REPEAT_ONCE);
                mWeekdays.setVisibility(GONE);
                mDate.setVisibility(VISIBLE);
                mAlarm.setRepeatDays(new ArrayList<>());
                updateDaysView();
            }

            mRepeatType.setText(mRes.getString(mAlarm.getRepeatMode()));
        });

        updateDaysView();
        for (int i = 0; i < mWeekdays.getChildCount(); i++) {
            TextView weekDay = (TextView) mWeekdays.getChildAt(i);

            weekDay.setOnClickListener((view) -> {
                if (mAlarm.getRepeatDays().contains(mWeekdays.indexOfChild(weekDay) + 1)) {
                    mAlarm.getRepeatDays().remove(Integer.valueOf(mWeekdays.indexOfChild(weekDay) + 1));
                    weekDay.setBackground(mRes.getDrawable(R.drawable.weekday_disabled, getActivity().getTheme()));
                    updateRepeatView();
                } else {
                    mAlarm.getRepeatDays().add(mWeekdays.indexOfChild(weekDay) + 1);
                    weekDay.setBackground(mRes.getDrawable(R.drawable.weekday, getActivity().getTheme()));
                    updateRepeatView();
                }
            });
        }

        // Block with signal
        CheckBox mVibrate = v.findViewById(R.id.vibrate_checkbox);
        mRandomOrder = v.findViewById(R.id.random_order_checkbox);
        mSignalContainer = v.findViewById(R.id.signals_container);
        ImageButton mAddSignal = v.findViewById(R.id.add_signal);

        for (String source : mAlarm.getSource()) {
            addSignalLayout(source);
        }

        mAddSignal.setOnClickListener((view) -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, mRes.getString(R.string.add_signal, mAlarm.getSource().size() + 1));
            newSignalPosition = mAlarm.getSource().size();
            startActivityForResult(intent,REQUEST_RINGTONE);
        });

        mVibrate.setChecked(mAlarm.isVibrating());
        mVibrate.setOnCheckedChangeListener(((buttonView, isChecked) -> mAlarm.setVibrating(isChecked)));

        if (mAlarm.getSource().size() < 2)
            mRandomOrder.setVisibility(GONE);

        mRandomOrder.setChecked(mAlarm.isRandom());
        mRandomOrder.setOnCheckedChangeListener(((buttonView, isChecked) -> mAlarm.setRandom(isChecked)));

        // Block with label
        mLabel = v.findViewById(R.id.label);

        mLabel.setText(mAlarm.getLabel());

        // Block with buttons;
        Button mOnOffButton = v.findViewById(R.id.on_off_button);
        Button mDeleteButton = v.findViewById(R.id.delete_button);

        mOnOffButton.setText(mAlarm.isOn()? mRes.getString(R.string.disable): mRes.getString(R.string.enable));
        mOnOffButton.setOnClickListener((view) -> {
            mAlarm.setOn(!mAlarm.isOn(), getActivity());
            mOnOffButton.setText(mAlarm.isOn()? mRes.getString(R.string.disable): mRes.getString(R.string.enable));
        });

        mDeleteButton.setOnClickListener((view) -> {
            ViewPager pager = ((AlarmPagerActivity) getActivity()).mAlarmPager;
            int index = mAlarmLab.getAlarms().indexOf(mAlarm);
            pager.getAdapter().destroyItem(pager, index, AlarmFragment.this);
            mAlarmLab.deleteAlarm(mAlarm);
            pager.getAdapter().notifyDataSetChanged();
            onPause();
            if (index != 0) {
                pager.setCurrentItem(index - 1);
                pager.getAdapter().notifyDataSetChanged();
            } else
                getActivity().finish();
        });

        return v;
    }

    private void addSignalLayout(String source) {
        LinearLayout mSignalLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.signal,null);
        mSignalContainer.addView(mSignalLayout);

        TextView mName = mSignalLayout.findViewById(R.id.signal_name);
        mName.setText(mAlarm.getSourceShortName(source));
        mName.setOnClickListener((view) -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,mRes.getString(R.string.add_signal, mAlarm.getSource().indexOf(source)));
            newSignalPosition = mAlarm.getSource().indexOf(source);
            startActivityForResult(intent,REQUEST_RINGTONE);
        });

        ImageButton mRemove = mSignalLayout.findViewById(R.id.button_remove);
        mRemove.setOnClickListener((view) -> {
            if (mAlarm.getSource().indexOf(source) <= mAlarm.getCurSong()) mAlarm.setCurSong(mAlarm.getCurSong() - 1);
            mAlarm.getSource().remove(source);
            mSignalContainer.removeView(mSignalLayout);
            mSignalContainer.refreshDrawableState();
            if (mAlarm.getSource().size() == 1) {
                LinearLayout mLonelySignal = (LinearLayout)  mSignalContainer.getChildAt(0);
                ImageButton mLonelyRemove = mLonelySignal.findViewById(R.id.button_remove);
                mLonelyRemove.setVisibility(GONE);
                mRandomOrder.setVisibility(GONE);
            }
        });

        if (mAlarm.getSource().size() == 1) {
            LinearLayout mLonelySignal = (LinearLayout)  mSignalContainer.getChildAt(0);
            ImageButton mLonelyRemove = mLonelySignal.findViewById(R.id.button_remove);
            mLonelyRemove.setVisibility(GONE);
            mRandomOrder.setVisibility(GONE);
        }
    }

    // TODO: update time for this cases
    private void updateDaysView() {
        for (int i = 0; i < mWeekdays.getChildCount(); i++) {
            TextView weekDay = (TextView) mWeekdays.getChildAt(i);
            if (mAlarm.getRepeatDays().contains(mWeekdays.indexOfChild(weekDay) + 1)) {
                weekDay.setBackground(mRes.getDrawable(R.drawable.weekday, getActivity().getTheme()));
            } else {
                weekDay.setBackground(mRes.getDrawable(R.drawable.weekday_disabled, getActivity().getTheme()));
            }
        }
    }

    private void updateRepeatView() {
        if (mAlarm.getRepeatDays().containsAll(new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7)))) {
            mRepeatType.setText(Alarm.REPEAT_EVERYDAY);
        } else if (mAlarm.getRepeatDays().containsAll(new ArrayList<>(Arrays.asList(1,2,3,4,5))) &
            !mAlarm.getRepeatDays().contains(6) & !mAlarm.getRepeatDays().contains(7)) {
            mRepeatType.setText(Alarm.REPEAT_WORKDAYS);
        } else if (mAlarm.getRepeatDays().containsAll(new ArrayList<>(Arrays.asList(6,7))) &
                !mAlarm.getRepeatDays().contains(1) & !mAlarm.getRepeatDays().contains(2) &
                !mAlarm.getRepeatDays().contains(3) & !mAlarm.getRepeatDays().contains(4) &
                !mAlarm.getRepeatDays().contains(5)) {
            mRepeatType.setText(Alarm.REPEAT_WEEKENDS);
        } else if(mAlarm.getRepeatDays().size() == 0) {
            mAlarm.setRepeatMode(Alarm.REPEAT_ONCE);
            mWeekdays.setVisibility(GONE);
            mDate.setVisibility(VISIBLE);
            mAlarm.setRepeatDays(new ArrayList<>());
            mRepeatType.setText(Alarm.REPEAT_ONCE);
            mRepeat.setChecked(false);
        } else mRepeatType.setText(Alarm.REPEAT_CUSTOM);
    }

    @Override
    public void onPause() {
        mAlarm.setLabel(mLabel.getText().toString());

       mAlarm.setRightTime(getActivity());

        mAlarmLab.updateAlarm(mAlarm);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_CHOICE) {
            String text = data.getStringExtra(EXTRA_CHOICE);
            mRepeatType.setText(text);

            int repeatMode = -1;
            try {
                Field id = R.string.class.getDeclaredField(text.toLowerCase().replace(" ","_"));
                repeatMode = id.getInt(id);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log.e("TAG",e.getMessage());
            }

            if (repeatMode != -1)
                mAlarm.setRepeatMode(repeatMode);

            if (mAlarm.isRepeating()) {
                mRepeat.setChecked(true);
                mWeekdays.setVisibility(VISIBLE);
                mDate.setVisibility(INVISIBLE);

                switch (repeatMode) {
                    case Alarm.REPEAT_EVERYDAY: mAlarm.setRepeatDays(new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7))); break;
                    case Alarm.REPEAT_WORKDAYS: mAlarm.setRepeatDays(new ArrayList<>(Arrays.asList(1,2,3,4,5))); break;
                    case Alarm.REPEAT_WEEKENDS: mAlarm.setRepeatDays(new ArrayList<>(Arrays.asList(6,7))); break;
                }

                updateDaysView();
                updateRepeatView();
            } else {
                mRepeat.setChecked(false);
                mWeekdays.setVisibility(GONE);
                mDate.setVisibility(VISIBLE);
            }

        } else if (requestCode == REQUEST_TIME | requestCode == REQUEST_DATE) {
            Date newTime = (Date) data.getSerializableExtra(EXTRA_DATE);
            mAlarm.setTime(newTime, getActivity());
            mTime.setText(mAlarm.getTimeString());
            mDate.setText(mAlarm.getDateString());
        } else if (requestCode == REQUEST_RINGTONE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null) {
                String newSource = uri.getPath();
                int index = newSignalPosition;
                if (index < mAlarm.getSource().size()) {
                    mAlarm.getSource().remove(index);
                    mAlarm.getSource().add(index, newSource);

                    LinearLayout mSignalLayout = (LinearLayout) mSignalContainer.getChildAt(index);
                    TextView mName = mSignalLayout.findViewById(R.id.signal_name);
                    mName.setText(mAlarm.getSourceShortName(index));
                } else {
                    if (mAlarm.getSource().contains(newSource)) {
                        Toast.makeText(getActivity(),mRes.getString(R.string.already_chosen),Toast.LENGTH_LONG).show();
                        return;
                    }
                    mAlarm.getSource().add(newSource);
                    addSignalLayout(newSource);
                    if (mAlarm.getSource().size() == 2) {
                        LinearLayout mFirstSignal = (LinearLayout)  mSignalContainer.getChildAt(0);
                        ImageButton mFirstRemove = mFirstSignal.findViewById(R.id.button_remove);
                        mFirstRemove.setVisibility(VISIBLE);
                        mRandomOrder.setVisibility(VISIBLE);
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
