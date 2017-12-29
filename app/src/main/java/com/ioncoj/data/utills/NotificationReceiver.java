package com.ioncoj.data.utills;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.FirebaseDao;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.ggdd.BuildConfig;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(BuildConfig.DEBUG)Log.d("NotificationReceiver", "onReceive: ");
        IDao dao=new FirebaseDao();
        dao.getTodayTask().subscribe(task->showNotification(task,context));
        //TODO get today task if there is any and show notification;
    }

    private void showNotification(Task task,Context context) {
        if(task!=null) {
            if(task.getDate()!=0) {
                MyNotificationManager notificationManager = new MyNotificationManager(context);
                notificationManager.pushNotificationTask(task);
            }
        }
    }
}
