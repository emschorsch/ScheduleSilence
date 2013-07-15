package com.examples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.text.format.DateFormat;

public class Event implements Serializable, Comparable<Event>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static boolean is24hours = true;
	public static final Calendar timeConverter = Calendar.getInstance();
	
	public long start, end;
	public ArrayList<Boolean> daysOfWeek;
	public String name; 
	public long Id; //should be time event was created
	public long date; //the day of the start with hour 0 and minute 0
	public Calendar cal; 
	public int ringMode; 
	public int startHour; 
	public int startMinute; 
	
	public String[] ringModes = {"Silent", "Vibrate", "Ringer"}; 
	
	public Event(long time){
		this.Id = time; 
	}
	
	public Event(){
		Calendar now = Calendar.getInstance(Locale.getDefault()); 
		setDate(now);
		daysOfWeek = new ArrayList<Boolean>(); 
		
		for(int i =0; i<7;i++){
			daysOfWeek.add(false); 
		}
		
		this.Id = now.getTimeInMillis();
	}
	
	public void setId(long time){
		this.Id = time; 
	}
	public void setCalendar(Calendar cal){
		this.cal = cal; 
		setDate(cal);
	}
	
	public void setStart( int hour, int minute ){
		Calendar cal1 = (Calendar) this.cal.clone();
		cal1.set(Calendar.MILLISECOND, 0);
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, minute);
		this.start = cal1.getTimeInMillis();
		this.startHour = hour;
		this.startMinute = minute; 

	}
	
	public void setStart( long start ){
		this.start = start;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getStart()); 
		this.startHour = cal.get(Calendar.HOUR_OF_DAY); //TODO: do we want 24 hour or 12 hour?
		this.startMinute = cal.get(Calendar.MINUTE);
	}
	
	public void setEnd( long end ){
		this.end = end;
	}
	
	public void setDate(Calendar cal){
		this.cal = cal; 
		this.date = getDateKey(cal);
	}
	public void setDate(Long date){
		this.date = date; 
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date); 
		this.cal = cal; 
	}
	
	public void setEnd( int hour, int minute ){
		Calendar cal1 = (Calendar) this.cal.clone(); 
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, minute);
		cal1.set(Calendar.MILLISECOND, 0);
		this.end = cal1.getTimeInMillis();
		if( this.end < this.start ){ //means end should be on next day
			this.end += TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); //gives 86400000
		}
	}
	public void setName(String name){
		this.name = name; 
	}
	
	public void setDays( ArrayList<Boolean> daysOfWeek ){
		if( daysOfWeek.size() != 7){
			throw new IllegalArgumentException();
		}
		this.daysOfWeek = daysOfWeek;
	}

	public String getName() {
		return this.name;
	}

	public long getId() {
		return this.Id;
	}
	public long getDate(){
		return this.date; 
	}
	
	public long getStart(){
		return this.start;
	}
	
	public long getEnd(){
		return this.end;
	}
	
	public void setRingMode(int mode){
		this.ringMode = mode;
	}
	
	public int getRingMode(){
		return this.ringMode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getStart()); 
		int start_hour = cal.get(Calendar.HOUR_OF_DAY); //TODO: do we want 24 hour or 12 hour?
		int start_minute = cal.get(Calendar.MINUTE);

		cal.setTimeInMillis(getEnd()); 
		int end_hour = cal.get(Calendar.HOUR_OF_DAY);
		int end_minute = cal.get(Calendar.MINUTE);
		
		String summary = getName() + " " + timeString(start_hour, start_minute) + " - " + 
								timeString(end_hour, end_minute) + "  (" + ringModes[ringMode] + ")";
		//summary += ", startTime: "+start_hour +":"+ start_string+" endTime: "+end_hour +":"+ end_string; 
		return summary;
	}
	
	public String timeString( int hour, int min ){
		String minute = Integer.toString(min); 
		if(min<10){
			minute = "0"+Integer.toString(min); 
		}
		
		if( is24hours ){
			return hour + ":" + minute;
		}else{
			timeConverter.set(Calendar.HOUR_OF_DAY, hour);	
			if( timeConverter.get(Calendar.HOUR) == 0 ){
				return 12 + ":" + minute + " "+((timeConverter.get(Calendar.AM_PM) == 1) ? "PM": "AM");
			}
			return timeConverter.get(Calendar.HOUR) + ":" + minute + " "+((timeConverter.get(Calendar.AM_PM) == 1) ? "PM": "AM");
		}
	}

	public static long getDateKey(Calendar cal){
		String key = "" + cal.get(Calendar.YEAR);
		while(key.length() < 4){
			key = "0" + key;
		}
		if(cal.get(Calendar.MONTH) < 10){
			key += "0" + cal.get(Calendar.MONTH);
		}else{
			key += cal.get(Calendar.MONTH);
		}
		if(cal.get(Calendar.DAY_OF_MONTH) < 10){
			key += "0" + cal.get(Calendar.DAY_OF_MONTH);
		}else{
			key += cal.get(Calendar.DAY_OF_MONTH);
		}
		return Long.parseLong(key);
	}

	@Override
	public int compareTo(Event arg0) {
		//long compStart = arg0.getStart();
		int compStartHour = arg0.startHour; 
		int compStartMinute = arg0.startMinute; 
		// TODO Auto-generated method stub
		if(compStartHour > this.startHour){
			return -1; 
		}
		else if(compStartHour == this.startHour){
			if(compStartMinute > this.startMinute){
				return -1; 
			}
			else{
				return 1; 
			}
		}
		else{
			return 1; 
		}
	}
}