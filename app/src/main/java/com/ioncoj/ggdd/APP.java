package com.ioncoj.ggdd;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ioncoj.data.repositories.FirebaseDao;
import com.ioncoj.data.utills.MyAlarmManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class APP extends Application {
    private int NOTIFICATION_HOUR=18;
    private DatabaseReference accountRef;
    private FirebaseDatabase database;
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG)Log.d("APP", "onCreate: ");
        initFirebase();
        setFirebasePersistenceStorage();
        setAlarms();
        keepAccountSync();

    }
    //TODO set alarms for every day;
    private void setAlarms() {
        MyAlarmManager manager=new MyAlarmManager(this);
        manager.setAlarmOnceAtHour(NOTIFICATION_HOUR);
    }

    private void initFirebase(){
        FirebaseApp.initializeApp(this);
        database=FirebaseDatabase.getInstance();
    }

    private void setFirebasePersistenceStorage(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private void keepAccountSync(){
        FirebaseDao repo=new FirebaseDao();
        repo.keepTaskInMemorry().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    if(accountRef!=null)
                    accountRef.keepSynced(false);
                    accountRef=null;
                }else{
                    if(accountRef!=null){
                        accountRef.keepSynced(false);
                    }
                    accountRef=database.getReference("/users/"+firebaseAuth.getUid()+"/info");
                    accountRef.keepSynced(true);
                }
            }
        });
    };

}
