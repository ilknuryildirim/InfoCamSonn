package com.infocam;

import android.graphics.Bitmap;

/**
 * Created by konuk on 18.4.2016.
 */
public class Image {
    String tag;
    Bitmap map;

    public Image(String t, Bitmap m){
        tag = t;
        map = m;
    }

    public Bitmap getBitmap(){
        return this.map;
    }

    public String getTag(){
        return this.tag;
    }
}
