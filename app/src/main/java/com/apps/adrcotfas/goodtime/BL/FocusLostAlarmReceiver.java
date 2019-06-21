package com.apps.adrcotfas.goodtime.BL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;
import de.greenrobot.event.EventBus;

public class FocusLostAlarmReceiver extends BroadcastReceiver {
	private final String TAG = AlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "FocusLostAlarm received i.e posting FocusLostEvent on EventBus");
		EventBus.getDefault().post(new Constants.FocusLostEvent());
	}
}
