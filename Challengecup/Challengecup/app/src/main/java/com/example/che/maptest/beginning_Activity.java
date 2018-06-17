package com.example.che.maptest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by che on 2017/5/13.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */


public class beginning_Activity extends Activity {

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beginning);
        // 延时两秒 进入登录界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent beginning_to_login = new Intent(beginning_Activity.this, Login_Activity.class);    //切换Login Activity至User Activity
                startActivity(beginning_to_login);// 转换   activity
            }
        },1000);

    }
    @Override
    protected void onResume() {
        /**
         * 设置为竖屏
         */
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }
}
