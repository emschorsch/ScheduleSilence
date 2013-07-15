package com.examples;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AtBootReceiver extends BroadcastReceiver {
	String LOG_TAG = "finalProjectService";
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
				Intent serviceIntent = new Intent(context, AudioChangeService.class);
				context.startService(serviceIntent);
				Log.i(LOG_TAG, "service started on bootup");
			}
		}
	}