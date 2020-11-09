package com.wellleo.common;

import android.app.Application;

public class CommonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Common.sApplication = this;
        Common.sAppContext = getApplicationContext();
    }
}
