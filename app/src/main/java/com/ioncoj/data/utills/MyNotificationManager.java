package com.ioncoj.data.utills;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ioncoj.data.models.Task;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.ggdd.R;
import com.ioncoj.ggdd.main.MainActivity;
//TODO verify if there is any task for today then show notification
public class MyNotificationManager {
    private Context context;
    private static final String TAG=MyNotificationManager.class.getCanonicalName();
    private static final int mNotificationId=24123;
    public MyNotificationManager(Context context){
        this.context=context;
    }

    public void pushNotificationTask(Task task){
                String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle("Today's recommendation")
                        .setAutoCancel(true)
                        .setContentText(task.getTitle());
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle=new Bundle();
        bundle.putParcelable(Task.OBJECT,task);
        if(BuildConfig.DEBUG)if(BuildConfig.DEBUG)Log.d(TAG,"adding task to intent,task:"+task.toString());
        resultIntent.putExtras(bundle);
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to MyNotificationManager.cancel().
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

}
