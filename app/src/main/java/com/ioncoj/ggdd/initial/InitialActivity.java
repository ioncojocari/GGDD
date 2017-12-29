package com.ioncoj.ggdd.initial;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.data.utills.InternetCallback;
import com.ioncoj.data.utills.InternetReceiver;
import com.ioncoj.ggdd.R;
import com.ioncoj.data.utills.Utills;
import com.ioncoj.ggdd.databinding.ActivityInitialBinding;
import com.ioncoj.ggdd.main.MainActivity;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO make this activity to request login and download data or update if there isn't necessary data to display;
//when data will finally be collected then i should redirect to MainActivity; and also i need to kill all from backstack;
public class InitialActivity extends RxAppCompatActivity {
    private static final String TAG=InitialActivity.class.getCanonicalName();
    private static final int RC_SIGN_IN=123;
    private static final String CHECK_LOGIN="check login ";
    private Utills utills;
    private final static String LOGIN="login" ;
    private boolean login=false;
    private BroadcastReceiver internetReciever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildConfig.DEBUG)Log.d(TAG,"activity creted");
        super.onCreate(savedInstanceState);
        ActivityInitialBinding binding=DataBindingUtil.setContentView(this,R.layout.activity_initial);
        binding.setId(R.drawable.init);
        init(savedInstanceState);
        registerReceiver();
        //TODO try to clear all prev activity from backstac;
    }

    @OnClick(R.id.loginButton)
    public void login(View view){
        checkLogin();
    }

    private void init(Bundle savedInstanceState){
        ButterKnife.bind(this);
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(LOGIN)){
                login=savedInstanceState.getBoolean(LOGIN);
            }
        }
        internetReciever=new InternetReceiver();
        utills=new Utills(getBaseContext());

    }

    private void checkLogin(){
        if(BuildConfig.DEBUG)Log.d(TAG,"check login");
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user==null){
            login=false;
            startLoginActivityForResult();
        }else{
            login=true;
        }
    }
    //TODO bug if not internet and first time tried to start app; login show mulitple times and downloanding isn't ending;
    private void startLoginActivityForResult(){
        if(BuildConfig.DEBUG)Log.d(TAG,"starting firebase ui");
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(BuildConfig.DEBUG)Log.d(TAG,"onActivityResult");
        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_OK){
                login=true;
                tryToExit();
            }else{
                if(BuildConfig.DEBUG)Log.d(TAG,"login failed");
                InternetCallback callback = new InternetCallback() {
                    @Override
                    public boolean isInternet() {
                        return true;
                    }

                    @Override
                    public void run() {
                        checkLogin();
                        if(BuildConfig.DEBUG)Log.d(TAG,"running check login");
                    }
                };
                if(!utills.isInternet()) {
                    authRequiredMessage();
                    InternetReceiver.addListeners(CHECK_LOGIN, callback);
                }
                //TODO I have a bug or firebase it logges in //but is called again ///doesn't disappear then once i end it InitialActivity show downloading data//
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private void registerReceiver(){
        registerReceiver(internetReciever,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(internetReciever);
    }

    public void authRequiredMessage() {
        String message="Please connect to internet";
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void tryToExit(){
        if(login){
            //TODO go to MainAcitivty;
           if(BuildConfig.DEBUG)Log.d(TAG,"starting main activity");
            Intent intent=new Intent(this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOGIN,login);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
