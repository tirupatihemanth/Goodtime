/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.BL;

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.WORK_DURATION;
import static com.apps.adrcotfas.goodtime.Util.Constants.DEFAULT_WORK_DURATION_DEFAULT;

/**
 * Maintains a global state of the app and the {@link CurrentSession}
 */
public class GoodtimeApplication extends Application implements LifecycleObserver {

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSessionManager mCurrentSessionManager;
    private static SharedPreferences mPreferences;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    static { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES); }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        INSTANCE = this;

        mPreferences = getSharedPreferences(getPackageName() + "_private_preferences", MODE_PRIVATE);
        PreferenceHelper.migratePreferences();

        mCurrentSessionManager = new CurrentSessionManager(this, new CurrentSession(TimeUnit.MINUTES.toMillis(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(WORK_DURATION, DEFAULT_WORK_DURATION_DEFAULT))));

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackground() {
        final CurrentSession currentSession = getCurrentSession();
        if(currentSession.getSessionType().getValue() == SessionType.WORK
            && currentSession.getTimerState().getValue() == TimerState.ACTIVE) {
            (new NotificationHelper(getApplicationContext())).notifyFocusLost();
                mCurrentSessionManager.scheduleFocusLostAlarm();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForeground() {
        // Each time the app starts we check if there is any alarm out from the previous exit of a work session.
        // Neverthless calling cancel everytime. If there isn't any alarm then nothing happens
        getCurrentSessionManager().cancelFocusLostAlarm();
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSessionManager.getCurrentSession();
    }

    public static CurrentSessionManager getCurrentSessionManager() {
        return mCurrentSessionManager;
    }

    public static SharedPreferences getPrivatePreferences() {
        return mPreferences;
    }
}