package com.examples;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


//Class for database access, table creation, and queries 

public class CalendarDBHandler {
    public static final String LOG_TAG = "DBHANDLER"; 
	
    // Database Version
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "calendarDB"; 
    
    //Tables: 
    private static final String TABLE_ONCE = "oneTimeEvents";
    private static final String TABLE_REPEATED = "repeatedEvents";
    
    //oneTimeEvents Table Column names: 
    private static final String KEY_ID = "id";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final String KEY_NAME = "name"; 
    private static final String KEY_DATE = "date"; //Given the UTC, this is the date the start time is on 
    private static final String KEY_RINGER = "ringer"; 

    //repeatedEvents Table Column names: (in addition to all of the above) 
    private static final String[] KEY_DAYS = {"sun", "mon", "tues", "wed", "thur", "fri", "sat"}; 
    
    private SQLiteDatabase db; 
    private CalendarSQLiteOpenHelper helper;
    
    //application/context that is calling the db
    Context context;
    
    public CalendarDBHandler(Context context){
    	this.context = context; 	
    	helper = new CalendarSQLiteOpenHelper(context);
    	this.db = helper.getWritableDatabase();    	
    }
    
    public void close(){
    	helper.close();
    }
    
    //Adds an event to the database (decides which table based on DOW selected) 
    public void addEvent(Event event){
        Log.i(LOG_TAG, "Adding Event");
        
        //decide which table it goes into: 
    	ContentValues values = new ContentValues(); 
        ArrayList<Boolean> DOW = event.daysOfWeek; 

        if(DOW.contains(true)){
        	//go into repeated events table        	
        	values.put(KEY_ID, event.getId()); 
        	values.put(KEY_START, event.start); 
        	values.put(KEY_END, event.end); 
        	values.put(KEY_NAME,  event.getName());     
        	values.put(KEY_DATE, event.getDate()); 
        	values.put(KEY_RINGER, event.getRingMode()); 
        	
        	Boolean value; 
        	for(int i = 0; i<event.daysOfWeek.size(); i++){
        		value = event.daysOfWeek.get(i);
        		values.put(KEY_DAYS[i], value ? 1 : 0); //converts boolean to int: true goes to 1
        	}  	
        	try{
        		db.insert(TABLE_REPEATED, null, values); 
        		Log.i(LOG_TAG, "added event " + event.name + "into repeated!");    		
        	}
        	catch(Exception e){
        		Log.i(LOG_TAG, "error inserting into repeated: "+ e.getMessage()); 
        	}
        	//db.close(); 
            }
        
        else{ //goes into onetime event table
        	
    	values.put(KEY_ID, event.getId()); 
    	values.put(KEY_START, event.start); 
    	values.put(KEY_END, event.end); 
    	values.put(KEY_NAME,  event.getName());     
    	values.put(KEY_DATE, event.getDate()); 
    	values.put(KEY_RINGER, event.getRingMode()); 
    	
    	try{
    		db.insert(TABLE_ONCE, null, values); 
    	}
    	catch(Exception e){
    		Log.i(LOG_TAG, e.getMessage()); 
    	}
    	//db.close(); 
        }
    }
    
    //not used, tester function
    public Event getEvent(int id){ //returns the ONETIMEEVENT with this id   	
        Cursor cursor = db.query(TABLE_ONCE, new String[] { KEY_ID,
                KEY_START, KEY_END, KEY_NAME, KEY_DATE, KEY_RINGER}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        Log.i(LOG_TAG, "made query"); 
        long Id = cursor.getLong(0); 
      
        Event event = new Event(Id); 
        event.setId(cursor.getLong(0));
        event.setStart(cursor.getLong(1));
        event.setEnd(cursor.getLong(2));
        event.setName(cursor.getString(3));
        event.setDate(cursor.getLong(4)); 
        event.setRingMode(cursor.getInt(5));
        cursor.close();
        
        return event; 
    }
        
    //returns the closest event starting in the future (for the service to choose for the ringer switch)
    public Event getNextEvent(long key){ //need to pass in the current time    	
        	List<Event> events = new ArrayList<Event>();
        	
        	// Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_ONCE +" WHERE " + KEY_START + " > " + Long.toString(key) + " Order By " + KEY_START; 
            Cursor cursor = db.rawQuery(selectQuery, null);
            
            // looping through all rows and adding to list (you really just need the first one) 
            if (cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    event.setId(cursor.getLong(0));
                    event.setStart(cursor.getLong(1));
                    event.setEnd(cursor.getLong(2));
                    event.setName(cursor.getString(3));     
                    event.setDate(cursor.getLong(4));
                    event.setRingMode(cursor.getInt(5));
                    events.add(event);
                } while (cursor.moveToNext());
            }        
            cursor.close();
            //db.close();
         if(events.size()>0){
        	 return events.get(0); //return the top one (ordered by the start time and date) 
         }
            return null; 
        	
        }
  
