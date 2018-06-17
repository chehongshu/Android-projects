package com.example.che.maptest;

/**
 * Created by chehongshu on 2017/4/11.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.che.maptest.map_Activity.alarm;

//  ble、的
public class blue_tooth_Activity extends Activity {
    public static String rece_data_Lat_Lon="30 120";
    public static int[] rece_data ;
    public static int[] rece_data_Complete ;
    public static String rece_data_String;
    private Handler mHandler;
    Vibrator vib;                         //手机系统震动 对象//  震动表示连接上
    //以下相当于宏定义  static final  相当于  宏定义的全部变量。
    private static final int REQUEST_ENABLE_BT = 1;  //  蓝牙
    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 5000; //  扫描时长
    // 三种 宏定义
    private static final int STATE_DISCONNECTED = 0;//  状态为没有连接上
    private static final int STATE_CONNECTING = 1;//  状态为正在连接
    private static final int STATE_CONNECTED = 2; //状态为连接上了
    static boolean flag=false;
    static  int shu=1;
    static  int lanya=1;
    private boolean mScanning;//   正在扫描
  //  Button change_button;

    Button btnConnect;   //连接蓝牙设备
    Button btndisconnect;
    Button btnSearch;    //搜索蓝牙设备
    Button btnsend;
    Spinner spinner; //  显示设备区域
    List<String> deviceNamelist,addressList; //  设备名字和设备地址的集合
    List<BluetoothGattService> bleServiceList;   //ble 蓝牙设备的集合

    ArrayAdapter<String> adapter;
    private BluetoothManager mBluetoothManager;  		//蓝牙管理器
    private BluetoothAdapter mBluetoothAdapter;  		//蓝牙适配器
    private String mBluetoothDeviceAddress;  //  蓝牙的设备的地址
    private BluetoothGatt mBluetoothGatt;        		//蓝牙Gatt
    private int mConnectionState = STATE_DISCONNECTED;  //默认为：未连接
    ArrayList<BluetoothDevice> mLeDevices;  //  存放蓝牙设备的数组
    BluetoothDevice mDevice;   //  蓝牙设备对象
    BluetoothGattCharacteristic rs232_out_characteristic,rs232_in_characteristic;
    Ble_Cachedata_handle  ble_cachedata_handle = new Ble_Cachedata_handle();
    /**
     *
     * @param buffer
     * @param count
     * @return
     */
    ///   打成一个类
    public String byte_String(byte[] buffer,int count)
    {
        StringBuffer msg = new StringBuffer();          //创建缓冲区
        String rece_data=null;
        //甩出异常
        try {
            for (int i = 0; i < count; i++)    //循环 加入 数据，十进制
                // StringBuffer,append-->连接一个字符串到末尾
                // String,format相当于重载。
                msg.append(String.format("%c", buffer[i]));//  现实的正常的数据
                rece_data=msg.toString();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return rece_data;
    }

    /*
	 *   显示从蓝牙设备接收到的数据 buffer  显示的数据
	 * */
    //达成一个类
    public void show_result(int[] buffer,int count)
    {
        StringBuffer msg = new StringBuffer();          //创建缓冲区
        TextView tvInfo = (TextView)findViewById(R.id.ble_showdata);   //创建 文本显示对象
        tvInfo.setText("");  //清空对象内容
        //甩出异常
        try {
            for (int i = 0; i < count; i++)    //循环 加入 数据，十进制
                // StringBuffer,append-->连接一个字符串到末尾
                // String,format相当于重载。
                msg.append(String.format("%c", buffer[i]));//  现实的正常的数据
        }catch(Exception e) {

        }
        msg.append("\r\n"); //  换行
        try {
        for (int i = 0; i < count; i++)       //循环 加入 数据，16进制 格式
            msg.append(String.format("0x%x ", buffer[i]));//  显示的二进制数字
    }catch (Exception e) {

    }
        tvInfo.setText(msg);		                                           //显示到界面上
    }

    //  利用handler更新UI
    Handler bluetoothMessageHandle = new Handler() {            //蓝牙消息 handler 对象
        public void handleMessage(Message msg) {
            //MSG  的属性相当于 这样可以一起处理很多个 msg
            if (msg.what == 0x1234) {                             //如果消息是 0x1234,则是从 线程中 传输过来的数据
                show_result((int [])msg.obj,msg.arg1);                    //将 缓冲区的数据显示到 UI
            }
        }
    };








    // Device scan callback.   扫描之后 返回callack  对象
    // 在蓝牙扫描过程中，发现一个设备，就会回调一次
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
      // 会初始化一个device对象 ; 当一个LE设备被发现的时候 , 这个对象device作为参数传递进来 ,
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //将扫描到设备添加到列表中
                                if(device.getName().equals("BTO5")) {
                                    mLeDevices.add(device);
                                    deviceNamelist.add(device.getName());
                                    addressList.add(device.getAddress());
                                    adapter.notifyDataSetChanged();
                                }
                            }catch (Exception e)
                            {
                                  e.printStackTrace();
                            }
                        }
                    });
                }
            };

    //  发送命令的
    void send_cmd(byte[] cmd,String tips) {
        if (rs232_out_characteristic == null) {
          //  Toast.makeText(getApplicationContext(), "蓝牙设备没有配置正确", Toast.LENGTH_SHORT).show();
            return;
        }
        rs232_out_characteristic.setValue(cmd);
        //  发出数据
        if (mBluetoothGatt.writeCharacteristic(rs232_out_characteristic)) {
           // Toast.makeText(getApplicationContext(), tips + "命令成功", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(getApplicationContext(), tips + "命令失败", Toast.LENGTH_SHORT).show();
        }
        vib.vibrate(100);  //震动
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //  扫描设备
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period. 延时
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);//  停止扫描
                    Toast.makeText(getApplicationContext(), "蓝牙搜索完成", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);//  开始扫描
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//  停止扫描
        }
    }

    //  创建callback对象，获取连接的数据
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();  //连接成功则 搜索服务

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        //  设备被发现    //当服务发现之后回调这里
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            boolean in_ok = false,out_ok = false;
            String strRs232_out_servier_Uuid = "0000ffe0-0000-1000-8000-00805f9b34fb"; //服务UUID
            String strRs232_out_chars_uuid   = "0000ffe1-0000-1000-8000-00805f9b34fb"; //特征UUID

            String strRs232_in_servier_Uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";//服务UUID
            String strRs232_in_chars_uuid   = "0000ffe1-0000-1000-8000-00805f9b34fb"; //特征UUID

            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleServiceList = gatt.getServices();
                for (BluetoothGattService bs : bleServiceList) {
                    String strUuid = bs.getUuid().toString();
                    if (strRs232_out_servier_Uuid.equals(strUuid)) {
                        //找到指定的串口输出服务
                        List<BluetoothGattCharacteristic>list_chars = bs.getCharacteristics();
                        for (BluetoothGattCharacteristic achars : list_chars) {
                            String strCharUUid = achars.getUuid().toString();
                            if (strRs232_out_chars_uuid.equals(strCharUUid)) {
                                rs232_out_characteristic = bs.getCharacteristic(achars.getUuid());
                                out_ok = true;
                            }
                        }
                    }
                    if (strRs232_in_servier_Uuid.equals(strUuid)) {
                        //找到指定的串口输出服务
                        List<BluetoothGattCharacteristic>list_chars = bs.getCharacteristics();
                        for (BluetoothGattCharacteristic achars : list_chars) {
                            String strCharUUid = achars.getUuid().toString();
                            if (strRs232_in_chars_uuid.equals(strCharUUid)) {
                                rs232_in_characteristic = bs.getCharacteristic(achars.getUuid());
                                // //通过判断，打开Notification 通知，提醒。一般是设备测量完成了之后会发送对应的数据上来。
                                mBluetoothGatt.setCharacteristicNotification(rs232_in_characteristic, true);
                                in_ok = true;
                            }
                        }
                    }
                }
                if (in_ok && out_ok) {
                    //搜索到蓝牙设备了
                    vib.vibrate(100);//  震动
                }
            } else {
               // Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override  //  可以读取蓝牙的值      读取特征值回调
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("ble","读取特征值回调");

        }

        @Override  //回调函数      特征值改变回调
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {

            byte[] buf = new byte[64];
            Message msg = new Message();          //定义一个消息,并填充数据
            msg.what = 0x1234;
            //1.  得到传输过来的值  Byte  16进制数组
            buf = characteristic.getValue();
            Log.e("接受的数据的长度",buf.length+"/.");
            //rece_data
            //  传的数据
            //进行校验
            //创建 数据转换方法的类
            //2.  将受到的数据转换为int型数组
            receData_handle handle_recedata_ways = new receData_handle();
            // 将蓝牙接收到的信息  byte[]  转换为 数据不超 大小的int[]''
            rece_data = handle_recedata_ways.negative_Bytetoint(buf);

            //3.将得到的数据进行整合
            ble_cachedata_handle.rece_all_data_handle(rece_data);
            //4. 得到全部数据处理之后再进入真正处理阶段
            if(ble_cachedata_handle.get_complete_receData()!=null)
            {
                // 进行处理
                //5.得到经纬度的firstdata
                int[] first_data=new int[20];
                       first_data = ble_cachedata_handle.getFirst_data();
                //6.得到经纬度
                float rece_Lat = handle_recedata_ways.getrece_Lat(first_data);//  纬度 30
                float rece_Lon = handle_recedata_ways.getrece_Lon(first_data);//  经度 120

                Log.e("（蓝牙的线程）蓝牙那边得到的数据的纬度",rece_Lat+"");
                Log.e("（蓝牙的线程）蓝牙那边得到的数据的经度",rece_Lon+"");
                System.out.println("（蓝牙接受信息的线程）蓝牙这边还在===========================蓝牙这边还在");
                //7.得到的经纬度的string  往地图activity上发送
                rece_data_Lat_Lon = rece_Lat+" "+rece_Lon;
                //8.得到全部的 data
                rece_data_Complete = ble_cachedata_handle.get_complete_receData();
                Log.e("（蓝牙的线程）蓝牙那边得到的数据的纬度", Arrays.toString(rece_data_Complete));
                // 向服务器传的值
                // rece_data_String =  rece_data_Complete.toString();


                // }
                //  赋给一个全局变量
                // rece_data = byte_String(buf,buf.length);

                // Log.e("收到的信息",rece_data);
                msg.obj = rece_data_Complete; //  得到的 String
                msg.arg1 = rece_data_Complete.length;
                bluetoothMessageHandle.sendMessage(msg);  //通过handler发送消息之后在UI上显示
            }



        }

        @Override   // 发现服务回调
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
        }

    };
    // 释放资源
    private void release_resource() {
        if (mConnectionState != STATE_CONNECTED)
            return;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();  //  取消连接
            mBluetoothGatt.close();   //  释放 蓝牙对象
            mBluetoothGatt = null;   //  蓝牙 清空
        }
    }
    // 每次利用tabhost切换界面的时候会出发这句话所以在这句话中不进行相应的  释放资源处理即可
    protected void onDestroy() {
        System.out.println("进行了释放资源 草草草草哦啊哦草哦啊哦草哦啊哦凑");
        super.onDestroy();
        //  去掉释放资源
        //release_resource();   //释放资源
        //Toast.makeText(getApplicationContext(), "蓝牙App连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bluetooth_main);
        // 对 byte 蓝牙数据初始化
        rece_data = new int[68];

        mHandler = new Handler();

        mLeDevices = new ArrayList<BluetoothDevice>(); // 设备集合

        vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);   //获取手机震动对象

        spinner = (Spinner)findViewById(R.id.spinner1);       //获取 下拉框控件 对象
        deviceNamelist = new ArrayList<String>();                   //创建列表，用于保存蓝牙设备地址
        addressList = new ArrayList<String>();         // 地址集合
        bleServiceList = new ArrayList<BluetoothGattService>();


        //创建数组适配器
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item,deviceNamelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	//设置 下来显示方式
        spinner.setAdapter(adapter);
        //change_button = (Button)findViewById(R.id.change_button);
