package com.example.small.onetouchbutton10;

import android.app.Application;

public class Jsondata extends Application {
    public int what;
    public int cameracnt;
    public int videosec;

    public Jsondata(){
        super();
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public int getCameracnt() {
        return cameracnt;
    }

    public void setCameracnt(int cameracnt) {
        this.cameracnt = cameracnt;
    }

    public int getVideosec() {
        return videosec;
    }

    public void setVideosec(int videosec) {
        this.videosec = videosec;
    }

    public Jsondata(int what, int cameracnt, int videosec){
        super();
        this.what = what;
        this.cameracnt = cameracnt;
        this.videosec = videosec;
    }



}
