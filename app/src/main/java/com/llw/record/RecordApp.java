package com.llw.record;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.llw.record.audio.RecordCore;
import com.llw.record.basic.ActivityManager;
import com.llw.record.utils.FileUtils;
import com.llw.record.utils.MVUtils;
import com.tencent.mmkv.MMKV;

public class RecordApp extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @SuppressLint("StaticFieldLeak")
    private static RecordCore recordCore;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        recordCore = RecordCore.getInstance(context);

        MMKV.initialize(context);
        //工具类初始化
        MVUtils.getInstance();
        //文件类初始化
        FileUtils.getInstance(context);
    }

    public static ActivityManager getActivityManager() {
        return ActivityManager.getInstance();
    }

    public static RecordCore getRecordCore() {
        if (recordCore == null) {
            recordCore = RecordCore.getInstance(context);
        }
        return recordCore;
    }
}
