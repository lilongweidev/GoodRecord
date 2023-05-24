package com.llw.record.basic;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.llw.record.RecordApp;

public class BasicActivity extends AppCompatActivity {

    private static final String TAG = BasicActivity.class.getSimpleName();
    protected Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        RecordApp.getActivityManager().addActivity(this);
    }
}