    //return all the events with this key (tester function) 
    public List<Event> getEvents(int key){ 
    	List<Event> events = new ArrayList<Event>(); 
        Cursor cursor = db.query(TABLE_ONCE, new String[] { KEY_ID,
                KEY_START, KEY_END, KEY_NAME, KEY_DATE, KEY_RINGER}, KEY_ID + "=?",
                new String[] { String.valueOf(key) }, null, null, null, null);
        if (cursor == null){
        	Log.e(LOG_TAG, "cursor null in getEvents. db: " + db);
            return events;
        }
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(0));
                event.setStart(cursor.getLong(1));
                event.setEnd(cursor.getLong(2));
                event.setName(cursor.getString(3));     
                event.setDate(cursor.getLong(4));
                event.setRingMode(cursor.getInt(5)); 
                events.add(event);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        //db.close();
        return events;
    }

    //return all of the events on a specific day (one time events) 
    public List<Event> getEventsOn(long date){ //get events on that day 
    	List<Event> events = new ArrayList<Event>(); 
        Cursor cursor = db.query(TABLE_ONCE, new String[] { KEY_ID,
                KEY_START, KEY_END, KEY_NAME, KEY_DATE, KEY_RINGER}, KEY_DATE + "=?",
                new String[] { String.valueOf(date) }, null, null, KEY_NAME, null);
        Log.i(LOG_TAG, "trying to get event on date: " + date); 
        
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(0));
                event.setStart(cursor.getLong(1));
                event.setEnd(cursor.getLong(2));
                event.setName(cursor.getString(3));     
                event.setDate(cursor.getLong(4));
                event.setRingMode(cursor.getInt(5));
                events.add(event);
            } while (cursor.moveToNext());
        }    
        cursor.close();
        //db.close();
        return events;
    }

    //return all events on a specified day of week (0-6) 
    public List<Event> getEventsOnDOW(int day){
    	List<Event> events = new ArrayList<Event>(); 
    	ArrayList<Boolean> daysOfWeek =  new ArrayList<Boolean>(); 
    	String DOW = KEY_DAYS[day];
    	
    	String[] queryCols = {KEY_ID,
                KEY_START, KEY_END, KEY_NAME, KEY_DATE, KEY_RINGER, KEY_DAYS[0],  KEY_DAYS[1],  KEY_DAYS[2],  KEY_DAYS[3],  KEY_DAYS[4],  KEY_DAYS[5],  KEY_DAYS[6]}; 
    	
        Cursor cursor = db.query(TABLE_REPEATED, queryCols, DOW + "=?",
                new String[] { String.valueOf(1) }, null, null, KEY_NAME, null);
        
 
        Log.i(LOG_TAG, "trying to get event on a DOW: " + DOW); 
        
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(0));
                event.setStart(cursor.getLong(1));
                event.setEnd(cursor.getLong(2));
                event.setName(cursor.getString(3)); 
                //event.setDate(cursor.getInt(4));
                event.setRingMode(cursor.getInt(5));
               
                int value;
                for(int i = 6; i<13; i++){
                	value = cursor.getInt(i); 
                	if(value==1){
                		daysOfWeek.add(true);
                	}
                	else{
                		daysOfWeek.add(false);
                	}
                }
                event.daysOfWeek = daysOfWeek; 
                
                events.add(event);
            } while (cursor.moveToNext());
        }    
        cursor.close();
        //db.close();
        return events;
    }
    
    //get all events in the repeated table (tester function) 
    public List<Event> getDOW(){

    	ArrayList<Boolean> daysOfWeek =  new ArrayList<Boolean>(); 
    	Log.i(LOG_TAG, "Retrieving all events from REPEATED"); 
    	List<Event> events = new ArrayList<Event>();

    	// Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_REPEATED + " Order By " + KEY_START; 
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(0));
                event.setStart(cursor.getLong(1));
                event.setEnd(cursor.getLong(2));
                event.setName(cursor.getString(3));     
                //event.setDate(cursor.getInt(4));
                event.setRingMode(cursor.getInt(5));
                
                int value;
                for(int i = 6; i<13; i++){
                	value = cursor.getInt(i); 
                	if(value==1){
                		daysOfWeek.add(true);
                	}
                	else{
                		daysOfWeek.add(false);
                	}
                }
                
                event.daysOfWeek = daysOfWeek; 

                events.add(event);
                //TODO: add days of week 
            } while (cursor.moveToNext());
        }        
        cursor.close();
        //db.close();
     
        return events;
    	
    }
    
    //get all events in the onetime table (tester function) 
    public List<Event> getALLEvents(){
        Log.i(LOG_TAG, "Retrieving all events"); 
    	List<Event> events = new ArrayList<Event>();

    	// Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ONCE + " Order By " + KEY_START; 
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(0));
                event.setStart(cursor.getLong(1));
                event.setEnd(cursor.getLong(2));
                event.setName(cursor.getString(3));     
                event.setDate(cursor.getLong(4));
                event.setRingMode(cursor.getInt(5));
                events.add(event);
            } while (cursor.moveToNext());
        }        
        cursor.close();
        //db.close();
     
        return events;
    	
    }
   
    public void delete(){ //deletes all tables/entries 
    	Log.i(LOG_TAG, "deleting db entries"); 	
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONCE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPEATED);   
    }
    public void restart(){
        helper.onCreate(db); 
    }
    
    private class CalendarSQLiteOpenHelper extends SQLiteOpenHelper{
    	public CalendarSQLiteOpenHelper(Context context) {
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	}

    	// Creating Tables
    	@Override
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONCE);
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPEATED);
    		
    		//Declare/create tables here: 
    		Log.i(LOG_TAG, "Creating tables"); 
    		String CREATE_TABLE_ONCE = "CREATE TABLE " + TABLE_ONCE + "("
    				+ KEY_ID + " INTEGER PRIMARY KEY,"
    				+ KEY_START + " INTEGER," + KEY_END+ " INTEGER," + KEY_NAME + " TEXT," + KEY_DATE + " INTEGER," + KEY_RINGER + " INTEGER)";
    		
    		String CREATE_TABLE_REPEATED = "CREATE TABLE " + TABLE_REPEATED + "("
    				+ KEY_ID + " INTEGER PRIMARY KEY,"
    				+ KEY_START + " INTEGER," + KEY_END+ " INTEGER," + KEY_NAME + " TEXT," + KEY_DATE + " INTEGER," + KEY_RINGER + " INTEGER";
    		

        for (String day : KEY_DAYS){
        	CREATE_TABLE_REPEATED += (", " + day + " INTEGER"); 
        }
        CREATE_TABLE_REPEATED += ")"; 

    		try{ 
    			db.execSQL(CREATE_TABLE_ONCE);
    			Log.i(LOG_TAG, "Created table once!"); 
    			db.execSQL(CREATE_TABLE_REPEATED); 
    			Log.i(LOG_TAG, "created table repeated"); 
    		}
    		catch(SQLException e){ //TODO: changed from catching general exception
    			Log.i(LOG_TAG, "database create table error"); 
    		}

    	}

    	// Upgrading database
    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//called for any updates 

    		// Drop older table if existed
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONCE);
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPEATED);

    		Log.i((LOG_TAG), "Dropping tables"); 

    		// Create tables again
    		onCreate(db);
    	}
    }


	public void deleteEvent(Event editing) {
		String delete1 = "DELETE FROM " + TABLE_ONCE + " WHERE " + KEY_ID + " = " + editing.Id; 
		String delete2 = "DELETE FROM " + TABLE_REPEATED + " WHERE " + KEY_ID + " = " + editing.Id; 

        //SQLiteDatabase db = this.getWritableDatabase(); //TODO: WHY WRITABLE??
        db.execSQL(delete1); //, null);
        db.execSQL(delete2); //, null); 
       return; 
		
	}
}
