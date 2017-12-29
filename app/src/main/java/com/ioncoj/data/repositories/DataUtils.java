package com.ioncoj.data.repositories;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataUtils {
    private Calendar calendar;

    public DataUtils(){
        calendar=Calendar.getInstance();
    }

    public Date getEndOfDay(Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date getStartOfDay(Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public long getBegin(Date date){
        return getStartOfDay(date).getTime();
    }

    public long getEnd(Date date){
        return getEndOfDay(date).getTime();
    }

    public Date getTimeNow(){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar.getTime();
    }

    public boolean isToday(long time){
        Date timeNow=getTimeNow();
        long begin =getBegin(timeNow);
        long end=getEnd(timeNow);
        return time>=begin&&time<=end;
    }

    public boolean isToday(Date date){
        return isToday(date.getTime());
    }
}
