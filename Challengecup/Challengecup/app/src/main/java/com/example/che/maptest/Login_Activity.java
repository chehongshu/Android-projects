package com.example.che.maptest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.che.maptest.util.ToastUtil;

/**
 *  Created by che on 2017/5/13.
 *  E-mail : 1454045208@qq.com
 *  qq : 1454045208
 */


public class Login_Activity extends Activity {
    Button login_button;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        login_button = (Button) findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(Login_Activity.this,"登陆成功");
                Intent  login_to_main = new Intent(Login_Activity.this, Main_Activity.class);    //切换Login Activity至User Activity
                startActivity(login_to_main);// 转换   activity
            }
        });
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