//        //  到另一个界面
//        change_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent change_tooth_map= new Intent(blue_tooth_Activity.this, map_Activity.class);    //切换Login Activity至User Activity
////           Bundle bundle = new Bundle();
////           bundle.putString("Latitude_and_longitude",rece_data);
////           change_tooth_map.putExtras(bundle);
////           Intent intent = getIntent();
////           station = intent.getExtras().get("data");
//
//                startActivity(change_tooth_map);// 转换   activity
//
//            }
//        });



          // 来个线程一直蓝牙发送给下位机数据
        Thread blue_send_zizhou = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {

                        if(mConnectionState == STATE_CONNECTED) {
                            Thread.sleep(1000);   //    堵塞一秒
                            System.out.println(mConnectionState + "");
                            if (mConnectionState == STATE_CONNECTED) {
                                //shu++;
                                byte[] cmd = new byte[1];
                                cmd[0] = (byte) '1';  //协议头1
                                String shuzi = alarm + "";
                                int ia[] = new int[shuzi.length()];   //字符串长度申明一个int数组
                                for (int i = 0; i < shuzi.length(); i++) {
                                    char c = shuzi.charAt(i);   //逐个获取字符串中的字符
                                }
                                for (int i = 0; i < shuzi.length(); i++) {
                                    char c = shuzi.charAt(i);   //逐个获取字符串中的字符
                                    ia[i] = (int) (c - '0');    //字符数字-字符0就是实际的数字值，赋值给数字数组
                                }

                                Data_conversion conversion = new Data_conversion();

                                for (int j = 0; j < ia.length; j++) {

                                    cmd = conversion.t1((long) ia[j]).getBytes();//   10进制变为 16进制
                                    send_cmd(cmd, "哈哈");
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        blue_send_zizhou.start();

        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
           // 1. 开启蓝牙的过程
            @Override
            public void onClick(View arg0) {
                //判断系统是否支持 蓝牙 BLE 4.0
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(getApplicationContext(), "不支持蓝牙4.0BLE功能，App即将退出！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                mBluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = mBluetoothManager.getAdapter();

                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "不支持蓝牙功能,即将退出", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (!mBluetoothAdapter.isEnabled()) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);// 选择 开始  蓝牙
                    }
                }

                //清空LE设备列表
                deviceNamelist.clear(); //  设备的名字清除
                mLeDevices.clear();     //
                addressList.clear();    //  设备的地址   清除
                bleServiceList.clear(); // ble  设备  储存 清除

                release_resource();     //释放资源

                scanLeDevice(true);     //搜索LE设备
            }
        });



        // 连接蓝牙
        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (addressList.size() < 1) {
                    Toast.makeText(getApplicationContext(), "没有搜索到蓝牙设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                String address = addressList.get(spinner.getSelectedItemPosition());
                mDevice = mLeDevices.get(spinner.getSelectedItemPosition());//  spinner  所选的设备。
                if (mDevice == null) {
                    Toast.makeText(getApplicationContext(), "错误的设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), false, mGattCallback);// 连接过程

            }
        });





        //  断开连接操作
        btndisconnect = (Button)findViewById(R.id.btndisconnect);
        btndisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mConnectionState != STATE_CONNECTED)
                    return;
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                Toast.makeText(getApplicationContext(), "蓝牙断开连接", Toast.LENGTH_SHORT).show();
            }
        });







