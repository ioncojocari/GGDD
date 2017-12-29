package com.ioncoj.ggdd.fragments;

import com.ioncoj.data.models.Task;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EveryDayTasksFragment extends ListTasksFragment {
    public static final String TAG=EveryDayTasksFragment.class.getCanonicalName();
    @Override
    public void displayItems() {
        dao.getEveryDayTasks()
           .compose(RxLifecycle.bindUntilEvent(lifecycle(), FragmentEvent.DESTROY))
           .subscribeOn(Schedulers.newThread())
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe(this::displayItems);
    }

}
