package com.example.che.maptest;

/**
 * Created by chehongshu on 2017/5/6.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

import android.util.Log;

/**
 *   蓝牙收到的数据进行操作
 */

public class receData_handle {

    public int[] negative_Bytetoint(byte[] data_Byte){
        Log.e("Byte里面的数组的大小",data_Byte.length+"");
            int [] data_int = new int[data_Byte.length];
            for(int k=0;k<data_Byte.length;k++)
            {
                if(data_Byte[k]<0)
                {
                    data_int[k] = (int)(data_Byte[k]+256);
                }else if(data_Byte[k]>0)
                {
                    data_int[k] = (int)(data_Byte[k]);
                }
            }
            return data_int;

    }
    /**
     *
     * @param recedata  接受到蓝牙那边的数据
     * @return   如果校验成功则 返回true   校验失败  返回false
     */
    public boolean receData_check(int[] recedata)
    {
        int sum = 0;
        //  byte 0-9 相加
        for(int j=0;j<=recedata.length;j++)
        {
            sum += recedata[j];
        }
        //  取低八位
        sum = (int)(sum&0xff);
        if(sum == recedata[recedata.length-1])
        {
            //  校验成功
            return true;
        }else{
            //  校验失败
            return false;
        }
    }
    /**
     *
     * @param data_Lon  蓝牙发过来的数据
     * @return   得到数据处理之后的  经度
     */
    public float getrece_Lon(int[] data_Lon)
    {

        float Lon=(float)(data_Lon[5]*two_power(24)+data_Lon[4]*two_power(16)+data_Lon[3]*two_power(8)+data_Lon[2]*1)/ten_power(7);
        return Lon;
    }

    /**
     *
     * @param data_Lat 蓝牙发过来的数据
     * @return 得到数据处理之后的  纬度
     */
    public float getrece_Lat(int[] data_Lat)
    {
        Log.e("数组大小",data_Lat.length+"");
        float Lat=(float) ((float)(data_Lat[9]*two_power(24)+data_Lat[8]*two_power(16)+data_Lat[7]*two_power(8)+data_Lat[6]*1)/ten_power(7));
        return Lat;
    }

    /**
     *
     * @param n  次幂
     * @return   2的n次幂的值
     */
    public  int two_power(int n)
    {
        int sum=1;
        for(int i=1;i<=n;i++)
        {
            sum*=2;
        }
        return sum;
    }

    /**
     *
     * @param n  次幂
     * @return   10的n次幂的值
     */
    public  int ten_power(int n)
    {
        int sum=1;
        for(int j=1;j<=n;j++)
        {
            sum*=10;
        }
        return sum;
    }

}
