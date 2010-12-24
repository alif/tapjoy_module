package com.tapjoy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class TapjoyReferralTracker extends BroadcastReceiver
{
	final String TJC_PREFERENCE = "tjcPrefrences";
	final String REFERRAL_URL = "InstallReferral";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i("TapjoyReferralTracker", "Traversing TapjoyReferralTracker Broadcast Receiver intent's info.......");

		String uri = intent.toURI();
		if( uri != null && uri.length() > 0 )
		{
			int index = uri.indexOf("referrer=");
			if( index > -1 )
			{
				uri = uri.substring(index, uri.length()-4);
				Log.i("TapjoyReferralTracker", "Referral URI: "+uri);

				SharedPreferences settings = context.getSharedPreferences(TJC_PREFERENCE, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(REFERRAL_URL, uri);//referralURL contains both referrer parameter and its value e.g. referralURL="referrer=com.tapjoy.tapX"
				editor.commit();
				Log.i("TapjoyReferralTracker", "Cached Referral URI: "+uri);
			}
			else
				Log.i("TapjoyReferralTracker", "No Referral URL.");
		}
		Log.i("TapjoyReferralTracker", "End");
	}
} 