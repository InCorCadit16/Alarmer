package com.incorcadit.alarmer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incorcadit.alarmer.AlarmFragment;

import java.util.Calendar;
import java.util.Date;

public class PickDateDialog extends DialogFragment implements DatePicker.OnDateChangedListener {
    private Date mDate;

    public static PickDateDialog newDialog(Date date) {
        PickDateDialog dialog = new PickDateDialog();
        dialog.mDate = date;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);

        DatePicker picker = new DatePicker(getActivity());
        picker.init(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(picker)
                .setPositiveButton("OK", (dialog, which) -> setDate(picker.getYear(),picker.getMonth(),picker.getDayOfMonth()))
                .setNegativeButton("Отмена", (dialog, which) -> setDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        );
        return builder.create();
    }

    private void setDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

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

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }
}
