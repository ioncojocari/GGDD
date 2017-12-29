package com.ioncoj.data.models;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.firebase.database.Exclude;
import com.google.firebase.storage.StorageReference;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ioncoj.data.configurations.GlideApp;
import com.ioncoj.data.repositories.DataUtils;
import com.ioncoj.ggdd.BuildConfig;

import java.util.Calendar;

public class Task implements Parcelable{
    private static final String TAG=Task.class.getCanonicalName();
    public static final String TITLE_FIELD="title";
    public static final String CONTENT_FIELD="content";
    public static final String URL_FIELD="url";
    public static final String DATE="date";
    public static final String OBJECT="TaskObject";
    public static final String EVERY_DAY_FIELD="everyDay";
    public static final String H2O_FIELD="h2o";
    public static final String CO2_FIELD="co2";
    public static final String LAST_TIME_CHECKED_FIELD="lastTimeChecked";

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("url")
    @Expose
    private String url;
    /**
     * date will be also the primary key
     */
    @SerializedName("date")
    @Expose
    private long date;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("everyDay")
    @Expose
    private boolean everyDay;
    @SerializedName("h2o")
    @Expose
    private float h2o;
    @SerializedName("co2")
    @Expose
    private float co2;
    @SerializedName("lastTimeChecked")
    @Expose
    private long lastTimeChecked;
    @Exclude
    public boolean enabled=true;
    @Exclude
    public Boolean checked=null;

    public Task(){

    }

    protected Task(Parcel in) {
        title = in.readString();
        url = in.readString();
        date = in.readLong();
        content = in.readString();
        everyDay = in.readByte() != 0;
        //h2o =
        setH2o(in.readFloat());
        //co2 =
        setCo2(in.readFloat());
        lastTimeChecked = in.readLong();
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public String toString() {
        return "RealmNews{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", date=" + date +
                ", content='" + content + '\'' +
                ", everyDay='" + everyDay + '\'' +
                ", h2o=" + h2o +
                ", co2=" + co2 +
                ", lastTimeChecked=" + lastTimeChecked +
                '}';
    }

    public long getLastTimeChecked() {
        return lastTimeChecked;
    }

    public void setLastTimeChecked(long lastTimeChecked) {
        this.lastTimeChecked = lastTimeChecked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDate(){
        return date;
    }

    public void setDate(long date){
        this.date=date;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEveryDay() {
        return everyDay;
    }

    public void setEveryDay(boolean everyDay) {
        this.everyDay = everyDay;
    }

    public float getH2o() {
        return h2o;
    }

    public void setH2o(float h2o) {
        if(h2o>0)
        this.h2o = h2o;
    }

    public float getCo2() {
        return co2;
    }

    public void setCo2(float co2) {
        if(co2>0)
        this.co2 = co2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(url);
        parcel.writeLong(date);
        parcel.writeString(content);
        parcel.writeByte((byte) (everyDay ? 1 : 0));
        parcel.writeFloat(h2o);
        parcel.writeFloat(co2);
        parcel.writeLong(lastTimeChecked);
    }

    public static class  Builder{
        private Task news;

        public Builder(){
            news=new Task();
        }
        public Task build(){
            return this.news;
        }

        public Builder setCo2(float co2){
            news.setCo2(co2);
            return this;
        };

        public Builder setH2o(float h2o){
            news.setH2o(h2o);
            return this;
        }

        public Builder setContent(String content){
            news.setContent(content);
            return this;
        }

        public Builder setTitle(String title){
            news.setTitle(title);
            return this;
        }

        public Builder setUrl(String url){
            news.setUrl(url);
            return this;
        }

        public Builder setDate(long date){
            news.setDate(date);
            return this;
        }

        public Builder setDate(Calendar date){
            news.setDate(date.getTimeInMillis());
            return this;
        };

        public Builder setLastTimeChecked(long date){
            news.setLastTimeChecked(date);
            return this;
        };

        public Builder setEveryDay(boolean everyDay){
            news.setEveryDay(everyDay);
            return this;
        }
    }

}
