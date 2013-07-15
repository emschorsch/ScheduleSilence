package com.examples;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CalendarPage extends FragmentActivity implements OnTimeSetListener{
	public static final String LOG_TAG = "calendarPage";
	EditText et;
	int startHour = -1, startMin = -1, endHour = -1, endMin = -1, ringerMode;
	boolean[] daysOfWeek;
	ArrayList<Integer> time;
	String startTimeString, endTimeString, eventName, checkRinger;
	ArrayList<CheckBox> weekTable;
	ArrayList<Boolean> checks;		
	
	CheckBox monday, tuesday, wednesday, thursday, friday, saturday, sunday;
	Spinner spinner;
	TimePickerFragment newFragment;
	TimePickerFragment newFragment1;
	TextView startTime, endTime;
	boolean startTimeSelected = true; //true if startTimeSelected false if endTimeSelected
	boolean startTimeSet = false, endTimeSet = false, checkAll = false;
	private final Calendar timeConverter = Calendar.getInstance();
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.calendar_event);
        et = (EditText) findViewById(R.id.EventName);
        spinner = (Spinner) findViewById(R.id.ringer_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
             R.array.ringer_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        weekTable = new ArrayList<CheckBox>();
        weekTable.add(sunday = (CheckBox) findViewById(R.id.checkbox_sunday));
        weekTable.add(monday = (CheckBox) findViewById(R.id.checkbox_monday));
        weekTable.add(tuesday = (CheckBox) findViewById(R.id.checkbox_tuesday));
        weekTable.add(wednesday = (CheckBox) findViewById(R.id.checkbox_wednesday));
        weekTable.add(thursday = (CheckBox) findViewById(R.id.checkbox_thursday));
        weekTable.add(friday = (CheckBox) findViewById(R.id.checkbox_friday));
        weekTable.add(saturday = (CheckBox) findViewById(R.id.checkbox_saturday));
        
        checks = new ArrayList<Boolean>();
        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);

        int count = 0;
        for( CheckBox day : weekTable ){
        	checks.add(false);
        	day.setTag(count);
        	count++;
        }
        
	}
	
	//when startTime is clicked
	public void showTimePickerDialog(View v) {
	    newFragment = new TimePickerFragment();
	    if( startHour != -1 ){
	    	newFragment.defaultHour = startHour;
	    	newFragment.defaultMin = startMin;
	    }
	    newFragment.show(getSupportFragmentManager(), "startTimePicker");
	    startTimeSelected = true;
	}
	
	public void showEndTimePickerDialog(View v) {
	    newFragment1 = new TimePickerFragment();
	    if( endHour != -1 ){
	    	newFragment1.defaultHour = endHour;
	    	newFragment1.defaultMin = endMin;
	    }
	    newFragment1.show(getSupportFragmentManager(), "endTimePicker");	    
	    startTimeSelected = false;
	}
	
	public String timeString( int hour, int min ){
		String minute = Integer.toString(min); 
		if(min<10){
			minute = "0"+Integer.toString(min); 
		}
		
		if( DateFormat.is24HourFormat(this) ){
			return hour + ":" + minute;
		}else{
			timeConverter.set(Calendar.HOUR_OF_DAY, hour);	
			if( timeConverter.get(Calendar.HOUR) == 0 ){
				return 12 + ":" + minute + " "+((timeConverter.get(Calendar.AM_PM) == 1) ? "PM": "AM");
			}
			return timeConverter.get(Calendar.HOUR) + ":" + minute + " "+((timeConverter.get(Calendar.AM_PM) == 1) ? "PM": "AM");
		}
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hour, int minute) {
		if( startTimeSelected ){
			startHour = hour;
			startMin = minute;
			startTimeSet = true;
			startTime.setText(timeString(startHour, startMin));
		}else{
			endHour = hour;
			endMin = minute;
			endTimeSet = true;
			endTime.setText(timeString(endHour, endMin));
		}		
	}
	
	public void addEvent(View v) {
		Intent intent = getIntent();
		Calendar date = (Calendar) intent.getExtras().get("calendar"); 
		Event e = new Event();
		e.setCalendar(date); 
		e.name = et.getText().toString();
		if( e.name.isEmpty() ){
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Missing name");
			alertDialog.setMessage("Please add a name for the event");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {			 
					et.requestFocus();	 
				} 
			}); 
			alertDialog.show();
			return;
		}
		
		date.setTimeInMillis( System.currentTimeMillis() );
		//TODO: do we want to not allow no start and end time selection?
		if ( !startTimeSet ) { 
			date.add(Calendar.MINUTE, 3);
			startHour = date.get(Calendar.HOUR_OF_DAY);
			startMin = date.get(Calendar.MINUTE);
			Toast.makeText(this, "start time not set: default is 3 minutes from now", Toast.LENGTH_SHORT).show();
		}
		if ( !endTimeSet ) {
			date.set(Calendar.HOUR_OF_DAY, startHour);
			date.set(Calendar.MINUTE, startMin);
			date.add(Calendar.MINUTE, 30);
			endHour = date.get(Calendar.HOUR_OF_DAY);
			endMin = date.get(Calendar.MINUTE);
			Toast.makeText(this, "end time not set: default is 30 minutes after start time", Toast.LENGTH_SHORT).show();
		}
		
		e.setStart(startHour, startMin);
		e.setEnd(endHour, endMin);
		Log.i(LOG_TAG, "START TIME IS " + startHour + ":" + startMin);
		Log.i(LOG_TAG, "END TIME IS " + endHour + ":" + endMin);
		checkRinger = String.valueOf(spinner.getSelectedItem());
		
		if (checkRinger.equals("Silent")) {
			ringerMode = AudioManager.RINGER_MODE_SILENT;
		}
		else if (checkRinger.equals("Vibrate")) {
			ringerMode = AudioManager.RINGER_MODE_VIBRATE;
		}
		else {
			ringerMode = AudioManager.RINGER_MODE_NORMAL;
		}

		e.setDays(checks);
		Log.i(LOG_TAG, checks.toString()); 

		e.setRingMode(ringerMode);
		Log.i(LOG_TAG, "added event " +e);
		intent.putExtra("Event", e);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void onCheckboxClicked(View view) {
		CheckBox box = (CheckBox) view;
		checks.set( ((Integer)box.getTag()).intValue(), box.isChecked());		
	}
	
	public boolean[] getDays(ArrayList<Integer> list) {
		boolean[] checks = new boolean[7];
		if (list.size() != 7) {
			throw new IllegalArgumentException();
		}
		else {
			for (int i = 0; i < 7; i++) {
				checks[i] = (list.get(i) == 1);
			}
		}
		return checks;
	} 
	
	public void checkAllDays (View v) {
		checks.clear();
		for( CheckBox day : weekTable ){
			day.setChecked(true);
			checks.add( true );
		}
		Log.i(LOG_TAG, "Finished checking all");
	}
	
	public void cancelEvent(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
