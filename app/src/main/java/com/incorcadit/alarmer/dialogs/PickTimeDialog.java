package com.incorcadit.alarmer.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incorcadit.alarmer.AlarmFragment;

import java.util.Calendar;
import java.util.Date;

public class PickTimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private Date mTime;

    public static PickTimeDialog newDialog(Date time) {
        PickTimeDialog dialog = new PickTimeDialog();
        dialog.mTime = time;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mTime);
        TimePickerDialog picker = new TimePickerDialog(getActivity(),this,
                calendar.get(Calendar.HOUR),calendar.get(Calendar.MINUTE),true);

        return picker;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mTime);
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        if (calendar.before(c)) {
            Toast.makeText(getActivity(),"Given time has gone", Toast.LENGTH_LONG).show();
            sendResult(Activity.RESULT_CANCELED,calendar.getTime());
        } else {
            sendResult(Activity.RESULT_OK, calendar.getTime());
        }
     }

    private void sendResult(int resultCode, Date time) {
        Intent intent = new Intent();
        intent.putExtra(AlarmFragment.EXTRA_DATE,time);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
