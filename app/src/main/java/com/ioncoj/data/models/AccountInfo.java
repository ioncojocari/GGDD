package com.ioncoj.data.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountInfo {
    private String userKey;
    private Map<String,Task> mostUsed;
    private double co2;
    private double h2o;

    public AccountInfo(){
        mostUsed=new HashMap<String,Task>();
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Map<String,Task> getMostUsed() {
        return mostUsed;
    }

    public boolean addTask(Task doneTask){
        String date=String.valueOf(doneTask.getLastTimeChecked());
        if(!mostUsed.containsKey(date)) {
            mostUsed.put(date, doneTask);
            return true;
        }else{
            return false;
        }
    }

    @Exclude
    public List<Task> getDoneTasks(){
        List<Task> doneTasks=new ArrayList<>();
        if(!mostUsed.isEmpty()){
            for(String key:mostUsed.keySet()){
                doneTasks.add(mostUsed.get(key));
            }
        }
        return doneTasks;
    };

    public void setMostUsed(Map<String,Task>newData) {
        mostUsed=newData;
    }
    public void setMostUsed(List<Task>newData) {
        mostUsed=new HashMap<>();
        for(Task task:newData){
            mostUsed.put(String.valueOf(task.getDate()),task);
        }

    }

    public double getCo2() {
        return co2;
    }

    public void setCo2(double co2) {
        this.co2 = co2;
    }

    public double getH2o() {
        return h2o;
    }

    public void setH2o(double h2o) {
        this.h2o = h2o;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "userKey='" + userKey + '\'' +
                ", mostUsed=" + mostUsed +
                ", co2=" + co2 +
                ", h2o=" + h2o +
                '}';
    }

    public boolean removeTask(Task doneTask) {
        String key=String.valueOf(doneTask.getLastTimeChecked());
        if(mostUsed.containsKey(key)) {
            mostUsed.remove(key);
            return true;
        }
        return false;
    }

}
