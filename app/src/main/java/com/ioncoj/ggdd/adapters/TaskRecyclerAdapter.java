package com.ioncoj.ggdd.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.DataUtils;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.data.utills.IAddListener;
import com.ioncoj.data.utills.IUpdateTasks;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.ggdd.R;
import com.ioncoj.data.utills.TaskDisplayer;
import com.ioncoj.ggdd.databinding.TaskRowBinding;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskRecyclerAdapter.MyHolder> {
    private List<Task> tasks;
    private TaskDisplayer taskDisplayer;
    private Observable<FragmentEvent> lifecycle;
    private IDao dao;
    private DataUtils dataUtils;
    public TaskRecyclerAdapter(List<Task> taskList, TaskDisplayer taskDisplayer, IDao dao, Observable<FragmentEvent>lifecycle, IAddListener addListener){
        this.tasks=taskList;
        this.taskDisplayer=taskDisplayer;
        this.lifecycle=lifecycle;
        this.dao=dao;
        dataUtils=new DataUtils();
        addListener.set(new IUpdateTasks() {
            @Override
            public void updateTasks(List<Task> taskList) {
                updateCheckedStatus(taskList);
            }
        });
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        View view=LayoutInflater.from(context).inflate(R.layout.task_row,parent,false);
        TaskRowBinding rowBinding=TaskRowBinding.bind(view);
        return new MyHolder(rowBinding);
    }

    public void updateCheckedStatus(List<Task> allTasks){
        List<Task> todayTasks=new ArrayList<>();
        //copy(tasks);

        for(Task first: allTasks){
            for(Task second: tasks){
                if(first.getDate()==second.getDate()){
                    int position=tasks.indexOf(second);
                    second.setLastTimeChecked(first.getLastTimeChecked());
                    notifyItemChanged(position);
                }
            }
        }
        Log.d("TaskRecycler","after ");
    }

    /**
     * copies just lastTimeChanged from task
     * @param tasks
     * @return
     */
    private List<Task> copy(List<Task> tasks){
        List<Task> result=new ArrayList<>();
        for(Task task:tasks){
            Task newTask=new Task();
            newTask.setLastTimeChecked(task.getLastTimeChecked());
            result.add(newTask);
        }
        return result;
    };

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Task task=tasks.get(position);
        if(task!=null) {
            holder.rowBinding.setTask(task);
            boolean showChecked;
            if(task.checked!=null&&task.checked){
                showChecked=true;
            }else if (task.checked==null) {
                showChecked= needToBeChecked(task);
            } else {
                if(BuildConfig.DEBUG)Log.d("TaskRecyclerAdapter","set unchecked"+task.getTitle() );
                showChecked=false;
            }
            holder.rowBinding.setEnabled(task.enabled);
            holder.rowBinding.setChecked(showChecked);
            if(BuildConfig.DEBUG)Log.d("TaskRecyclerAdapter","tAsk" +task.toString());
        }
    }

    private boolean needToBeChecked(Task task){
        boolean show=false;
        if(task.getLastTimeChecked()==0){
            show=false;
        }else if(!task.isEveryDay()){
            show=true;
        }else if (task.isEveryDay()){
            long lastTime=task.getLastTimeChecked();
            DataUtils utils=new DataUtils();
            show=utils.isToday(lastTime);
        }
        return show;
    };

    @Override
    public int getItemCount() {
        if(tasks!=null){
            return tasks.size();
        }
        return 0;
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TaskRowBinding rowBinding;
        MyHolder(TaskRowBinding taskRowBinding) {
            super(taskRowBinding.getRoot());
            rowBinding=taskRowBinding;
            taskRowBinding.getRoot().setOnClickListener( (v)->{
                taskDisplayer.display(tasks.get(getAdapterPosition()));
            });
            CheckBox checkBox=taskRowBinding.getRoot().findViewById(R.id.checkbox);
            checkBox.setOnClickListener(v-> {
                changeTaskStatus(getAdapterPosition(),checkBox);
            });
        }
    }

    private void changeTaskStatus(int position,CheckBox checkBox){
        checkBox.setEnabled(false);
        tasks.get(position).checked=!checkBox.isChecked();
        checkBox.setChecked(tasks.get(position).checked);
        tasks.get(position).enabled=false;
        dao.changeDoneStatus(tasks.get(position))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycle.bindUntilEvent(lifecycle,FragmentEvent.DESTROY))
                .subscribe(task-> {
                    tasks.remove(position);
                    tasks.add(position,task);
                    tasks.get(position).checked=null;
                    tasks.get(position).enabled=true;
                    notifyItemChanged(position);
                    if(BuildConfig.DEBUG)Log.d("Adapter","position changed "+position);
                });
    }

}
