package com.example.che.maptest;

/**
 * Created by chehongshu on 2017/4/15.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

/**
 *   数值转换的  ways
 */
public class Data_conversion {

    //把a转成指定进制
    public String t2(int a,int n){
        String str = "";
        //1:用a去除以n，得到商和余数
        int sun = a/n;
        int yuShu = a%n;
        str = ""+shuZhiToZhiMu(String.valueOf(yuShu));
        while(sun > 0 ){
            //2：继续用商去除以n，得到商和余数
            yuShu = sun % n;
            sun = sun / n;
            //3：如果商为0，那么就终止
            //4：把所有的余数倒序排列
            str = shuZhiToZhiMu(String.valueOf(yuShu)) + str;
        }
        System.out.println(n+"进制==="+str);
        return str;
    }
    //写一个方法实现：把一个十进制的数转换成为16进制的数
    public String t1(Long a){
        String str = "";
        //1:用a去除以16，得到商和余数
        Long sun = a/(16L);
        Long yuShu = a%16L;
        str = ""+shuZhiToZhiMu(String.valueOf(yuShu));
        while(sun > 0 ){
            //2：继续用商去除以16，得到商和余数
            yuShu = sun % 16;
            sun = sun / 16;
            //3：如果商为0，那么就终止
            //4：把所有的余数倒序排列
            str = shuZhiToZhiMu(String.valueOf(yuShu)) + str;
        }
        System.out.println("16进制==="+str);
        return str;
    }
    private String shuZhiToZhiMu(String a){
        switch(a){   //若jdk版本switch不支持String,可以将String转换为int类型后判断
            case "10" :
                return "a";
            case "11" :
                return "b";
            case "12" :
                return "c";
            case "13" :
                return "d";
            case "14" :
                return "e";
            case "15" :
                return "f";
        }
        return ""+a;
    }

    /**
     * 经度  纬度都为double类型
     */
    /**
     * String中间有空格 以这个为分界线分解成两个 string
     *    字符串前面 为获得的纬度
     * @return   前面的
     */
    public double get_Latitude(String ss)
    {
        double rece_Latitude;
        String[] as = ss.split(" ");
        rece_Latitude = Double.valueOf(as[0]);
        return rece_Latitude;
    }

    /**
     * String中间有空格 以这个为分界线分解成两个 string
     *    字符串前面 为获得的  经度
     * @return   前面的
     */
    public  double get_Longitude(String ss)
    {
        double rece_Longitude;
        String[] as = ss.split(" ");
        rece_Longitude = Double.valueOf(as[1]);
        return rece_Longitude;
    }
}
