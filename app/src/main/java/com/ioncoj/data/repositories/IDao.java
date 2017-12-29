package com.ioncoj.data.repositories;

import com.ioncoj.data.models.AccountInfo;
import com.ioncoj.data.models.MenuBundle;
import com.ioncoj.data.models.Response;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.utills.InfoChanged;

import java.util.List;

import io.reactivex.Observable;

public interface IDao {
    Observable<List<Task>> getEveryDayTasks();
    Observable<Task> getTodayTask();
    Observable<Task> changeDoneStatus(Task tasks);
    Observable<AccountInfo> getUserInfo();
    Observable<InfoChanged> getUserInfoUpdate();
}
