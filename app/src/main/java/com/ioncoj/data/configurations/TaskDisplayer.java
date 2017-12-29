package com.ioncoj.data.configurations;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;
import com.ioncoj.data.models.MenuBundle;
import com.ioncoj.data.models.Task;
import com.ioncoj.data.repositories.DataUtils;
import com.ioncoj.ggdd.BuildConfig;

public class TaskDisplayer {
    private static final String TAG=TaskDisplayer.class.getCanonicalName();
    @BindingAdapter(value = {"src","repository"})
    public static void setImageUrl(@NonNull ImageView imageView, @Nullable String url, @NonNull StorageReference ref) {
        if(url==null){
            if(BuildConfig.DEBUG) Log.d(TAG,"url null");
        }else{
            WindowManager mWinMgr = (WindowManager)imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display =mWinMgr.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            imageView.getLayoutParams().height=width*9/16;
            imageView.setLayoutParams(imageView.getLayoutParams());
            imageView.requestLayout();
            GlideApp.with(imageView.getContext())
                    .load(ref)
                    .into(imageView);

            if(BuildConfig.DEBUG)Log.d(TAG,"url:"+url+",size:"+url.length());
        }
        if(BuildConfig.DEBUG)Log.d(TAG,"setting url");
    }

    @BindingAdapter("checked")
    public static void setCheckedToCheckBox(@NonNull CheckBox checkBox, @Nullable Task task){
        if(task!=null) {
            long time = task.getLastTimeChecked();
            boolean checked = false;
            if (time == 0) {
                checked = false;
            } else if (!task.isEveryDay()) {
                checked = true;
            } else if (task.isEveryDay()) {
                DataUtils utils = new DataUtils();
                if (utils.isToday(time)) {
                    checked = true;
                }
            }
            checkBox.setChecked(checked);
            checkBox.jumpDrawablesToCurrentState();
        }
    }

    @BindingAdapter(value = {"type","amount"},requireAll = false)
    public static void setText(@NonNull TextView textView, @NonNull MenuBundle.TYPE type,double amount){
        String result="";

        if(type== MenuBundle.TYPE.h2o){
            if(amount!=0) {
                result = "h2o " + amount + " l";
            }
        }else{
            if(amount!=0) {
                result = "co2 " + amount + " kg";
            }
        }
        textView.setText(result);
    }
}
