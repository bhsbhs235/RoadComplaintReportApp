package com.example.small.onetouchbutton10;

import android.graphics.drawable.Drawable;

/**
 * Created by Small on 2018-05-13.
 */

public class ListViewItem {
    private Drawable icon;
    private String content;
    private String lat;
    private String lng;
    private String date;

    public void setIcon(Drawable icon){
        this.icon = icon;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setLat(String lat){ this.lat= lat;}
    public void setLng(String lng){ this.lng = lng;}
    public void setDate(String date){  this.date = date;}

    public Drawable getIcon(){
        return this.icon;
    }
    public String getTitle(){
        return this.content;
    }
    public String getLat(){
        return this.lat;
    }
    public String getLng(){
        return this.lng;
    }
    public String getDate(){
        return this.date;
    }
}
