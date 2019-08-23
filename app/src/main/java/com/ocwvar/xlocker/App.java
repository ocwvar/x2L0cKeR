package com.ocwvar.xlocker;

import android.app.Application;

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        //这里进行全局的异常捕捉，并生成对应的日志文件到 /sdcard/.xcl/ 下
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
    }
}
