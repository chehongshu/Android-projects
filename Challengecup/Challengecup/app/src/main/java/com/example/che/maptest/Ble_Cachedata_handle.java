package com.example.che.maptest;

/**
 * Created by chehongshu on 2017/5/11.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

import java.util.ArrayList;

/**
 *  Ble蓝牙每次 接收数据进行处理，因为  BLE 蓝牙每次发送数据最多只有20 Byte
 *  发送超过20Byte需要进行相关的处理和 自行算法
 */

public class Ble_Cachedata_handle {


    private int[] first_data = null;
    private int[] second_data = null;
    private int[] third_data = null;
    private int[] forth_data = null;

    public int[] getFirst_data() {
        return first_data;
    }
    /**
     *
     * @return   整个 处理好的数据 若不为null则可以一直 取出
     */
    public int[] getComplete_Data() {
        return Complete_Data;
    }

    public void setComplete_Data(int[] complete_Data) {
        Complete_Data = complete_Data;
    }

    private int[] Complete_Data = null;

    /**
     *
     * @param  data  整个的数据解析
     * @return
     */
    public void rece_all_data_handle(int[] data)
    {
        if((data[0]==0x55)) {
        switch(data[1])
        {
            case 0x57:  this.first_data  = data; break;
            case 0x58:  this.second_data = data; break;
            case 0x59:  this.third_data  = data; break;
            case 0x60:  this.forth_data  = data; break;
        }
    }
    }
    public int[] get_complete_receData()
    {
        if((this.first_data!=null)&&(this.second_data!=null)&&(this.third_data !=null)&&(this.forth_data!=null))
        {
            this.Complete_Data = this.combine_two_intdata(this.first_data,this.second_data);// 第一个包和 第二个包结合
            this.Complete_Data = this.combine_two_intdata(this.Complete_Data,this.third_data);// 第三包结合
            this.Complete_Data = this.combine_two_intdata(this.Complete_Data,this.forth_data);// 第四个包结合
            return this.Complete_Data;
        }
        else{
            return null;
        }

    }

    /**
     *
     * @param a  结合前面的int型数组
     * @param b  结合后面的int型数组
     * @return   a+b的结合的int型数组
     */
    public int[] combine_two_intdata(int a[],int b[]) {

        ArrayList<Integer> alist = new ArrayList<Integer>(a.length + b.length);

        for (int j = 0; j < a.length; j++) {
            alist.add(a[j]);
        }

        for (int k = 0; k < b.length; k++) {
            alist.add(b[k]);
        }

        int c[] = new int[alist.size()];

        for (int i = 0; i < alist.size(); i++) {
            c[i] = alist.get(i);
        }
        return c;
    }



}
