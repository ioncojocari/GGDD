package com.ioncoj.data.configurations;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;
import com.ioncoj.ggdd.BuildConfig;

public class ImageDisplayer {
    private static final String TAG=ImageDisplayer.class.getCanonicalName();
    @BindingAdapter("src")
    public static void setImage(@NonNull ImageView imageView , int id) {
        //TODO display image //get it from firebase storage ;
        WindowManager mWinMgr = (WindowManager)imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display =mWinMgr.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        imageView.getLayoutParams().height=width*2/3;
        imageView.setLayoutParams(imageView.getLayoutParams());
        imageView.requestLayout();
        if(id!=0) {
            GlideApp.with(imageView.getContext())
                    .load(id)
                    .into(imageView);

        }
    }
}
