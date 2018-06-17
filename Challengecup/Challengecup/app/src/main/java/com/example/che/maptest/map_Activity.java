package com.example.che.maptest;

/**
 * Created by chehongshu on 2017/4/18.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.example.che.maptest.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONObject;

import static com.example.che.maptest.blue_tooth_Activity.rece_data_Complete;
import static com.example.che.maptest.blue_tooth_Activity.rece_data_Lat_Lon;

public class map_Activity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    static String sendData_server_string=null;
    //显示地图需要的变量
    private MapView mapView;//地图控件
    private AMap aMap;//地图对象


    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器
    AMapLocation amapLocation_change=null;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    Thread thread_location;


    Socket_Client socket_server = null ;
    static  public  int alarm = 0;//  警报


            /*
            1是 危险  -1是迫降   0是平常
            */
            Handler alarm_handle = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                     if (msg.what == 1) {
                         ToastUtil.showToast(map_Activity.this,"出现危险");
                     }else if (msg.what ==0){

                     }else if (msg.what ==-1)
                     {
                         ToastUtil.showToast(map_Activity.this, "迫降");
                     }
                }
            };


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        setContentView(R.layout.map_activity_main);
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        //显示地图
        mapView = (MapView) findViewById(R.id.map);
        //必须要写
        mapView.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);// 不知道为什么 加上就好用了
        //获取地图对象
        aMap = mapView.getMap();
        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);


        initLoc();
    /*
        //定位的小图标 默认是蓝点 这里自定义一团火，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.firetwo));
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle)
    */


             // 来个线程
        thread_location = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     while(true)
                     {

                         try {

                             thread_location.sleep(1500);//  延迟一秒
                             Data_conversion map_conversion = new Data_conversion();

                             double rece_Latitude  = map_conversion.get_Latitude(rece_data_Lat_Lon); // 得到纬度
                             double rece_Longitude = map_conversion.get_Longitude(rece_data_Lat_Lon);//得到精度
//
                             JSONObject json_send_data = new JSONObject();
//                            json_send_data.put("Latitude",rece_Latitude);
//                           json_send_data.put("Longitude",rece_Longitude);
//                             sendData_server_string = json_send_data.toString();

                             json_send_data.put("rece_data_Complete",rece_data_Complete);// int型数组  需要在 服务器端解析
                             sendData_server_string = json_send_data.toString();
                             Log.e("收到的数据",sendData_server_string);
                             // 这个线程一直在跑
                             //Log.e("（地图的线程）地图那边要发送的数据发送的数据是",sendData_server_string);


                             //Log.e("（地图的线程）地图那边得到的纬度",rece_Latitude+"");
                             //Log.e("（地图的线程）地图那边得到的经度",rece_Longitude+"");

                             LatLng marker1 = new LatLng(rece_Latitude, rece_Longitude);// 先定义自定义的经度纬度地区

                             //设置中心点和缩放比例
                             aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker1)); //  将位置移动到此处（此自定义的经纬度地区）
                             aMap.moveCamera(CameraUpdateFactory.zoomTo(19));// 缩放最大  即  离地面最近

                             //添加图钉
                             aMap.addMarker(getMarkerOptions_change(amapLocation_change,rece_Latitude,rece_Longitude));// 添加所在点的标志

                             // amapLocation_change.setLatitude(rece_Latitude);  //设置纬度
                             // amapLocation_change.setLongitude(rece_Longitude); //设置经度

                         }catch (Exception e)
                         {
                             e.printStackTrace();
                         }
                     }
                 }
        });

        thread_location.start();     // 线程开始跑

        // 来个子线程(发送线程  socket线程  若socket 连接不上则会跳出apk)
        Thread thread_send_receive = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {

                    try {
                        Thread.sleep(1000);// 之前1500ms
                        socket_server = new Socket_Client("123.206.176.72", 8001);
                        System.out.println("（socket的线程）准备发送了");
                        socket_server.sendMsg(sendData_server_string);
                        System.out.println("（socket的线程）发送成功了");

                        System.out.println("准备接受了");
                        String rece_data = socket_server.receMsg();


                        if(rece_data!=null) {
                            System.out.println("接受成功了" + rece_data);
                            //首先从服务器得到String类型数据recedata
                            JSONObject Back_jsonObject =  JSONObject.fromObject(rece_data);//将接受的string类型数据转换为原来的JSONObject类型

                            alarm=Back_jsonObject.getInt("st");//取出String类型
                            Log.e("alarm",alarm+"");
                            Message alarm_message = new Message();// 创建一个 alarm_message
                            alarm_message.what = alarm;
                            alarm_handle.sendMessage(alarm_message);
                        }
                        else {
                            System.out.println("接受失败了");
                        }// System.out.println("（socket的线程）接受的信息为"+socket_server.receMsg());
                        Thread.sleep(1000);// 之前1500ms
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }finally {

                        socket_server.closeSocket();
                    }
                }
            }
        });
        thread_send_receive.start();


//

        //Toast.makeText(getApplicationContext(),"纬度是"+rece_Latitude+" "+"经度是"+rece_Longitude  , Toast.LENGTH_LONG).show();
    }
    //定位
    private void initLoc() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                double latitude =  amapLocation.getLatitude();//获取纬度
                double longitude =  amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码
               Toast.makeText(getApplicationContext(),"纬度是"+latitude+" "+"经度是"+longitude  , Toast.LENGTH_LONG).show();

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(19));// 最大19  越大离地面越近
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //添加图钉
                    aMap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }


            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private MarkerOptions getMarkerOptions_change(AMapLocation amapLocation,
                                                  double Latitude ,double Longitude) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        // options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.fire));
        //位置
        options.position(new LatLng(Latitude , Longitude));

        StringBuffer buffer_change = new StringBuffer();
        buffer_change.append("纬度是"+Latitude+" "+"精度是"+Longitude);
        //标题
        options.title(buffer_change.toString());
        //设置多少帧刷新一次图片资源
        options.period(60);

        return options;

    }


    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private MarkerOptions getMarkerOptions(AMapLocation amapLocation) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        // options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.fire));
        //位置
        options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        StringBuffer buffer = new StringBuffer();
        buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() +  "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
        //标题
        options.title(buffer.toString());
        //子标题
        options.snippet("这里好火");
        //设置多少帧刷新一次图片资源
        options.period(60);
        return options;

    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }

                    /**
                     * 方法必须重写
                     */
                    @Override
                    public void onResume() {
                        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                        super.onResume();
                        mapView.onResume();


                        }


                        /**
                         * 方法必须重写
                         */
                        @Override
                        public void onPause() {
                            super.onPause();
                            mapView.onPause();
                        }

                    /**
                     * 方法必须重写
                     */
                    @Override
                    public void onSaveInstanceState(Bundle outState) {
                        super.onSaveInstanceState(outState);
                        mapView.onSaveInstanceState(outState);
                    }

                        /**
                         * 方法必须重写
                         */
                        @Override
                        protected void onDestroy() {
                            super.onDestroy();
                            mapView.onDestroy();


                        }




}


