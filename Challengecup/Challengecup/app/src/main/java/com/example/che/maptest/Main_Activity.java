package com.example.che.maptest;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Created by chehongshu on 2017/4/24.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

public class Main_Activity extends TabActivity {
    private TabHost tabHost;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabHost = getTabHost();
        addTab("act1", "连接设备", blue_tooth_Activity.class);
        addTab("act2", "地图界面", map_Activity.class);
        addTab("act3", "飞机参数", plane_parameter_Activity.class);
        addTab("act4","极客论坛",forum_Activity.class);
        addTab("act5","极客商城",shop_Activity.class);


        setContentView(tabHost);

    }
    /**
     * 添加Activity标签
     * @param tag   标识
     * @param title 标签标题
     * @param clazz 激活的界面
     */
    private void addTab(String tag, String title, Class clazz) {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(title);

        Intent intent = new Intent(getApplicationContext(),clazz);
        tabSpec.setContent(intent);
        tabHost.addTab(tabSpec);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}