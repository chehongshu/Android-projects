package com.example.che.maptest;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


/**
 * Created by chehongshu on 2017/4/24.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

/**
 *   飞机参数界面
 */
public class plane_parameter_Activity extends Activity {

    private int[] imageResIds ={R.drawable.feiji_xia,R.drawable.feiji_you,
    R.drawable.gaodu,R.drawable.jiangluo,R.drawable.kongzhi,R.drawable.suoding,
    R.drawable.yonghu,R.drawable.youxiajiao};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.feiji_canshu_main);
//        RelativeLayout fr=(RelativeLayout)findViewById(R.id.feijicanshu_Layout);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        // 获取这个图片的宽和高
//        Bitmap bitmap = BitmapFactory.decodeFile(imageResIds, options); //此时返回bm为空
//        options.inJustDecodeBounds = false;
//        //计算缩放比
//        int be = (int)(options.outHeight / (float)200);
//        if (be <= 0)
//            be = 1;
//        options.inSampleSize = be;
//        //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
//        bitmap=BitmapFactory.decodeFile(R.drawable.feiji_xia,options);
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        System.out.println(w+"   "+h);
//        ImageView iv=new ImageView(this);
//        iv.setImageBitmap(bitmap);


    }
    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

}