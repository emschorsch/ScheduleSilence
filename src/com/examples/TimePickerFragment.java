package com.examples;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	public static final String LOG_TAG = "timePickerFragment";
	int hour;
	int min;
	int defaultHour = -1;
	int defaultMin = -1;
	Time t;
	
	public TimePickerFragment(){
		final Calendar c = Calendar.getInstance();
		defaultHour = c.get(Calendar.HOUR_OF_DAY);
		defaultMin = c.get(Calendar.MINUTE);
	} 

	public Dialog onCreateDialog(Bundle savedInstanceState) {		
		return new TimePickerDialog(getActivity(), (OnTimeSetListener) getActivity(), defaultHour, defaultMin, 
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		hour = hourOfDay;
		min = minute;
		//getActivity().findViewById(R.id.StartText);
		Log.i(LOG_TAG, "HOUR :" + hourOfDay + "MINUTE :" + minute);
	}

	public int getHour() {
		return hour;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getDefaultHour() {
		return defaultHour;
	}
	
	public int getDefaultMin() {
		return defaultMin;
	}
}
