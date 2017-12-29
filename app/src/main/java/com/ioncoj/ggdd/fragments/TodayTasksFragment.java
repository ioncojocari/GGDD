package com.ioncoj.ggdd.fragments;

import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TodayTasksFragment extends TaskFragment  {
    public static final String TAG=TodayTasksFragment.class.getCanonicalName();
    @Override
    protected void doAfterInit() {
        displayTodayTask();
    }

    private void displayTodayTask() {
        dao.getTodayTask()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .compose(RxLifecycle.bindUntilEvent(lifecycle(),FragmentEvent.DESTROY))
                .subscribe(this::display);
    }
}

