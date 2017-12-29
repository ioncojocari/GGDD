package com.ioncoj.data.utills;

import android.view.Menu;

import com.ioncoj.data.models.MenuBundle;
import com.ioncoj.data.models.Task;

import java.util.List;

public class InfoChanged {
    public enum TYPE {
        MENU_BUNDLE,TASK;
    }

    private TYPE type;
    private MenuBundle menuBundle;
    private List<Task> tasks;

    public InfoChanged(){

    }

    public InfoChanged(MenuBundle menuBundle){
        type=TYPE.MENU_BUNDLE;
        this.menuBundle=menuBundle;
    }

    public InfoChanged(List<Task> task){
        type=TYPE.TASK;
        this.tasks=task;
    }

    public TYPE getUpdateType(){
        return type;
    }

    public MenuBundle getMenuBundle(){
        return menuBundle;
    }

    /**
     *
     * @return just task is if checked was changed;
     */
    public List<Task> getTasks(){
        return tasks;
    }



}
