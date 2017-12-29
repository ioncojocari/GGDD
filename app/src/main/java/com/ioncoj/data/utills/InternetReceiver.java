package com.ioncoj.data.utills;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ioncoj.data.utills.InternetCallback;

import java.util.Map;
import java.util.WeakHashMap;

public class InternetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent == null || intent.getExtras() == null)
            return;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager!=null) {
            NetworkInfo ni = manager.getActiveNetworkInfo();
            Boolean connected = null;
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                connected = false;
            }
            if (connected != null) {
                notifyStateToAll(connected);
            }
        }
    }

    public static void notifyStateToAll(boolean connect){
        synchronized (lock){
        for (String key:callbackMap.keySet()){
            InternetCallback callback=callbackMap.get(key);
            if (callback != null && callback.isInternet() == connect) {
                callback.run();
                callbackMap.remove(key);
            }
        }
        }
    }

    public static void addListeners(String key,InternetCallback callback){
        synchronized (lock){
            callbackMap.put(key,callback);
        }
    };

    public static void removeListener(String key){
        synchronized (lock){
            if(callbackMap.containsKey(key)) {
                callbackMap.remove(key);
            }
        }
    };

    private static Map<String,InternetCallback> callbackMap;
    private final static Object lock;
    static{
        callbackMap=new WeakHashMap<String,InternetCallback>();
        lock=new Object();
    }
}
