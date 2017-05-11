package com.lisn.baidumapplugin;

import android.app.Application;

/**
 * Created by admin on 2017/5/10.
 */

public class myApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());
    }
}
