package com.incorcadit.alarmer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.incorcadit.alarmer.AlarmFragment;

public class ChoiceDialog extends DialogFragment {
    private int mLayoutId;

    public static ChoiceDialog newDialog(@LayoutRes int layoutId) {
        ChoiceDialog dialog = new ChoiceDialog();
        dialog.mLayoutId = layoutId;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LinearLayout parent = (LinearLayout) LayoutInflater.from(getActivity()).inflate(mLayoutId,null);
        for (int i = 0; i < parent.getChildCount(); i++) {
            TextView option = (TextView) parent.getChildAt(i);
            option.setOnClickListener((v) -> {
                sendResult(Activity.RESULT_OK, option.getText().toString());
                dismiss();
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(parent);

        return builder.create();
    }

    private void sendResult(int resultCode, String choice) {
        Intent intent = new Intent();

        intent.putExtra(AlarmFragment.EXTRA_CHOICE,choice);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
