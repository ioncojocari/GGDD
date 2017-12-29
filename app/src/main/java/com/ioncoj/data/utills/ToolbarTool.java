package com.ioncoj.data.utills;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.ggdd.R;

import static android.content.ContentValues.TAG;

public class ToolbarTool {
    private FirebaseAuth auth;
    private Runnable showEveryDay;
    public ToolbarTool(FirebaseAuth auth,Runnable showEveryDay){
        this.showEveryDay=showEveryDay;
        this.auth=auth;
    }

    private void logout(){
        if(auth!=null&&auth.getCurrentUser()!=null){
            if(BuildConfig.DEBUG)Log.d(TAG, "logout: ");
            auth.signOut();
        }
    }

    private void startEveryDay(){
        showEveryDay.run();
    };

    public int getMenuResource(){
        return R.menu.main;
    }

    public boolean handleAction(int id){
        switch (id){
            case R.id.logout:logout();
            return true;
            case R.id.everyDay:startEveryDay();
            return true;
        }
        return false;
    }
}
