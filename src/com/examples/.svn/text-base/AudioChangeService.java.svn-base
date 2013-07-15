package com.examples;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

public class AudioChangeService extends Service {
	public static final String LOG_TAG = "finalProjectService";
	private static final long dayMillis = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); //gives 86400000
	private AudioManager manager;
	private AlarmManager alarm;
	private long alarmTime;	
	private PendingIntent pIntent;
	private CalendarDBHandler db;	
	private boolean changeRingBack = true;
	public static int ringMode; //TODO: change from main.java
	
	@Override
	public void onCreate() {
		Log.i(LOG_TAG, "service ready at time: " + System.currentTimeMillis());

		manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		Intent serviceIntent = new Intent(this, AudioChangeService.class);
		serviceIntent.putExtra("prevRingMode", manager.getRingerMode());
		
		/* DB STUFF: */ 
		Log.i("DBHANDLER", "trying to make new DB"); 
		db = new CalendarDBHandler(this);
		
		
		List<Event> dbEvents = new ArrayList<Event>();
		Calendar cal1 = Calendar.getInstance(Locale.getDefault());
		cal1.set(Calendar.HOUR_OF_DAY, 0);//necessary for setting service to go off as soon as tomorrow hits
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Log.i(LOG_TAG, "date query: " + Event.getDateKey(cal1));
		int DOW = cal1.get(Calendar.DAY_OF_WEEK)-1;
		
		dbEvents = db.getEventsOn(Event.getDateKey(cal1));
		dbEvents.addAll( db.getEventsOnDOW(DOW) );
		Collections.sort(dbEvents);
		
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		if( dbEvents.isEmpty() ){
			alarmTime = cal1.getTimeInMillis() + dayMillis;
			Log.i(LOG_TAG, "setting alarm to start service tomorrow, no events today");
		}else{
			Event curr = nextEvent(dbEvents, System.currentTimeMillis());
			if( curr == null ){
				alarmTime = cal1.getTimeInMillis() + dayMillis;
				Log.i(LOG_TAG, "setting alarm to start service tomorrow (no more events today): " + alarmTime);
			}else{
				if( Math.abs(System.currentTimeMillis() - curr.getStart()) < 2000){
					manager.setRingerMode(curr.getRingMode());
					changeRingBack = false;
					alarmTime = curr.getEnd();
					Log.i(LOG_TAG, "setting alarm to start service when this event ends " + alarmTime);
				}else{
					alarmTime = curr.getStart();
					Log.i(LOG_TAG, curr.toString() );
					Log.i(LOG_TAG, "setting alarm to start service when this event starts: " + alarmTime);
				}
			}
		}
		
		pIntent = PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);	
		//use Date.getTime() to get UTC from a date. If you adjust phone settings alarm wont go off tile phone utc time is hit
		alarm.set(AlarmManager.RTC, alarmTime, pIntent);
		stopSelf();		
		
	}
		
		
	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "AudioChangeService for ScheduleSilence stopped");
		//stopSelf(0);
		if( db != null){
			db.close();
		}
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		//super.onStart(intent, startId);
	}

	//method called when startService is called from another component
	//idea is to have startService called every time event is added,
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "running service called with startService from other app at time: " + System.currentTimeMillis());
		if( changeRingBack ){
			//manager.setRingerMode(intent.getIntExtra("prevRingMode", AudioManager.RINGER_MODE_NORMAL));
		}
		return START_STICKY;
	}

	public IBinder onBind(Intent intent) {
        return null; //don't provide binding so return null
    }
	
	//This method returns the event which is next, as defined as the event with the lowest start time that is still
	//greater than the currTime. Assumes eventList is sorted in ascending order. Returns null if no event is after currTime
	public Event nextEvent(List<Event> eventList, long currTime){
		for( Event event : eventList ){
			if( event.getStart() >= currTime - 2000 ){
				return event;
			}
		}
		return null;		
	}
}