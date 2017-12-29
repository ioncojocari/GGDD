package com.ioncoj.data.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Response implements Parcelable {

    public Response(){

    }

    public Response(boolean succesful, String message){
        setSuccesful(succesful);
        setMessage(message);
    }
    private boolean succesful;
    private String message;

    protected Response(Parcel in) {
        succesful = in.readByte() != 0;
        message = in.readString();
    }

    public static final Creator<Response> CREATOR = new Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel in) {
            return new Response(in);
        }

        @Override
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };

    public boolean isSuccesful() {
        return succesful;
    }

    public void setSuccesful(boolean succesful) {
        this.succesful = succesful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Response{" +
                "succesful=" + succesful +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (succesful ? 1 : 0));
        dest.writeString(message);
    }

}
