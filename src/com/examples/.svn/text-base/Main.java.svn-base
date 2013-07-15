package com.examples;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Main extends Activity{

	private final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	private static final String LOG_TAG = "Calendar";
	private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 300;
    private static final int SWIPE_THRESHOLD_VELOCITY = 120;
	private TextView current, events;
	private GridView calendarView;
	private GridCellAdapter gridAdapter;
	private Calendar calendar, selectedCell; 
	private int month, year, day, curr_month, curr_year, curr_day;
	private ListView eventsView; 
	private Intent serviceIntent, addEventView, editEventView;
	private Event selectedEvent;
	CalendarDBHandler db; 

	private ArrayAdapter<Event> listAdapter; 
	ArrayList<Event> listEvents;
	Event event;
	AsyncTask<Void, List<Tuple<View,Integer>>, List<Tuple<View,Integer>>> loadEventIcons;
	private GestureDetectorCompat mDetector;
	View.OnTouchListener gestureListener;
	AlertDialog alertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //get the past one 
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		selectedCell = Calendar.getInstance(Locale.getDefault());
		selectedCell.set(Calendar.HOUR_OF_DAY, 0); 
		selectedCell.set(Calendar.MINUTE, 0); 
		selectedCell.set(Calendar.MILLISECOND, 0); 
		Event.is24hours = DateFormat.is24HourFormat(this);

		/* DB STUFF: */ 
		Log.i("DBHANDLER", "trying to make new DB"); 
		db = new CalendarDBHandler(this);
		//db.delete(); 
		//db.restart(); //if database structure modified need to call db.restart
		// dbTest(); 
		/* ----- */ 

		addEventView = new Intent(this, CalendarPage.class);
		editEventView = new Intent(this, EditEvent.class);
		serviceIntent = new Intent(this, AudioChangeService.class);

		calendar = Calendar.getInstance(Locale.getDefault()); //saved calendar 
		month = curr_month = calendar.get(Calendar.MONTH); //set to current 
		year = curr_year = calendar.get(Calendar.YEAR);
		day = curr_day = calendar.get(Calendar.DAY_OF_MONTH); 
		calendar.set(curr_year, curr_month, curr_day, 0, 0);

		current = (TextView) this.findViewById(R.id.currentMonth);
		current.setText(months[month] + " " + Integer.toString(year)); 

		events = (TextView) this.findViewById(R.id.Events);
		events.setText(calendar.getTime().toString()); 

		//days go into "month" via gridview 
		calendarView = (GridView) this.findViewById(R.id.calendar);

		//make adapter for calendar grid, give info so it can grab the calendar 
		gridAdapter = new GridCellAdapter(getApplicationContext(), R.id.curr_month, month, year, day);
		calendarView.setAdapter(gridAdapter);

		listEvents = new ArrayList<Event>(); //to be displayed in the listview 
		eventsView = (ListView) findViewById(R.id.list_view);			

		listAdapter = new ArrayAdapter<Event>(this, android.R.layout.simple_list_item_1,listEvents);

		eventsView.setAdapter(listAdapter); 
		eventsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				onEventClickForEdit(arg0, arg1, position, arg3);
			}
		});
		
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Delete/Modify event");
		alertDialog.setMessage("Which would you like to do?");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			 
				return;
			} 
		}); 
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Edit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				db.deleteEvent(selectedEvent); 	
				editEventView.putExtra("Event", selectedEvent);
				startActivityForResult(editEventView, 1);
				return;
			} 
		}); 
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				db.deleteEvent(selectedEvent); 				
				updateList(Event.getDateKey(selectedCell)); 
				if( selectedEvent.daysOfWeek.contains(true) ){
					loadEventIcons.cancel(true);
					loadEventIcons = new LoadEventIndicatorTask().execute(null,null,null);
				}else if( listAdapter.isEmpty() ){
					gridAdapter.currentCell.findViewById(R.id.event_icon).setVisibility(4);
				}
				return;
			} 
		}); 
		
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
		gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        };
        calendarView.setOnTouchListener(gestureListener);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	public void onDestroy(){
		if( db != null){
			db.close();
		}
		loadEventIcons.cancel(true);
		super.onDestroy();
	}

	public void onEventClickForEdit(AdapterView<?> arg0, View arg1, int position, long arg3){
		Log.i(LOG_TAG, position + " was clicked in the list View with value: " + arg0.getItemAtPosition(position)); 
		alertDialog.show();
		selectedEvent = (Event) arg0.getItemAtPosition(position);
	}

	public void addClick(View v){
		Log.i(LOG_TAG, "date before adding event: " + selectedCell.getTimeInMillis()); 
		addEventView.putExtra("calendar", selectedCell); 
		startActivityForResult(addEventView, 1);		
	}

	public void nextClick(View v){
		loadEventIcons.cancel(true);
		selectedCell.add(Calendar.MONTH, 1);
		year = selectedCell.get(Calendar.YEAR);
		month = selectedCell.get(Calendar.MONTH);
		current.setText(months[month] + " " + Integer.toString(year));

		Log.d(LOG_TAG, "After 1 MONTH " + "Month: " + month + " " + "Year: " + year);
		gridAdapter.setDate(year, month);
		gridAdapter.notifyDataSetChanged();
	}

	public void prevClick(View v){
		loadEventIcons.cancel(true);
		selectedCell.add(Calendar.MONTH, -1);
		year = selectedCell.get(Calendar.YEAR);
		month = selectedCell.get(Calendar.MONTH);
		current.setText(months[month] + " " + Integer.toString(year)); 

		Log.d(LOG_TAG, "Before 1 MONTH " + "Month: " + month + " " + "Year: " + year);
		gridAdapter.setDate(year, month);
		
		gridAdapter.notifyDataSetChanged();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){      
				event = (Event) data.getSerializableExtra("Event");
				db.addEvent(event);

				updateList(Event.getDateKey(selectedCell));
				startService(serviceIntent);
				
				if( event.daysOfWeek.contains(true) ){
					loadEventIcons.cancel(true);
					loadEventIcons = new LoadEventIndicatorTask().execute(null,null,null);
				}else{
					gridAdapter.currentCell.findViewById(R.id.event_icon).setVisibility(1);
				}
				
			}
			if (resultCode == RESULT_CANCELED) {    
				//Write your code on no result return 
			}
		}
	}
	public void dbTest(){
		/*
		 * TESTING THE DB: 
		 */
		Event event = new Event(2); 
		ArrayList<Boolean> days = new ArrayList<Boolean>();
		days.add(true);
		days.add(false);
		days.add(true);
		days.add(false);
		days.add(true);
		days.add(true);
		days.add(true);
	
		event.setCalendar(selectedCell); 
		event.setDays(days);
		event.setStart(9, 10); 
		event.setEnd(7,8); 
		event.setName("event 2"); 
		db.addEvent(event); 
		event.setId(3); 
		event.setName("event 3"); 
		
		ArrayList<Boolean> days2 = new ArrayList<Boolean>();
		days2.add(false);
		days2.add(false);
		days2.add(false);
		days2.add(false);
		days2.add(false);
		days2.add(false);
		days2.add(false);
		event.setDays(days2);
		db.addEvent(event); 

		List<Event> events = new ArrayList<Event>();
		events = db.getALLEvents(); 
		List<Event> events2 = new ArrayList<Event>();
		events2 = db.getDOW(); 
	
		
	}		

	public void updateList(long date){   
		//QUERY THE DB ON THE DATE, repopulate the LISTVIEW 
		/*DATABASE ACCESS: */ 
		List<Event> dbEvents = new ArrayList<Event>();
		List<Event> dbEventsRepeated= new ArrayList<Event>();

		dbEvents = db.getEventsOn(date);
		
		int DOW; 
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day); 
		DOW = selectedCell.get(Calendar.DAY_OF_WEEK) -1;
		dbEventsRepeated = db.getEventsOnDOW(DOW); 
		
		listAdapter.clear(); 
		dbEvents.addAll(dbEventsRepeated); 

		Collections.sort(dbEvents); //sort the events in ascending order by start time
		
		for(Event event:dbEvents){
			listAdapter.add(event); 
		}	

		eventsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				onEventClickForEdit(arg0, arg1, position, arg3);
			}
		});
	}

	//Adapter to populate the g
	public class GridCellAdapter extends BaseAdapter implements OnClickListener{
		private static final String tag = "GridCellAdapter";
		private final Context context;
		private final List<String> list;
		private int count=0;
		int daysInMonth, prevMonthDays;
		private int currentDayOfMonth;
		private Button gridcell;
		private Calendar calendar;
		private RelativeLayout currentCell = null; 

		public GridCellAdapter(Context context, int textViewResourceId, int month, int year, int day){
			super();
			this.context = context;
			this.list = new ArrayList<String>();

			Log.d(tag, "Month: " + month + " " + "Year: " + year);
			this.calendar = Calendar.getInstance(); 
			calendar.set(year,month,day);  

			currentDayOfMonth = day;

			monthDayList(month, year);
		}
		
		public void setDate(int year, int month){
			calendar.set(year, month, 1);
			monthDayList(month, year);
		}

		public String getItem(int position){
			return list.get(position);
		}

		@Override
		public int getCount(){
			return list.size();
		}

		private void monthDayList(int mm, int yy){
			list.clear();
			
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			int dayOne = calendar.get(Calendar.DAY_OF_WEEK)-1;
			
			daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			calendar.add(Calendar.MONTH, -1);
			int daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int prevMonth = calendar.get(Calendar.MONTH);
			calendar.add(Calendar.MONTH, 2);
			int nextMonth = calendar.get(Calendar.MONTH);
			calendar.add(Calendar.MONTH, -1);

			//days from previous month
			if(dayOne>0){
				int day = daysInPrevMonth - (dayOne-1);
				Log.i(LOG_TAG, "First day of" + month + "is: " + dayOne); 
				for(int i=0; i<dayOne; i++){
					if( prevMonth > mm ){
						list.add(Integer.toString(day) + "-prev" + "-" + months[prevMonth] + "-" + Integer.toString(yy-1));
					}else{
						list.add(Integer.toString(day) + "-prev" + "-" + months[prevMonth] + "-" + yy);
					}
					day++; 
				}
			}
			String color = "-curr";
			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++){ 
				if( i == curr_day && mm == curr_month && yy == curr_year ){
					list.add(Integer.toString(i) + "-today" + "-" + months[mm] + "-" + yy);
				}else{
					list.add(Integer.toString(i) + color + "-" + months[mm] + "-" + yy);
				}
			}

			//days for next month
			int remaining = 6-((dayOne + (daysInMonth-1))%7); 
			for (int i = 1; i <= remaining; i++){
				Log.d(tag, "NEXT MONTH:= " + months[nextMonth]);
				if( nextMonth < mm ){
					list.add(Integer.toString(i) + "-prev" + "-" + months[nextMonth] + "-" + Integer.toString(yy+1));
				}else{
					list.add(Integer.toString(i) + "-prev" + "-" + months[nextMonth] + "-" + yy);
				}
			}
		}

		@Override
		public long getItemId(int position){
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			//populates the "position" grid with the correct info/colors 
			Log.d(tag, "getView ...");
			View row = convertView;
			String[] day_color = list.get(position).split("-");
			String cellTag = day_color[0] + "-" + day_color[2] + "-" + day_color[3];
			RelativeLayout gridcell1;

			if (row == null){
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.gridcell, parent, false);
				Log.d(tag, "Successfully completed XML Row Inflation!");
				gridcell = (Button) row.findViewById(R.id.curr_month);
			}else{
				gridcell = (Button) row.findViewById(R.id.curr_month);				
				if( gridcell.getTag().equals(cellTag) ){
					gridcell1 = (RelativeLayout) gridcell.getParent();
					if( gridcell1.getTag().equals(day_color[1]) ){
						return row;
					}
				}
			}

			Log.d(tag, "Current Day: " + currentDayOfMonth);
			
			gridcell1 = (RelativeLayout) gridcell.getParent();
			gridcell1.setOnClickListener(this);
			gridcell.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					View parent = (View) view.getParent();						
					parent.performClick();
				}
			});
			gridcell1.setOnTouchListener(gestureListener);

			gridcell.setText(day_color[0]); //sets day#s to gridcells 
			gridcell.setTag( cellTag );
			gridcell1.setTag( day_color[1] ); //sets tag for relative view so know if its wrong background

			int day = Integer.parseInt(day_color[0]); 
			
			if( day_color[1].equals("prev") ){
				gridcell.setTextColor(Color.BLACK);
				gridcell1.setBackgroundResource(R.drawable.not_curr_month_selector);
			}else if( day_color[1].equals("today") ){
				gridcell1.setBackgroundResource(R.drawable.curr_month_selector);
				gridcell.setTextColor(Color.BLUE);
				events.setText( cellTag );
				gridcell1.performClick();
			}else{
				gridcell1.setBackgroundResource(R.drawable.curr_month_selector);
				gridcell.setTextColor(Color.BLACK);
				if(day == 1 && (month != curr_month || year != curr_year) ){
					events.setText( cellTag );
					gridcell1.performClick();
				}
			}	
			
			count++;
			if( count == list.size() ){
				loadEventIcons = new LoadEventIndicatorTask().execute(null,null,null);
				count=0;
			}
			return row;
		}

		@Override
		public void onClick(View view){   
			View child = view.findViewById(R.id.curr_month);
			String date_month_year = (String) child.getTag();
			Log.i(LOG_TAG, "in onClick: " + date_month_year);
			String[] values = date_month_year.split("-"); 
			events.setText(date_month_year);

			if( currentCell != null ){
				currentCell.setSelected(false);
			}
			currentCell = (RelativeLayout) view; 
			currentCell.setSelected(true);

			int day = Integer.parseInt(values[0]); 
			int year = Integer.parseInt(values[2]); 
			int month = 0; 
			for(int i=0; i<months.length; i++){
				if(values[1].equals(months[i])){
					month = i; 
				}
			}

			selectedCell.set(year, month, day);			
			updateList(Event.getDateKey(selectedCell));	
		}	
	}
	
	public class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
	} 

	private class LoadEventIndicatorTask extends AsyncTask<Void, List<Tuple<View,Integer>>, List<Tuple<View, Integer>>>{
		
		@Override
		protected List<Tuple<View, Integer>> doInBackground(Void... arg0) {			
			List<Tuple<View, Integer>> validDays = new ArrayList<Tuple<View, Integer>>();
			if( db == null){
				db = new CalendarDBHandler(getBaseContext());
			}
			
			for(int i=0; i<calendarView.getCount(); i++){
				if( isCancelled() ){
					return validDays;
				}
				View grid = calendarView.getChildAt(i);
				String[] day_color = gridAdapter.list.get(i).split("-");
				Calendar cal1 = new GregorianCalendar(); 
				int day = Integer.parseInt(day_color[0]); 
				int year = Integer.parseInt(day_color[3]); 
				int month = 0; 
				for(int i1 =0; i1<months.length; i1++){
					if(day_color[2].equals(months[i1])){
						month = i1; 
					}
				}
				cal1.set(year, month, day, 0, 0);
				int DOW = cal1.get(Calendar.DAY_OF_WEEK)-1;
				
				try{
					if( db.getEventsOn(Event.getDateKey(cal1)).isEmpty() && db.getEventsOnDOW(DOW).isEmpty() ){
						validDays.add( new Tuple<View, Integer>(grid.findViewById(R.id.event_icon), 4) );
					}else{				
						validDays.add( new Tuple<View, Integer>(grid.findViewById(R.id.event_icon), 0) );					
					}
				}catch(NullPointerException e){ //necessary due to orientation changes which render references null
					Log.i(LOG_TAG, "error in aSyncTask, references null");
					validDays.clear();
					return validDays;
				}
			}
			return validDays;
		}
		
		protected void onProgressUpdate(List<View> validEventIcons){
			for( View eventIcon : validEventIcons){
				eventIcon.setVisibility(1);
			}
		}
		
		protected void onPostExecute(List<Tuple<View,Integer>> validEventIcons){
			for( Tuple<View,Integer> eventIcon : validEventIcons){
				eventIcon.x.setVisibility(eventIcon.y);
			}
		}
	}
	
	class MyGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent event) { 
			Log.i(LOG_TAG, "in onDown");
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if( e2 == null || e1 == null ){
				return true;
			}
			Log.i(LOG_TAG, "in onFling");
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
				return false;
			}
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				nextClick(calendarView);
			} 
			// left to right swipe
			else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				prevClick(calendarView);
			}
			return true;
		}
	}
}