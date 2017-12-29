package com.ioncoj.data.utills;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.ioncoj.ggdd.BuildConfig;

import java.util.Calendar;
import java.util.TimeZone;

public class MyAlarmManager {
    private Context context;
    public MyAlarmManager(Context context){
        this.context=context;
    }

    public void startAlarmAtHourEveryDay(int hour){
       if(BuildConfig.DEBUG)Log.d("MyAlarmManager","startAlarmAtHour :"+hour);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);

// With setInexactRepeating(), you have to use one of the AlarmManager interval
// constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
//        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime()+61*1000, 61*1000,
//                alarmIntent);
    }

    public void setAlarmOnceAtHour(int hour){
        Intent intent = new Intent(context, NotificationReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmUp) {
            if(BuildConfig.DEBUG)Log.d("MyAlarmManager","setting alarm:");
            startAlarmAtHourEveryDay(hour);
        }
        if(BuildConfig.DEBUG)Log.d("MyAlarmManager","alarm is set:"+alarmUp);
    }
}
