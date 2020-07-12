package com.nathanael.pierregignoux.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


public class DatepickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    // This is the listener for the activity
    private DatePickerFragmentListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
// Get the current date to start the DatePicker with
        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        listener.onDateSet(year, month, dayOfMonth);
    }
    // This is the method from the DatePicker fragment to implement in the Main Activity
    public interface DatePickerFragmentListener{
        public void onDateSet(int year, int month, int day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DatePickerFragmentListener) context;
    }
}