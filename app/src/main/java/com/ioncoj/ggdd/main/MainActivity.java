package com.ioncoj.ggdd.main;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.ioncoj.data.models.AccountInfo;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.FirebaseDao;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.data.utills.InfoChanged;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.data.models.MenuBundle;
import com.ioncoj.ggdd.R;
import com.ioncoj.ggdd.fragments.EveryDayTasksFragment;
import com.ioncoj.ggdd.fragments.MyFragment;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ioncoj.data.utills.ToolbarTool;
import com.ioncoj.ggdd.fragments.TaskFragment;
import com.ioncoj.ggdd.fragments.TodayTasksFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends RxAppCompatActivity implements MainActivityView {
    private static final String TAG =MainActivity.class.getCanonicalName(); ;
    private static final String TASK_BACKSTACK_NAME="TASK_BACKSTACK_NAME";
    private static final String TODAY_LIST_BACKSTACK_NAME="TODAY_LIST_BACKSTACK_NAME";
    private static final String EVERY_DAY_LIST_BACKSTACK_NAME="EVERY_DAY_LIST_BACKSTACK_NAME";
    private static final String EVERY_DAY="every day";
    private static final String TODAY="today";
    private static final String SHOW_TASK="show task";
    @Nullable
    @BindView(R.id.second_layout)
    protected FrameLayout secondFrame;
    @BindView(R.id.first_layout)
    protected FrameLayout firstFrame;
    @BindView(R.id.my_toolbar)
    protected Toolbar toolbar;
    private MenuItem co2View;
    private MenuItem h2oView;
    private MenuItem everyDayView;
    private MainActivityPresenter presenter;
    private ToolbarTool toolbarTool;
    private Task task;
    private IDao dao;
    private boolean everyDay=false;
    private boolean showTask=false;
    private boolean needToUpdateIcons=false;
    private double co2;
    private double h2o;
    private boolean cameFromNotification=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG)Log.d(TAG,"onCreate start");
        setContentView(R.layout.activity_main);
        initButterknife();
        init(savedInstanceState);
        presenter.login();
        if(BuildConfig.DEBUG)Log.d(TAG,"onCreate end");
    }

    @Override
    protected void onResume() {
        if(BuildConfig.DEBUG)Log.d(TAG,"onResume");
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            finishAffinity();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
       if(BuildConfig.DEBUG)Log.d(TAG,"önPause");
        super.onPause();
    }

    private void initButterknife() {
        ButterKnife.bind(this);
    }

    private void init(Bundle savedInstanceState){
        //TODO get intent and verify if there is Task object;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Task.OBJECT)) {
                task = savedInstanceState.getParcelable(Task.OBJECT);
            }
            if (savedInstanceState.containsKey(EVERY_DAY)) {
                everyDay = savedInstanceState.getBoolean(EVERY_DAY);
            }
            if (savedInstanceState.containsKey(SHOW_TASK)) {
                showTask = savedInstanceState.getBoolean(SHOW_TASK);
            }
        } else if (getIntent()!=null&&getIntent().getExtras()!=null&&getIntent().getExtras().containsKey(Task.OBJECT)) {
            task=getIntent().getExtras().getParcelable(Task.OBJECT);
            cameFromNotification=true;
            if(task!=null)
            if(BuildConfig.DEBUG)Log.d(TAG,"task received from intent,task:"+task.toString());
        }

        dao=new FirebaseDao(lifecycle(),ActivityEvent.DESTROY,FirebaseAuth.getInstance().getCurrentUser());
        toolbarTool=new ToolbarTool(FirebaseAuth.getInstance(), new Runnable() {
            @Override
            public void run() {
                showDifferentList();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        presenter=new MainActivityPresenter(dao,getBaseContext(),this,lifecycle());
        initFragments();
        presenter.init();

    }

    private void showDifferentList(){
        String text=everyDayView.getTitle().toString();
        if(BuildConfig.DEBUG)Log.d(TAG,"show Different List text:"+text);
        if(text.equalsIgnoreCase(EVERY_DAY)){
            everyDayView.setTitle(TODAY);
            putFragment(new EveryDayTasksFragment(),EveryDayTasksFragment.TAG, R.id.first_layout,true,EVERY_DAY_LIST_BACKSTACK_NAME);
            everyDay=true;
            showTask=false;
        }else if (text.equalsIgnoreCase(TODAY)){
            everyDayView.setTitle(EVERY_DAY);
            //kill all from fragments backstack ,show first one;
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack (null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            everyDay=false;
            //putFragment(new TodayTasksFragment(),TodayTasksFragment.TAG, R.id.first_layout,true,null);
            //i need to delete all fragments except todayTasks;
        }
    }

    private void initFragments(){
        if(secondFrame==null){
            initFragmentsForPhone();
        }else{
            //TODO show both; check if there is insert or generate;
            initFragmentsForTable();
        }
    }

    private void initFragmentsForPhone(){
        Fragment lastFragment;
        //TODO refactor to much code and it's not easy to understand
        if(cameFromNotification){
            putFragment(new TodayTasksFragment(),TaskFragment.TAG,R.id.first_layout,false,null);
            //putFragment(TaskFragment.newInstance(task),TaskFragment.TAG,R.id.first_layout,true,TASK_BACKSTACK_NAME);
        }else {
            //check if there is something in backstack;
            int count = getSupportFragmentManager().getBackStackEntryCount();
            String lastBackstack = null;
            if (count != 0) {
                lastBackstack = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            }

            if (lastBackstack == null || lastBackstack.equals(TODAY_LIST_BACKSTACK_NAME)) {
                //show today tasks;
                lastFragment = getSupportFragmentManager().findFragmentByTag(TodayTasksFragment.TAG);
                if (lastFragment == null) {
                    Log.d(TAG,"TODAY LIST null");
                    lastFragment = new TodayTasksFragment();
                }
                //TODO show
                putFragment(lastFragment, TodayTasksFragment.TAG, R.id.first_layout, false, null);
            } else if (lastBackstack.equals(EVERY_DAY_LIST_BACKSTACK_NAME)) {
                // show EveryDayList
                lastFragment = getSupportFragmentManager().findFragmentByTag(EveryDayTasksFragment.TAG);
                if (lastFragment == null) {
                    Log.d(TAG,"EveryDayTasksFragment null");
                    lastFragment = new EveryDayTasksFragment();
                }
                putFragment(lastFragment, EveryDayTasksFragment.TAG, R.id.first_layout, false, null);
            } else if (lastBackstack.equals(TASK_BACKSTACK_NAME)) {
                //show everything else;
                lastFragment = getSupportFragmentManager().findFragmentByTag(TaskFragment.TAG);
                if (lastFragment == null) {
                    Log.d(TAG,"TASK_BACKSTACK_NAME  null");
                    lastFragment = TaskFragment.newInstance(task);
                }
                putFragment(lastFragment, TaskFragment.TAG, R.id.first_layout, false, null);
            }
        }
    }

    private void initFragmentsForTable(){
        Fragment lastFragment;
        if(cameFromNotification){
            putFragment(new TodayTasksFragment(),TaskFragment.TAG,R.id.first_layout,false,null);
            putFragment(TaskFragment.newInstance(task),TaskFragment.TAG,R.id.second_layout,true,TASK_BACKSTACK_NAME);
        }else {

            int count = getSupportFragmentManager().getBackStackEntryCount();
            String lastBackstack = null;
            if (count != 0) {
                lastBackstack = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            }

            if (lastBackstack == null || lastBackstack.equals(TODAY_LIST_BACKSTACK_NAME)) {
                //show today tasks;
                lastFragment = getSupportFragmentManager().findFragmentByTag(TodayTasksFragment.TAG);
                if (lastFragment == null) {
                    lastFragment = new TodayTasksFragment();
                }
                putFragment(lastFragment, TodayTasksFragment.TAG, R.id.first_layout, false, null);
            }
        }
    }

    public void putFragment(Fragment fragment,String TAG,int id,boolean replace,String backstack,boolean push){
        Fragment prevFragment=null;
        if(!push){
            prevFragment=getSupportFragmentManager().findFragmentByTag(TAG);
        }
        if(prevFragment!=null){
            fragment=prevFragment;
            FragmentTransaction transaction=getSupportFragmentManager()
                    .beginTransaction();
                    transaction.replace(id,fragment,TAG);
                    if(backstack!=null&&replace) {
                        transaction.addToBackStack(backstack);
                    }
                    transaction.commit();
        }else{
            FragmentTransaction transaction=getSupportFragmentManager()
                    .beginTransaction();
                    if(!replace) {
                        transaction.add(id, fragment, TAG);
                    }else{
                        transaction.replace(id,fragment,TAG);
                    }
                    if(backstack!=null&&replace) {
                        transaction.addToBackStack(backstack);
                    }
                    transaction.commit();
        }
    }

    public void putFragment(Fragment fragment,String TAG,int id,boolean replace,String backstack){
        putFragment(fragment,TAG,id,replace,backstack,false);
    }

    @Override
    public void updateUserUi(AccountInfo info){
        if(co2View==null||h2oView==null){
            needToUpdateIcons=true;
            co2=info.getCo2();
            h2o=info.getH2o();
        }else {
            updateCo2(info.getCo2());
            updateH2o(info.getH2o());
        }
    }

    private void updateH2o(double nr){
        h2oView.setTitle(nr+"kg H2O");
    }

    private void updateCo2(double nr){
        co2View.setTitle(nr+"l CO2");
    }

    @Override
    public void display(Task task) {
        showTask=true;
        int id;
        String backstackName;
        if(secondFrame==null) {
            id=R.id.first_layout;
            backstackName=TASK_BACKSTACK_NAME;
        }else{
            id=R.id.second_layout;
            backstackName=null;
        }
        if(BuildConfig.DEBUG)Log.d(TAG, "display: task:"+task.toString());
        putFragment(TaskFragment.newInstance(task),TaskFragment.TAG,id,true,backstackName,true);
        //TODO make fragment to show task or put fragment on top and show this object;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EVERY_DAY,everyDay);
        outState.putBoolean(SHOW_TASK,showTask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(BuildConfig.DEBUG)Log.d(TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(toolbarTool.getMenuResource(),menu);
        co2View=menu.findItem(R.id.co2);
        h2oView=menu.findItem(R.id.h2o);
        everyDayView=menu.findItem(R.id.everyDay);
        if(everyDay){
            everyDayView.setTitle(TODAY);
        }
        if(needToUpdateIcons){
            if(BuildConfig.DEBUG)Log.d(TAG,"updating icons");
            updateH2o(h2o);
            updateCo2(co2);
            needToUpdateIcons=false;
        }
        return true;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toolbarTool.handleAction(item.getItemId());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(secondFrame==null) {
            if (!showTask && everyDay) {
                everyDay = false;
                everyDayView.setTitle(EVERY_DAY);
            }
            if (showTask) {
                showTask = false;
            }
        }else{
            everyDay = false;
            everyDayView.setTitle(EVERY_DAY);
            //TODO manage backpress for tablet
        }
        if(BuildConfig.DEBUG)Log.d(TAG,"back pressed");
    }

    public void updateToolbar(MenuBundle menuBundle){
        double quantity=menuBundle.getQuantity();
        if(menuBundle.getType().equals(MenuBundle.TYPE.co2)){
            updateCo2(quantity);
        }else{
            updateH2o(quantity);
        }
    }

    @Override
    public void display(InfoChanged bundle) {
        if(bundle.getUpdateType().equals(InfoChanged.TYPE.MENU_BUNDLE)){
            updateToolbar(bundle.getMenuBundle());
        }else{
            notifyFragment(bundle.getTasks());
            Log.d(TAG,"notify framgnet");
        }
    }

    private void notifyFragment(List<Task> tasks) {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        String lastBackstack = null;
        String tag=null;
        if (count != 0) {
            Log.d(TAG, "count!=0");
            lastBackstack = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            if (lastBackstack.equals(TODAY_LIST_BACKSTACK_NAME)) {
                tag = TodayTasksFragment.TAG;
            } else if (lastBackstack.equals(EVERY_DAY_LIST_BACKSTACK_NAME)) {
                tag = EveryDayTasksFragment.TAG;
            } else if (lastBackstack.equals(TASK_BACKSTACK_NAME)) {
                tag = TaskFragment.TAG;
            }
        }else {
            tag=TodayTasksFragment.TAG;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment instanceof MyFragment) {
            Log.d(TAG, "update tasks");
            ((MyFragment) fragment).updateTasks(tasks);
        }
    }
}
