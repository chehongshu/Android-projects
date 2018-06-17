package com.example.che.userdefined_toast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.che.userdefined_toast.Util.ToastUtil;
import com.example.che.userdefined_toast.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View view){
        ToastUtil.showToast(MainActivity.this , "Toast一下");
    }
}