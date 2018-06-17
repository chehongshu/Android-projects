package com.example.che.maptest;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Created by chehongshu on 2017/4/29.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */
/**
 *    商城app
 */


public class shop_Activity  extends Activity {
    WebView forum_webview;     //
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        forum_webview = new WebView(this);  // 论坛 网页
        setContentView(forum_webview);
        forum_webview.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
        forum_webview.getSettings().setJavaScriptEnabled(true);// 启用js
        //该设置使打开新链接时还在同一个WebView中打开，不加该设置，打开新链接时将会在android自带浏览器中打开网页。
        forum_webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        forum_webview.loadUrl("http://123.206.176.72/shop"); // 进入相应的界面
        setTitle("极客商城");

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
