package com.ioncoj.data.repositories;

import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.ioncoj.data.models.AccountInfo;
import com.ioncoj.data.models.Response;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.models.MenuBundle;
import com.ioncoj.data.utills.InfoChanged;
import com.ioncoj.ggdd.BuildConfig;

import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class FirebaseDao implements IDao {
    private Format format;
    private static final int MAX_AMOUNT_OF_TASKS_SHOWN=5;
    private static final String TAG="FirebaseDao";
    private FirebaseDatabase database;
    //TODO i can make it static and add updates when add or removetask is triggered; it will be more acuratly //need to use syncr(
    private PublishSubject<InfoChanged> updates;
    private DatabaseReference scoreUpdatesReference;
    private DatabaseReference mostUsedUpdatesReference;
    private FirebaseUser firebaseUser;
    private DataUtils utils;
    //TODO refactor updating ,deleting ,
    public FirebaseDao(Observable events, Object stop, FirebaseUser user){
        this();
        firebaseUser=user;
        utils=new DataUtils();
        events.subscribe(event->{
           if(event.equals(stop)){
               if(scoreUpdatesReference !=null){
                   scoreUpdatesReference.removeEventListener(scoreNotifier);
               }
               if(mostUsedUpdatesReference!=null){
                   mostUsedUpdatesReference.removeEventListener(notifiesAboutCheckedTasks);
               }
            }
        });
    }

    /**
     * Never use this constructor if there is lifecycle like in activity,fragments;
     */
    public FirebaseDao(){
        format=new SimpleDateFormat("yy-MM-dd");
        database=FirebaseDatabase.getInstance();
        updates=PublishSubject.create();
    }

    @Override
    public Observable<List<Task>> getEveryDayTasks() {
        //TODO get most recently used everydaytask till today including today;

        Observable<List<Task>> observable=Observable.create(e->{
            if(firebaseUser==null){
                e.onComplete();
                return;
            }
            PublishSubject<List<Task>> doneTasks=PublishSubject.create();
            DatabaseReference ref = database.getReference("/users/"+firebaseUser.getUid()+"/info/mostUsed/");
            long from=utils.getBegin(utils.getTimeNow());
            Log.d(TAG,"from:"+from);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> lastDates=getLastDays(7,System.currentTimeMillis(),format);
                    List<DataSnapshot> snapshots=getSnapshotsWithKeys(dataSnapshot,lastDates);
                    List<Task> tasks=getEveryDayTasksFromSnapshots(snapshots);
                    Log.d(TAG, "tasks sie:"+tasks.size());
                    List<TaskDone> doneList=getTaskDoneFromTasks(tasks);

                    Collections.sort(doneList, new Comparator<TaskDone>() {
                        @Override
                        public int compare(TaskDone o1, TaskDone o2) {
                            return o1.count> o2.count? -1 : (o1.count < o2.count) ? 1 : 0;
                        }
                    });
                    if(doneList.size()>MAX_AMOUNT_OF_TASKS_SHOWN){
                        doneList=doneList.subList(0,MAX_AMOUNT_OF_TASKS_SHOWN);
                    }
                    List<Task> finalTasks=new ArrayList<>();
                    for(TaskDone done:doneList){
                        finalTasks.add(done.task);
                    }
                    doneTasks.onNext(finalTasks);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            doneTasks.subscribe(tasks->{
                if(tasks.size()==MAX_AMOUNT_OF_TASKS_SHOWN){
                    e.onNext(tasks);
                }else{
                    DatabaseReference reference=database.getReference("/tasks/everyDay");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Task> everyDayTasks=getTasks(dataSnapshot);
                            int size=MAX_AMOUNT_OF_TASKS_SHOWN-tasks.size();
                            List<Task> needToRemove=new ArrayList<>();
                            for(Task task:everyDayTasks){
                                for(Task task2:tasks) {
                                    if (task.getDate() ==task2.getDate()){
                                        needToRemove.add(task);
                                    }
                                }
                            }
                            everyDayTasks.removeAll(needToRemove);
                            if(everyDayTasks.size()>size){
                                everyDayTasks=everyDayTasks.subList(0,size);
                            }
                            everyDayTasks.addAll(tasks);
                            Collections.reverse(everyDayTasks);
                            e.onNext(everyDayTasks);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        });
        return observable;
    }

    private List<String> getLastDays(int amountOfDays,long tillTime,Format format){
        List<String> lastDates=new ArrayList<>();
        BigDecimal nr=new BigDecimal(tillTime);
        BigDecimal oneDay=new BigDecimal(24*3600*1000L);
        for(int i=(-1)*amountOfDays;i<=0;i++){
            BigDecimal dateLong=nr.add(oneDay.multiply(new BigDecimal(i)));
            String date=format.format(dateLong);
            lastDates.add(date);
        }
        return lastDates;
    }

    private List<DataSnapshot> getSnapshotsWithKeys(DataSnapshot dataSnapshot,List<String> keys){
        List<DataSnapshot> snapshots=new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            String key=snapshot.getKey();
            if(keys.contains(key)){
                snapshots.add(snapshot);
                keys.remove(key);
            }

            if(keys.size()==0){
                break;
            }
        };
        return snapshots;
    }

    private List<Task> getEveryDayTasksFromSnapshots(List<DataSnapshot> snapshots){
        List<Task> tasks=new ArrayList<>();
        for(DataSnapshot snapshot:snapshots){
            for(DataSnapshot sn:snapshot.getChildren()) {
                Task task=getTask((Map<String, Object>) sn.getValue());
                if(task.isEveryDay()) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    private List<TaskDone> getTaskDoneFromTasks(List<Task> tasks){
        List<TaskDone> doneList=new ArrayList<>();
        for(Task task:tasks){
            boolean needToAdd=true;
            if(doneList.size()==0){
                TaskDone done=new TaskDone();
                done.task=task;
                done.count++;
                doneList.add(done);
            }else {
                for (TaskDone done : doneList) {
                    if (task.getDate() == done.task.getDate()) {
                        done.count++;
                        needToAdd = false;
                        if (task.getLastTimeChecked() > done.task.getLastTimeChecked()) {
                            done.task.setLastTimeChecked(task.getLastTimeChecked());
                        }
                    }
                }
                if (needToAdd) {
                    TaskDone taskDone = new TaskDone();
                    taskDone.task = task;
                    taskDone.count++;
                    doneList.add(taskDone);
                }
            }
        }
        return doneList;
    }

    private class TaskDone{
        Task task;
        int count;
    }

    @Override
    public Observable<Task> getTodayTask() {
        //TODO get task with date (today);
        return Observable.create(e->{
            if(firebaseUser==null){
                e.onComplete();
                return;
            }
            String date=getTodayDate();
            PublishSubject<Task> taskPublishSubject=PublishSubject.create();
            DatabaseReference ref=database.getReference("/tasks/all/"+date);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Task task=new Task();
                    if(dataSnapshot!=null&&dataSnapshot.exists()){
                        Task tryTask=dataSnapshot.getValue(Task.class);
                        if(tryTask!=null){
                            task=tryTask;
                        }
                    }
                    taskPublishSubject.onNext(task);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            taskPublishSubject.subscribe(task-> {
                DatabaseReference reference = database.getReference("/users/" + firebaseUser.getUid() + "/info/mostUsed/"+date +"/"+ task.getDate());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Task finalTask=getTask((Map<String,Object>)dataSnapshot.getValue());
                            task.setLastTimeChecked(finalTask.getLastTimeChecked());
                        }else{
                            task.setLastTimeChecked(0);
                        }
                        e.onNext(task);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            });
        });
    }

    @Override
    public Observable<Task> changeDoneStatus(Task task) {
        //TODO read task create 3 publishers
        return Observable.create(e-> {
            if (firebaseUser == null) {
                e.onComplete();
                return;
            }

            PublishSubject<Boolean> h2oPublisher=PublishSubject.create();
            PublishSubject<Boolean> co2Publisher=PublishSubject.create();
            PublishSubject<Boolean> changeDoneStatus=PublishSubject.create();

            boolean taskIsChecked=false;
            if (task.getLastTimeChecked() == 0) {
                taskIsChecked = false;
            } else if (!task.isEveryDay()) {
                taskIsChecked = true;
            } else if (task.isEveryDay()) {
                long lastTime = task.getLastTimeChecked();
                taskIsChecked = utils.isToday(lastTime);
            }
            boolean finalTaskIsChecked = taskIsChecked;
            if(taskIsChecked){
                task.setLastTimeChecked(0);
            }else{
                task.setLastTimeChecked(System.currentTimeMillis());
            }

            DatabaseReference pointsRef = database.getReference("/users/" + firebaseUser.getUid() + "/info/points");
            pointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean needToUpdateCo2=true;
                    boolean needToUpdateH2o=true;
                    long[] points=readPoints(dataSnapshot);
                    long co2=points[0];
                    long h2o=points[1];
                    if(task.getCo2()==0){
                        needToUpdateCo2=false;
                    }
                    if(task.getH2o()==0){
                        needToUpdateH2o=false;
                    }
                    if(!finalTaskIsChecked){
                        co2+=task.getCo2();
                        h2o+=task.getH2o();
                    }else{
                        co2-=task.getCo2();
                        h2o-=task.getH2o();
                    }

                    if(needToUpdateCo2){
                        updatePoints(co2,co2Publisher, MenuBundle.TYPE.co2);
                    }else{
                        co2Publisher.onNext(true);
                        co2Publisher.onComplete();
                    }

                    if(needToUpdateH2o){
                        updatePoints(h2o,h2oPublisher, MenuBundle.TYPE.h2o);
                    }else{
                        h2oPublisher.onNext(true);
                        h2oPublisher.onComplete();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference mostUsedRef = database.getReference("/users/" + firebaseUser.getUid() + "/info/mostUsed/"+format.format(utils.getTimeNow())+"/"+task.getDate());
            OnCompleteListener listener=new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                    changeDoneStatus.onNext(true);
                    changeDoneStatus.onComplete();
                }
            };
            if(!taskIsChecked) {
                task.setLastTimeChecked(System.currentTimeMillis());
                mostUsedRef.setValue(task).addOnCompleteListener(listener);
            }else{
                mostUsedRef.removeValue().addOnCompleteListener(listener);
            }
            TimerTask timerTask;
            Timer timer=new Timer();
            Disposable disposable=Observable.zip(h2oPublisher,co2Publisher,changeDoneStatus,(f, s, t)->{
                return task;
            }).subscribe(t-> {
                e.onNext (t);
                timer.cancel();
            });
            timerTask=new TimerTask(){
                @Override
                public void run(){
                    if(!disposable.isDisposed()){
                        disposable.dispose();
                    }
                    e.onNext(task);
                }
            };

            timer.schedule(timerTask,4000);
        });
    }

    private long[] readPoints(DataSnapshot dataSnapshot){
        long co2=0,h2o=0;
        if(dataSnapshot.exists() && !(dataSnapshot.getKey()==null)){
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                if(snapshot.exists()) {
                    if (snapshot.getKey().equals("co2")) {
                        co2 =(long)snapshot.getValue();
                    } else if(snapshot.getKey().equals("h2o")) {
                        h2o=(long) snapshot.getValue();
                    }
                }
            }
        }
        long[] result={co2,h2o};
        return result;
    };

    @Override
    public Observable<AccountInfo> getUserInfo() {
        return Observable.create(e->{
            if(firebaseUser==null){
                e.onComplete();
                return;
            }
            PublishSubject<Points> pointsPublishSubject=PublishSubject.create();
            PublishSubject<List<Task>> mostUsedPublishSubject=PublishSubject.create();
            DatabaseReference reference=database.getReference("/users/"+firebaseUser.getUid()+"/info/points");
            DatabaseReference todayDoneReference=database.getReference("/users/"+firebaseUser.getUid()+"/info/mostUsed/"+format.format(System.currentTimeMillis()));

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                     Points points=getPoint(dataSnapshot);
                     pointsPublishSubject.onNext(points);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    e.onComplete();
                }
            });
            todayDoneReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Task>tasks=getTasks(dataSnapshot);
                    mostUsedPublishSubject.onNext(tasks);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Observable.zip(pointsPublishSubject,mostUsedPublishSubject,(f,s)->{
                AccountInfo accountInfo=new AccountInfo();
                accountInfo.setH2o(f.h2o);
                accountInfo.setCo2(f.co2);
                accountInfo.setMostUsed(s);
                return accountInfo;
            }).subscribe(e::onNext);
        });
    }

    private void updatePoints(long amount, PublishSubject<Boolean> observable, MenuBundle.TYPE type){
        String endPath;
        if(type.equals(MenuBundle.TYPE.co2)){
            endPath="co2";
        }else{
            endPath="h2o";
        }
        DatabaseReference reference=database.getReference("/users/" + firebaseUser.getUid() + "/info/points/"+endPath);
        reference.setValue(amount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                observable.onNext(true);
                observable.onComplete();
            }
        });
    }


    private Points getPoint(DataSnapshot snapshot){
        Points points=new Points();
        Map<String,Object> map= (Map<String, Object>) snapshot.getValue();
        if(map!=null&&!map.isEmpty()) {
            if (map.containsKey("co2")) {
                Long co2 = (Long) map.get("co2");
                points.co2=co2;
            }
            if (map.containsKey("h2o")) {
                Long h2o = (Long) map.get("h2o");
                points.h2o=h2o;
            }
        }
        return points;
    }

    private class Points{
        long h2o;
        long co2;
    }

    private void initUpdates(FirebaseUser user){
        if(scoreUpdatesReference ==null) {
            scoreUpdatesReference = database.getReference("/users/" + user.getUid() + "/info/points");
            scoreUpdatesReference.addChildEventListener(scoreNotifier);
        };
        if(mostUsedUpdatesReference==null){
            mostUsedUpdatesReference=database.getReference("/users/"+user.getUid()+"/info/mostUsed/"+format.format(System.currentTimeMillis()));
            mostUsedUpdatesReference.addChildEventListener(notifiesAboutCheckedTasks);
        }
    }

    public Observable<List<Task>> keepTaskInMemorry(){
        return Observable.create(e->{
            DatabaseReference reference=database.getReference("/tasks/all");
            Query query=reference.orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG,"size:start");
                    List<Task> tasks=getTasks(dataSnapshot);
                    Log.d(TAG,"size:"+tasks.size());
                    e.onNext(tasks);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });
    }

    @Override
    public Observable<InfoChanged> getUserInfoUpdate() {
        if(firebaseUser==null){
            return Observable.empty();
        }
        initUpdates(firebaseUser);
        return updates;
    }

    private Task getTask(Map<String,Object> map){
        Task task=new Task();
        task.setTitle((String) map.get(Task.TITLE_FIELD));
        task.setUrl((String) map.get(Task.URL_FIELD));
        task.setDate((long)map.get(Task.DATE));
        task.setContent((String) map.get(Task.CONTENT_FIELD));
        task.setEveryDay((boolean)map.get(Task.EVERY_DAY_FIELD));
        task.setH2o((long)map.get(Task.H2O_FIELD));
        task.setCo2((long)map.get(Task.CO2_FIELD));
        task.setLastTimeChecked((long)map.get(Task.LAST_TIME_CHECKED_FIELD));
        return task;
    };

    private List<Task> getTasks(DataSnapshot dataSnapshot){
        List<Task> tasks=new ArrayList<>();
        if(dataSnapshot.exists()){
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                Task task=getTask((Map<String,Object>)snapshot.getValue());
                if(task!=null&&task.getDate()!=0){
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    private ChildEventListener scoreNotifier =new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(BuildConfig.DEBUG)Log.d(TAG, "scoreNotifier,onChildChanged" + dataSnapshot);
            String key = dataSnapshot.getKey();
            InfoChanged infoChanged=new InfoChanged();
            if (key.equals("co2")||key.contains("h2o")) {
                MenuBundle.TYPE type;
                if(key.equals("co2")) {
                    type = MenuBundle.TYPE.co2;
                }else{
                    type = MenuBundle.TYPE.h2o;
                }
                Long quantity = (Long) dataSnapshot.getValue();
                MenuBundle bundle = new MenuBundle();
                bundle.setType(type);
                bundle.setQuantity(quantity);
                infoChanged=new InfoChanged(bundle);

            }
            updates.onNext(infoChanged);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener notifiesAboutCheckedTasks=new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(BuildConfig.DEBUG)Log.d(TAG, "onChildAdded" );
            notifyAboutDiference(dataSnapshot,true);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            notifyAboutDiference(dataSnapshot,false);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        private void notifyAboutDiference(DataSnapshot dataSnapshot,boolean added){
            Task task=dataSnapshot.getValue(Task.class);
            if(added){
                task.setLastTimeChecked(System.currentTimeMillis());
            }else {
                task.setLastTimeChecked(0);
            }
            List<Task> tasks=new ArrayList<>();
            tasks.add(task);
            InfoChanged infoChanged=new InfoChanged(tasks);
            updates.onNext(infoChanged);
        }
    };

    private String getTodayDate(){
        long date=System.currentTimeMillis();
        return format.format(date);
    }

}
