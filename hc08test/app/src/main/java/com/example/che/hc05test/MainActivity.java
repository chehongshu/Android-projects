package com.example.che.hc05test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
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
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//  ble、的
public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    Vibrator vib;                         //手机系统震动 对象

    private static final int REQUEST_ENABLE_BT = 1;  //  蓝牙
    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 1000;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private boolean mScanning;

    Button btnConnect;   //连接蓝牙设备
    Button btndisconnect;
    Button btnSearch;    //搜索蓝牙设备
    Button btnJDQOn,btnJDQOff;
    Button btnPlayMusic;
    Button btnLEDOn,btnLEDOff;
    Button btnGetTemp;
    Button btnDigitalLed;
    Spinner spinner;
    List<String> deviceNamelist,addressList;
    List<BluetoothGattService> bleServiceList;

    ArrayAdapter<String> adapter;
    private BluetoothManager mBluetoothManager;  		//蓝牙管理器
    private BluetoothAdapter mBluetoothAdapter;  		//蓝牙适配器
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;        		//蓝牙Gatt
    private int mConnectionState = STATE_DISCONNECTED;  //默认为：未连接
    ArrayList<BluetoothDevice> mLeDevices;
    BluetoothDevice mDevice;
    BluetoothGattCharacteristic rs232_out_characteristic,rs232_in_characteristic;

    /*
	 *   显示从蓝牙设备接收到的数据
	 * */
    public void show_result(byte[] buffer,int count)
    {
        StringBuffer msg = new StringBuffer();                                //创建缓冲区
        TextView tvInfo = (TextView)findViewById(R.id.tvInfo);   //创建 文本显示对象
        tvInfo.setText("");  //清空对象内容
        try {
            for (int i = 0; i < count; i++)                                       //循环 加入 数据，16进制 格式
                msg.append(String.format("%c", buffer[i]));
        }catch(Exception e) {

        }
        msg.append("\r\n");
        try {
            for (int i = 0; i < count; i++)                                       //循环 加入 数据，16进制 格式
                msg.append(String.format("0x%x ", buffer[i]));
        }catch (Exception e) {

        }
        tvInfo.setText(msg);		                                           //显示到界面上
    }
    //  利用handler更新UI
    Handler bluetoothMessageHandle = new Handler() {            //蓝牙消息 handler 对象
        public void handleMessage(Message msg) {
            if (msg.what == 0x1234) {                             //如果消息是 0x1234,则是从 线程中 传输过来的数据
                show_result((byte [])msg.obj,msg.arg1);                    //将 缓冲区的数据显示到 UI
            }
        }
    };
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //将扫描到设备添加到列表中
                            mLeDevices.add(device);
                            deviceNamelist.add(device.getName());
                            addressList.add(device.getAddress());
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    //  发送命令的
    void send_cmd(byte[] cmd,String tips) {
        if (rs232_out_characteristic == null) {
            Toast.makeText(getApplicationContext(), "蓝牙设备没有配置正确", Toast.LENGTH_SHORT).show();
            return;
        }
        rs232_out_characteristic.setValue(cmd);//  发出数据
        if (mBluetoothGatt.writeCharacteristic(rs232_out_characteristic)) {
            Toast.makeText(getApplicationContext(), tips + "命令成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), tips + "命令失败", Toast.LENGTH_SHORT).show();
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
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toast.makeText(getApplicationContext(), "蓝牙搜索完成", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
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

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            boolean in_ok = false,out_ok = false;
            String strRs232_out_servier_Uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
            String strRs232_out_chars_uuid   = "0000ffe1-0000-1000-8000-00805f9b34fb";

            String strRs232_in_servier_Uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
            String strRs232_in_chars_uuid   = "0000ffe1-0000-1000-8000-00805f9b34fb";

            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleServiceList = gatt.getServices();
                for (BluetoothGattService bs : bleServiceList) {
                    String strUuid = bs.getUuid().toString();
                    if (strRs232_out_servier_Uuid.equals(strUuid)) {   //找到指定的串口输出服务
                        List<BluetoothGattCharacteristic>list_chars = bs.getCharacteristics();
                        for (BluetoothGattCharacteristic achars : list_chars) {
                            String strCharUUid = achars.getUuid().toString();
                            if (strRs232_out_chars_uuid.equals(strCharUUid)) {
                                rs232_out_characteristic = bs.getCharacteristic(achars.getUuid());
                                out_ok = true;
                            }
                        }
                    }

                    if (strRs232_in_servier_Uuid.equals(strUuid)) {   //找到指定的串口输出服务
                        List<BluetoothGattCharacteristic>list_chars = bs.getCharacteristics();
                        for (BluetoothGattCharacteristic achars : list_chars) {
                            String strCharUUid = achars.getUuid().toString();
                            if (strRs232_in_chars_uuid.equals(strCharUUid)) {
                                rs232_in_characteristic = bs.getCharacteristic(achars.getUuid());
                                mBluetoothGatt.setCharacteristicNotification(rs232_in_characteristic, true);
                                in_ok = true;
                            }
                        }
                    }
                }
                if (in_ok && out_ok) {      //搜索到蓝牙设备了
                    vib.vibrate(100);
                }
            } else {
               // Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] buf = new byte[24];
            Message msg = new Message();          //定义一个消息,并填充数据
            msg.what = 0x1234;
            buf = characteristic.getValue();
            msg.obj = buf;
            msg.arg1 = buf.length;
            bluetoothMessageHandle.sendMessage(msg);          //通过handler发送消息
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
        }
    };
    // 释放资源
    private void release_resource() {
        if (mConnectionState != STATE_CONNECTED)
            return;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }
    //
    protected void onDestroy() {
        super.onDestroy();
        release_resource();   //释放资源
        Toast.makeText(getApplicationContext(), "蓝牙App连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        mLeDevices = new ArrayList<BluetoothDevice>();

        vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);   //获取手机震动对象

        spinner = (Spinner)findViewById(R.id.spinner1);       //获取 下拉框控件 对象
        deviceNamelist = new ArrayList<String>();                   //创建列表，用于保存蓝牙设备地址
        addressList = new ArrayList<String>();
        bleServiceList = new ArrayList<BluetoothGattService>();

        //创建数组适配器
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item,deviceNamelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	//设置 下来显示方式
        spinner.setAdapter(adapter);


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
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }

                //清空LE设备列表
                deviceNamelist.clear();
                mLeDevices.clear();
                addressList.clear();
                bleServiceList.clear();

                release_resource();  //释放资源

                scanLeDevice(true);   //搜索LE设备
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
                mDevice = mLeDevices.get(spinner.getSelectedItemPosition());
                if (mDevice == null) {
                    Toast.makeText(getApplicationContext(), "错误的设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), false, mGattCallback);

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

//        btnJDQOn = (Button)findViewById(R.id.btnJDQOn);
//        btnJDQOn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                if (mConnectionState != STATE_CONNECTED) {
//                    Toast.makeText(getApplicationContext(), "蓝牙没有连接，无法操作", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                byte []cmd = new byte[1];
//                cmd[0] = (byte)'c';  //协议头1
//
//                send_cmd(cmd,"继电器开");
//            }
//        });

//        btnJDQOff = (Button)findViewById(R.id.btnJDQOff);
//        btnJDQOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                if (mConnectionState != STATE_CONNECTED) {
//                    Toast.makeText(getApplicationContext(), "蓝牙没有连接，无法操作", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                /******************/
//                byte []cmd = new byte[1];
//                cmd[0] = (byte)'d';  //协议头1
//                /******************/
//
//                send_cmd(cmd,"继电器关");
//            }
//        });

//        btnLEDOn = (Button)findViewById(R.id.btnLEDOn);
//        btnLEDOn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                if (mConnectionState != STATE_CONNECTED) {
//                    Toast.makeText(getApplicationContext(), "蓝牙没有连接，无法操作", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                byte []cmd = new byte[1];
//                cmd[0] = (byte)'a';  //协议头1
//
//                send_cmd(cmd,"继电器开");
//            }
//        });


    }
}
