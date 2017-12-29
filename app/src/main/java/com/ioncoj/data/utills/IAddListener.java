package com.ioncoj.data.utills;

import android.view.View;

import com.ioncoj.data.models.Task;

import java.util.HashSet;
import java.util.List;

public class IAddListener {
    private IUpdateTasks runnable;
    public IAddListener(){

    }
    public void set(IUpdateTasks runnable){
        this.runnable=runnable;
    }

    public void call(List<Task> taskList){
        if(runnable!=null)
        runnable.updateTasks(taskList);
    }

    public void clear(){
        runnable=null;
    }
}

