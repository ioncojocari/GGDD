package com.ioncoj.ggdd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.FirebaseDao;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.ggdd.R;
import com.ioncoj.data.utills.TaskDisplayer;
import com.ioncoj.ggdd.databinding.TaskLayoutBinding;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TaskFragment extends MyFragment implements TaskDisplayer {
    public  static final String TAG=TaskFragment.class.getCanonicalName();
    protected Task task;
    protected TaskLayoutBinding binder;
    protected FirebaseStorage storageReference;
    protected IDao dao;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.task_layout,container,false);
        binder =TaskLayoutBinding.bind(root);
        CheckBox checkBox=binder.getRoot().findViewById(R.id.checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTaskStatus(checkBox);
            }
        });
        storageReference=FirebaseStorage.getInstance("gs://ggdd-6cdca.appspot.com/");
        dao=new FirebaseDao(lifecycle(), FragmentEvent.DESTROY, FirebaseAuth.getInstance().getCurrentUser());
        if(savedInstanceState!=null&&savedInstanceState.containsKey(Task.OBJECT)){
            task=savedInstanceState.getParcelable(Task.OBJECT);
        }
        if(task!=null){
            display(task);
        }
        doAfterInit();
        return binder.getRoot();
    }

    protected void doAfterInit(){

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(task!=null)
            outState.putParcelable(Task.OBJECT,task);
    }

    public void display(Task task) {
        this.task=task;
        binder.setTask(task);
        StorageReference ref=storageReference.getReference().child("images/"+task.getUrl());
        binder.setStorage(ref);
    }

    private void changeTaskStatus(CheckBox checkBox){
        checkBox.setEnabled(false);
        dao.changeDoneStatus(task)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycle.bindUntilEvent(lifecycle(),FragmentEvent.DESTROY))
                .subscribe(task-> {
                    display(task);
                    checkBox.setEnabled(true);
                });
    }

    public static TaskFragment newInstance(Task task){
        TaskFragment taskFragment=new TaskFragment();
        taskFragment.task=task;
        return taskFragment;
    };

    @Override
    public void updateTasks(List<Task> taskList) {
        Log.d(TAG,"updating tasks start");
        if(task!=null) {
            Log.d(TAG,"updating tasks"+taskList.get(0));
            for (Task first : taskList) {
                if (first.getDate()==task.getDate()){
                    display(first);
                    return;
                }
            }
        }
    }
}
