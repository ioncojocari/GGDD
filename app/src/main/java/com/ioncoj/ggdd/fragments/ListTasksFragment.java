package com.ioncoj.ggdd.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.FirebaseDao;
import com.ioncoj.data.repositories.IDao;
import com.ioncoj.data.utills.IAddListener;
import com.ioncoj.ggdd.BuildConfig;
import com.ioncoj.ggdd.R;
import com.ioncoj.data.utills.TaskDisplayer;
import com.ioncoj.ggdd.adapters.TaskRecyclerAdapter;
import com.trello.rxlifecycle2.android.FragmentEvent;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class ListTasksFragment extends MyFragment{

    private static final String TAG="ListTasksFragment";
    protected IDao dao;
    @BindView(R.id.recycler_view)
    protected RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private TaskDisplayer taskDisplayer;
    private IAddListener addListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.tasks_list_fragment,container,false);
        bindViews(root);
        recyclerView.setHasFixedSize(true);
        dao=new FirebaseDao(lifecycle(), FragmentEvent.DESTROY,FirebaseAuth.getInstance().getCurrentUser());
        addListener=new IAddListener();
        displayItems();
        return root;
    }

    private void bindViews(View view){
        ButterKnife.bind(this,view);
    }

    protected void displayItems(List<Task>tasks){
        if(BuildConfig.DEBUG)Log.d(TAG, "displaingItems: size:"+tasks.size());
        adapter=new TaskRecyclerAdapter(tasks,taskDisplayer,dao,lifecycle(),addListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        taskDisplayer=(TaskDisplayer)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        taskDisplayer=null;
    }

    @Override
    public void updateTasks(List<Task> taskList) {
        Log.d(TAG,"updating tasks");
        addListener.call(taskList);
    }

    protected abstract void displayItems();
}
