package com.example.che.userdefined_toast.Util;

/**
 * Created by che on 2017/5/5.
 */
//工具类代码
//(util)ToastUtil
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.che.userdefined_toast.R;

public class ToastUtil {
    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable runnable = new Runnable() {
        public void run() {
            mToast.cancel();
            //toast隐藏后，将其置为null
            mToast=null;
    }
    };

    public static void showToast(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //自定义布局
        View view = inflater.inflate(R.layout.custom_toast, null);
        TextView text = (TextView) view.findViewById(R.id.toast_message);
        //显示的提示文字
        text.setText(message);
        mHandler.removeCallbacks(runnable);
        //只有mToast==null时才重新创建，否则只需更改提示文字
        if (mToast == null){
            mToast = new Toast(context);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(view);
        }
        mToast.show();
    }
}