//  测试发送按键
        btnsend = (Button)findViewById(R.id.btnsend);
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mConnectionState != STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "蓝牙没有连接，无法操作", Toast.LENGTH_SHORT).show();
                    return;
                    }
    //                flag=true;
    //                Log.e("按键F",flag+"");

                     byte []cmd = new byte[1];

                    String shuzi=shu+"";
                    int ia[] = new int[shuzi.length()];//字符串长度申明一个int数组
                    for(int i=0;i<shuzi.length();i++){
                        char c = shuzi.charAt(i);//逐个获取字符串中的字符
                    }
                    for(int i=0;i<shuzi.length();i++){
                        char c = shuzi.charAt(i);//逐个获取字符串中的字符
                        ia[i]=(int)(c-'0');//字符数字-字符0就是实际的数字值，赋值给数字数组
                    }
                    Data_conversion conversion = new Data_conversion();

                    for(int j=0;j<ia.length;j++) {

                        cmd = conversion.t1((long)ia[j]).getBytes();//   10进制变为 16进制
                        send_cmd(cmd,"哈哈");
                    }

            }
        });




    }

    @Override
    public void onPause() {
        System.out.println("蓝牙的程序暂停了");
        super.onPause();  // Always call the superclass method first
        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.

    }
    //开启一个子线程
    @Override
    protected void onResume() {
        /**
         * 设置为竖屏
         */
        System.out.println("重新启动了");
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }

}
