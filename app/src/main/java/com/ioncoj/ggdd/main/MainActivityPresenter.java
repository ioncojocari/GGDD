package com.ioncoj.ggdd.main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.ggdd.initial.InitialActivity;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {
    private static final String TAG=MainActivityPresenter.class.getCanonicalName();
    private IDao dao;
    private Context context;
    private MainActivityView view;
    private Observable<ActivityEvent> lifecycle;
    private FirebaseUser user;
    public MainActivityPresenter( IDao dao, Context context, MainActivityView view,Observable<ActivityEvent> lifecycle){
        this.dao=dao;
        this.context=context;
        this.view=view;
        this.lifecycle=lifecycle;
    };

    public void init(){
        initLoginStatus();
    }

    public void initLoginStatus(){
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(getUser()==null){
                    startActivityForGettingData();
                }else{
                    displayUserInfo();
                    showLatestUserInfo();
                }
            }
        });
    }

    private void showLatestUserInfo() {
        if(getUser()!=null)
        dao.getUserInfoUpdate()
                .compose(RxLifecycleAndroid.bindActivity(lifecycle))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(info->{
                    view.display(info);
                });
    }

    private void displayUserInfo(){
        dao.getUserInfo()
                .compose(RxLifecycleAndroid.bindActivity(lifecycle))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(info->{
                    view.updateUserUi(info);
                });
    }

    private void startActivityForGettingData(){
        Intent intent=new Intent(context, InitialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    };

    public void login() {
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            startActivityForGettingData();
        }
    }
}
