package com.example.che.uwb_che;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import static com.example.che.uwb_che.tools.binaryToHexString;
import static com.example.che.uwb_che.tools.dialog;
import static com.example.che.uwb_che.tools.find;
import static com.example.che.uwb_che.tools.isWifiConnected;
import static com.example.che.uwb_che.tools.spGet;
import static com.example.che.uwb_che.tools.toast;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {
    private TextView tv_log;
    private Log log;
    private boolean threadFlag;
    private boolean Led_Flag;
    private boolean Device_Flag;
    private boolean Catch_Flag;
    private Thread workThread;
    private SocketClient socket;
    private String url;
    private int port;
    private int state;

    //自检信息
    private int Power;
    private String STA;

    //Textview
    private TextView Time_tv;
    private TextView Temperature_tv;
    private TextView Power_tv;
    private TextView Depth_tv;
    private TextView DownY91_tv;
    private TextView UpY91_tv;
    private TextView Ph_tv;
    private TextView ax_tv;
    private TextView ay_tv;
    private TextView az_tv;
    private TextView wx_tv;
    private TextView wy_tv;
    private TextView wz_tv;
    private TextView roll_tv;
    private TextView pitch_tv;
    private TextView yaw_tv;

    private static final class STATE {
        public static final int INIT = 0;
        public static final int WIFI_NO_CON = 1000;
        public static final int CONNED = 1001;
    }

    ////++
    public enum FrameStatus
    {
        FRAME_IDLE,
        FRAME_SYNC1,
        FRAME_SYNC2,
        FRAME_TYPE,
        FRAME_DATE,
        FRAME_SUM,
        FRAME_ERROR,
        FRAME_OK
    }
    public class InstrType {

        public static final int INSTR_SelfCheck = 0x01;//自检

        public static final int  INSTR_GPS = 0x02;//GPS

        public static final int INSTR_JY901 = 0x03;//九轴

        public static final int INSTR_ENCODER = 0x05;//编码器

        public static final int INSTR_CABLE = 0x55;

        public static final int INSTR_RETRIEVAL = 0xFF;//应答

        public static final int INSTR_DEPTH = 0x06;// 深度

        public static final int INSTR_PH = 0x07;// ph

        public static final int INSTR_TEMPREATURE = 0x08;// 温度

    }
    private boolean IsReceiveStoped = false;               // 停止接收按钮的命令状态
    private boolean IsSerialPortOpen = false;              // 串口开启状态
    private boolean IsOpenSerialPortCommand = true;        // 按钮“打开串口”的行为
    private int OpenSerialPortRetryTimes = 0;         // 打开串口重试次数

    private int sync_header1 = 0xEB;
    private int sync_header2 = 0x90;

    private byte instr_selfcheck = 0x46;
    private byte instr_jy901 = 0x55;
    private byte instr_gps   = 0x36;

    private static int MAX_REC_LENGTH = 256;
    private FrameStatus currentFrameStatus = FrameStatus.FRAME_IDLE;
    private byte sum = 0x00;
    private int index = 0;
    private int length = 0;
    private int currentInstrType;
    private int[] DataBuffer = new int[MAX_REC_LENGTH];

    // jy901模块数据
    private float accx, accy, accz;
    private float gyrox, gyroy, gyroz;
    private float anglex, angley, anglez;

    private float depth;
    private float temperature;
    private float ph_value;

    // gps模块数据
    private double lat, lon;
    private double direction;

    // 时间戳
    private int timecount;
    private Timer rtctime;

    /////++
    //消息处理
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG.SOCKET_ERROR:
                    log.l("SOCKET异常：" + msg.obj);
                    log.l("重置SOCKET");
                    if (socket != null) {
                        try {
                            socket.closeSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socket = null;
                        state = STATE.INIT;
                    }
                    break;
                case MSG.READ_ERROR:
                    log.l("数据读取异常：" + msg.obj);
                    break;
                case MSG.SEND_ERROR:
                    log.l("数据发送异常：" + msg.obj);
                    break;
            }
        }
    };

    /////////////////////
    @SuppressLint("跟新UI数据")
    private Handler Uihandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            parameter p = (parameter)msg.obj;
            ///// update TextView
            ax_tv.setText(p.accx+"");
            ay_tv.setText(p.accy+"");
            az_tv.setText(p.accz+"");
            wx_tv.setText(p.gyrox+"");
            wy_tv.setText(p.gyroy+"");
            wz_tv.setText(p.gyroz+"");
            roll_tv.setText(p.anglex+"");
            pitch_tv.setText(p.angley+"");
            yaw_tv.setText(p.anglez+"");
            Temperature_tv.setText(p.temperature+"");
            Ph_tv.setText(p.ph+"");
            Depth_tv.setText(p.depth+"");
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
            String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
            Time_tv.setText(date);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind();
        init();
    }

    private void bind() {
        find(this, R.id.bt_left_up).setOnTouchListener(this);
        find(this, R.id.bt_up).setOnTouchListener(this);
        find(this, R.id.bt_right_up).setOnTouchListener(this);
        find(this, R.id.bt_left).setOnTouchListener(this);
        find(this, R.id.bt_o).setOnTouchListener(this);
        find(this, R.id.bt_right).setOnTouchListener(this);
        find(this, R.id.bt_left_down).setOnTouchListener(this);
        find(this, R.id.bt_down).setOnTouchListener(this);
        find(this, R.id.bt_right_down).setOnTouchListener(this);
        find(this, R.id.bt_shallow).setOnTouchListener(this);
        find(this, R.id.bt_deep).setOnTouchListener(this);
        find(this, R.id.bt_connect).setOnClickListener(this);
        find(this, R.id.bt_change).setOnClickListener(this);
        find(this, R.id.bt_led).setOnClickListener(this);
        find(this, R.id.bt_stop).setOnTouchListener(this);
        find(this, R.id.bt_catch).setOnClickListener(this);

        tv_log = find(this, R.id.tv_log);
        Time_tv = find(this, R.id.time_tv);
        Temperature_tv = find(this, R.id.Temperature_tv);
        Power_tv = find(this, R.id.power_tv);
        Depth_tv = find(this, R.id.depth_tv);
        DownY91_tv = find(this, R.id.downY91_tv);
        Ph_tv = find(this, R.id.ph_tv);
        UpY91_tv = find(this, R.id.upY91_tv);
        ax_tv = find(this, R.id.ax_tv);
        ay_tv = find(this, R.id.ay_tv);
        az_tv = find(this, R.id.az_tv);
        wx_tv = find(this, R.id.wx_tv);
        wy_tv = find(this, R.id.wy_tv);
        wz_tv = find(this, R.id.wz_tv);
        roll_tv = find(this, R.id.roll_tv);
        pitch_tv = find(this, R.id.pitch_tv);
        yaw_tv = find(this, R.id.yaw_tv);
    }

    private void init() {
        state = STATE.INIT;
        log = Log.getInstance(this, tv_log);
        threadFlag = false;
        Led_Flag = true;
        Device_Flag = true;
        Catch_Flag = true;
        log.l("程序启动");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (v.getId()) {
                    //根据不同的按钮，发送不同的指令
                    case R.id.bt_up:
                        if (Device_Flag) {
                            sendCommand(Command.Y91_left_max);
                            sendCommand(Command.Y91_right_max);
                            break;
                        } else {
                            sendCommand(Command.Y91_down_right_max);
                            sendCommand(Command.Y91_down_left_max);
                            break;
                        }
                    case R.id.bt_left_up:
                        sendCommand(Command.Gear_left);
                        //sendCommand(Command.);
                        break;
                    case R.id.bt_right_up:
                        sendCommand(Command.Gear_right);
                        break;
                    case R.id.bt_left:
                        if (Device_Flag) {
                            sendCommand(Command.Y91_right_max);
                            sendCommand(Command.Y91_left_min);
                        } else {
                            sendCommand(Command.Y91_down_right_max);
                            sendCommand(Command.Y91_down_left_min);
                        }
                        break;
                    case R.id.bt_o:
                        if (Device_Flag) {
                            sendCommand(Command.Y91_left_mid);
                            sendCommand(Command.Y91_right_mid);
                        } else {
                            sendCommand(Command.Y91_down_left_mid);
                            sendCommand(Command.Y91_down_right_mid);
                        }
                        break;
                    case R.id.bt_right:
                        if (Device_Flag) {
                            sendCommand(Command.Y91_left_max);
                            sendCommand(Command.Y91_right_min);
                        } else {
                            sendCommand(Command.Y91_down_left_max);
                            sendCommand(Command.Y91_down_right_min);
                        }
                        break;
                    case R.id.bt_left_down:
                        sendCommand(Command.Gear_o);
                        break;
                    case R.id.bt_down:
                        if (Device_Flag) {
                            sendCommand(Command.Y91_right_min);
                            sendCommand(Command.Y91_left_min);
                        } else {
                            sendCommand(Command.Y91_down_right_min);
                            sendCommand(Command.Y91_down_left_min);
                        }
                        break;
                    case R.id.bt_right_down:
                        //sendCommand(Command.);
                        break;
                    case R.id.bt_shallow:
                        sendCommand(Command.Y91_down_vertical_max);
                        break;
                    case R.id.bt_deep:
                        sendCommand(Command.Y91_down_vertical_min);
                        break;
                    case R.id.bt_stop:
                        sendCommand(Command.Y91_down_vertical_mid);
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_connect:
                if (state == STATE.INIT) {
                    ((TextView) v).setText("断开连接");
                    start();
                } else {
                    ((TextView) v).setText("连接设备");
                    stop();
                }
                break;
            case R.id.bt_change:
                if (Device_Flag == true) {
                    Device_Flag = false;
                    ((TextView) v).setText("水下设备");
                } else {
                    ((TextView) v).setText("水上设备");
                    Device_Flag = true;
                }
                break;
            case R.id.bt_led:
                if (Led_Flag == true) {
                    sendCommand(Command.LEDON);
                    Led_Flag = false;
                } else {
                    sendCommand(Command.LEDOFF);
                    Led_Flag = true;
                }
                break;
            case R.id.bt_catch:
                if (Catch_Flag == true) {
                    //sendCommand(Command.);
                    ((TextView) v).setText("放开");
                    Catch_Flag = false;
                } else {
                    //sendCommand(Command.);
                    ((TextView) v).setText("勾住");
                    Catch_Flag = true;
                }
                break;
        }
    }


    //开始准备工作
    private void start() {
        log.l("开始准备工作");
        //判断wifi状态
        if (!isWifiConnected(this)) {
            log.l("WIFI未连接");
            dialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    toast(MainActivity.this, "点击重新载入以重新加载程序", Toast.LENGTH_LONG);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    log.l("程序终止，请打开wifi后再试");
                    state = STATE.WIFI_NO_CON;
                }
            }, "提示", "wifi未连接，是否打开设置连接wifi？", "是", "否", -1);
        } else {
            //初始化主要参数
            url = (String) spGet(this, null, "url", "192.168.1.7");
            port = (int) spGet(this, null, "port", 8233);
            log.l("当前目标设备：" + url);
            log.l("当前目标端口：" + port);
            threadFlag = true;
            //初始化线程
            workThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.l("工作线程启动");
                    DataInputStream dis = null;
                    //初始化SOCKET
                    try {
                        if (socket != null) {
                            socket.closeSocket();
                        }
                        //尝试创建SOCKET
                        socket = new SocketClient(url, port);
                    } catch (Exception e) {
                        Message message = new Message();
                        message.what = MSG.SOCKET_ERROR;
                        message.obj = e.getMessage();
                        handler.sendMessage(message);
                    } finally {
                        try {

                            log.l("准备SOCKET缓冲区");
                            dis = new DataInputStream(socket.getInputStream());
                            state = STATE.CONNED;
                            log.l("SOCKET已创建");
                            log.l("与目标设备连接成功");
                            int readCount;
                            while (threadFlag) {
                                //工作线程主循环
                                try {
                                    byte[] buffer = new byte[dis.available()];
                                    ByteActualLength b = new ByteActualLength();
                                    readCount = b.returnActualLength(buffer);
                                    log.l(buffer[1]+"");
                                    log.l("接收到数据：" + binaryToHexString(buffer));
                                    log.l("数据长度：" + readCount);

//                                    ///////
//                                    SerialPort_DataFrameDecode(buffer, readCount);
////                                    Message UImessage = new Message();
//
//                                    log.l(temperature+"");
//                                    parameter jy = new parameter(accx, accy, accz,gyrox,gyroy,gyroz,anglex,
//                                            angley,anglez,depth,temperature,ph_value);
//                                    UImessage.obj = jy;
//                                    Uihandle.sendMessage(UImessage);
//                                    /////////////////////
                                } catch (Exception e) {
                                    Message message = new Message();
                                    message.what = MSG.READ_ERROR;
                                    message.obj = e.getMessage();
                                    handler.sendMessage(message);
                                }
                                SystemClock.sleep(200);
                            }
                            log.l("工作线程已退出");
                        } catch (Exception e) {
                            Message message = new Message();
                            message.what = MSG.CACHE_ERROR;
                            message.obj = e.getMessage();
                            handler.sendMessage(message);
                        }
                    }
                }
            });
            workThread.start();
        }
    }

    public void stop() {
        state = STATE.INIT;
        if (socket != null) {
            try {
                socket.closeSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
        threadFlag = false;
    }

    public void sendCommand(final byte[] data) {
        if (state != STATE.CONNED) {
            log.l("请确保与目标设备连接后再发送指令");
            return;
        }

        log.l("尝试发送指令：" + binaryToHexString(data));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMsg(data);
                } catch (Exception e) {
                    Message message = new Message();
                    message.what = MSG.SEND_ERROR;
                    message.obj = e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    private FrameStatus DataFrameDecode(int ch)
    {
        if (currentFrameStatus == FrameStatus.FRAME_IDLE || currentFrameStatus == FrameStatus.FRAME_OK || currentFrameStatus == FrameStatus.FRAME_ERROR)
        {
            if (ch == sync_header1)
            {
                currentFrameStatus = FrameStatus.FRAME_SYNC1;
            }
            else
            {
                currentFrameStatus = FrameStatus.FRAME_IDLE;
            }
        }
        else if (currentFrameStatus == FrameStatus.FRAME_SYNC1)
        {
            if (ch == sync_header2)
            {
                currentFrameStatus = FrameStatus.FRAME_SYNC2;
            }
            else
            {
                currentFrameStatus = FrameStatus.FRAME_ERROR;
            }
        }
        else if (currentFrameStatus == FrameStatus.FRAME_SYNC2)
        {
            switch (ch)
            {
                case InstrType.INSTR_SelfCheck:
                    currentInstrType = InstrType.INSTR_SelfCheck;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_GPS:
                    currentInstrType = InstrType.INSTR_GPS;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_JY901:
                    currentInstrType = InstrType.INSTR_JY901;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_ENCODER:
                    currentInstrType = InstrType.INSTR_ENCODER;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_CABLE:
                    currentInstrType = InstrType.INSTR_CABLE;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_RETRIEVAL:
                    currentInstrType = InstrType.INSTR_RETRIEVAL;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                    break;
                case InstrType.INSTR_DEPTH:
                    currentInstrType = InstrType.INSTR_DEPTH;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                case InstrType.INSTR_PH:
                    currentInstrType = InstrType.INSTR_PH;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                case InstrType.INSTR_TEMPREATURE:
                    currentInstrType = InstrType.INSTR_TEMPREATURE;
                    currentFrameStatus = FrameStatus.FRAME_TYPE;
                default:
                    currentFrameStatus = FrameStatus.FRAME_ERROR;
                    break;
            }
        }
        else if (currentFrameStatus == FrameStatus.FRAME_TYPE)
        {
            length = ch;
            index = 0;
            sum = 0x00;
            if (length == 0x00)
            {
                currentFrameStatus = FrameStatus.FRAME_SUM;
            }
            else
            {
                currentFrameStatus = FrameStatus.FRAME_DATE;
            }
        }
        else if (currentFrameStatus == FrameStatus.FRAME_DATE)
        {
            if (index < MAX_REC_LENGTH)
            {
                sum += DataBuffer[index++] = ch;
                if (index >= length)
                {
                    currentFrameStatus = FrameStatus.FRAME_SUM;
                }
            }
            else
            {
                currentFrameStatus = FrameStatus.FRAME_ERROR;
            }
        }
        else if (currentFrameStatus == FrameStatus.FRAME_SUM)
        {
            if (ch == sum)
            {
                currentFrameStatus = FrameStatus.FRAME_OK;
            }
            else
            {
                currentFrameStatus = FrameStatus.FRAME_ERROR;
            }
        }

        return currentFrameStatus;
    }

    ////// <summary>
    /// 解码数据帧
    /// </summary>
    /// <param name="SerialPortName">接收数据的总线</param>
    private void SerialPort_DataFrameDecode(byte ReceiceBytes[], int Length)
    {
        for (int ch:ReceiceBytes) {

            if (DataFrameDecode(ch) == FrameStatus.FRAME_OK)
            {
                if(currentInstrType == InstrType.INSTR_JY901)
                {
                    accx = (float)((DataBuffer[1]<<8)|DataBuffer[0])/32768 * 16;
                    accy = (float)((DataBuffer[3]<<8)|DataBuffer[2]) / 32768 * 16;
                    accz = (float)((DataBuffer[5]<<8)|DataBuffer[4]) / 32768 * 16;

                    gyrox = (float)((DataBuffer[7]<<8)|DataBuffer[6])  / 32768 * 2000;
                    gyroy = (float)((DataBuffer[9]<<8)|DataBuffer[8])  / 32768 * 2000;
                    gyroz = (float)((DataBuffer[11]<<8)|DataBuffer[10])  / 32768 * 2000;

                    anglex = (float)((DataBuffer[13]<<8)|DataBuffer[12]) / 32768 * 180;
                    angley = (float)((DataBuffer[15]<<8)|DataBuffer[14])  / 32768 * 180;
                    anglez = (float)((DataBuffer[17]<<8)|DataBuffer[16])  / 32768 * 180;
                    /*
                    accx = (float)BitConverter.ToInt16(DataBuffer, 0) / 32768 * 16;
                    accy = (float)BitConverter.ToInt16(DataBuffer, 2) / 32768 * 16;
                    accz = (float)BitConverter.ToInt16(DataBuffer, 4) / 32768 * 16;

                    gyrox = (float)BitConverter.ToInt16(DataBuffer, 6) / 32768 * 2000;
                    gyroy = (float)BitConverter.ToInt16(DataBuffer, 8) / 32768 * 2000;
                    gyroz = (float)BitConverter.ToInt16(DataBuffer, 10) / 32768 * 2000;

                    anglex = (float)BitConverter.ToInt16(DataBuffer, 12) / 32768 * 180;
                    angley = (float)BitConverter.ToInt16(DataBuffer, 14) / 32768 * 180;
                    anglez = (float)BitConverter.ToInt16(DataBuffer, 16) / 32768 * 180;
                    */
                }
                else if(currentInstrType == InstrType.INSTR_DEPTH)
                {
                    //深度
                    //潜航深度
                    depth = (float)((DataBuffer[1]<<8)|DataBuffer[0]);
                    float V = (float)(depth*3.3)/4096;
                    float C = (float)(V*1000)/120;
                    depth = 10000*C/9800;
                }else if(currentInstrType == InstrType.INSTR_PH)
                {
                    //PH
                    ph_value = (float)((DataBuffer[1]<<8)|DataBuffer[0]);
                    float Voltage = (float)(3.3*ph_value)/4096;
                    ph_value = (float)(-5.9647* Voltage+22.25);

                }else if(currentInstrType == InstrType.INSTR_TEMPREATURE)
                {
                    //温度
                    temperature = (float)((DataBuffer[1]<<8)|DataBuffer[0]);
                    temperature = temperature/16;
                }
                else if (currentInstrType == InstrType.INSTR_SelfCheck)
                {

                }else if(currentInstrType == InstrType.INSTR_RETRIEVAL)
                {
                    //应答

                }
            }
        }
    }

    public void Analysis(byte Buffer[], int Length) {
        for (int i = 0; i < Length; i++) {
            if (Buffer[i] == (byte) 0xEA && Buffer[i + 1] == (byte) 0x90) {
                if (Buffer[i + 2] == (byte) 0xFF) {
                    //应答帧x
                  /*  switch (Buffer[i + 4]) {
                        case (byte) 0x00:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("设置回传速率:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("设置回传速率:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("设置回传速率:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("设置回传速率:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("设置回传速率:校验和出错");
                                    break;
                            }
                            break;
                        case ((byte) 0x01):
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询自检信息:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询自检信息:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询自检信息:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询自检信息:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询自检信息:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x02:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询GPS:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询GPS:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询GPS:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询GPS:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询GPS:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x03:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询水上板JY901:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询水上板JY901:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询水上板JY901:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询水上板JY901:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询水上板JY901:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x04:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询水下板JY901:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询水下板JY901:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询水下板JY901:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询水下板JY901:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询水下板JY901:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x05:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询编码器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询编码器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询编码器命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询编码器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询编码器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x06:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("查询潜航深度:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("查询潜航深度:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("查询潜航深度:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("查询潜航深度:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("查询潜航深度:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x07:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制放线舵机:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制放线舵机:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制放线舵机:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制放线舵机:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制放线舵机:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x08:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水平云台舵机:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水平云台舵机:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水平云台舵机:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水平云台舵机:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水平云台舵机:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x09:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制垂直云台舵机:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制垂直云台舵机:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制垂直云台舵机:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制垂直云台舵机:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制垂直云台舵机:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0A:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水上板水平左推进器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水上板水平左推进器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水上板水平左推进器:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水上板水平左推进器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水上板水平左推进器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0B:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水上板水平右推进器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水上板水平右推进器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水上板水平右推进器:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水上板水平右推进器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水上板水平右推进器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0C:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水下板竖直推进器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水下板竖直推进器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水下板竖直推进器:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水下板竖直推进器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水下板竖直推进器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0D:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水下板水平左推进器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水下板水平左推进器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水下板水平左推进器:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水下板水平左推进器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水下板水平左推进器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0E:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制水下板水平右推进器:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制水下板水平右推进器:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制水下板水平右推进器:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制水下板水平右推进器:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制水下板水平右推进器:校验和出错");
                                    break;
                            }
                            break;
                        case (byte) 0x0F:
                            switch (Buffer[i + 5]) {
                                case (byte) 0x00:
                                    log.l("控制照明LED灯:指令接收成功");
                                    break;
                                case (byte) 0x01:
                                    log.l("控制照明LED灯:指令同步失败");
                                    break;
                                case (byte) 0x02:
                                    log.l("控制照明LED灯:命令字节无法识别");
                                    break;
                                case (byte) 0x03:
                                    log.l("控制照明LED灯:数据储存溢出");
                                    break;
                                case (byte) 0x04:
                                    log.l("控制照明LED灯:校验和出错");
                                    break;
                            }
                            break;
                    }*/
                    i = i + 7;
                } else if (Buffer[i + 2] == (byte) 0x01) {
                    //自检
                    String TIME;
                    if (Buffer[i + 4] + Buffer[i + 5] + Buffer[i + 6] + Buffer[i + 7] + Buffer[i + 8] + Buffer[i + 9] == Buffer[i + 10]) {
                        Power = Integer.valueOf(binaryToHexString(Buffer[i + 4]), 16);
                        STA = HToB(binaryToHexString(Buffer[i + 5]));
                        TIME = toD(binaryToHexString(Buffer[i + 6]), 16) + toD(binaryToHexString(Buffer[i + 7]), 16) + toD(binaryToHexString(Buffer[i + 8]), 16) + toD(binaryToHexString(Buffer[i + 9]), 16);
                        log.l(Power + STA + TIME);
                    }
                    i = i + 11;
                } else if (Buffer[i + 2] == (byte) 0x02) {
                    //GPS
                    i = i + 28;
                } else if (Buffer[i + 2] == (byte) 0x03) {
                    //水上板Y91
                    i = i + 23;
                } else if (Buffer[i + 2] == (byte) 0x04) {
                    //水下板Y91
                    i = i + 23;
                } else if (Buffer[i + 2] == (byte) 0x05) {
                    //编码器
                    i = i + 9;

                } else if (Buffer[i + 2] == (byte) 0x06) {
                    //潜航深度
                   /* String DEPTH = toD(binaryToHexString(Buffer[i + 3]), 16) + toD(binaryToHexString(Buffer[i + 4]), 16);
                    if (Buffer[i + 3] + Buffer[i + 4] == Buffer[i + 5]) {
                        float Voltage = (float) (3.3 * Float.parseFloat(DEPTH) / 4096);
                        float Current = Voltage * 1000 / 120;
                        float Depth = 10000 * Current / 9800;
                        log.l(String.valueOf(Depth));
                    }*/
                    i = i + 7;
                }
            }
        }
    }

    public String HToB(String a) {
        String b = Integer.toBinaryString(Integer.valueOf(toD(a, 16)));
        return b;
    }

    public String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (r + formatting(a.substring(i, i + 1))
                    * Math.pow(b, a.length() - i - 1));
        }
        return String.valueOf(r);
    }

    public int formatting(String a) {
        int i = 0;
        for (int u = 0; u < 10; u++) {
            if (a.equals(String.valueOf(u))) {
                i = u;
            }
        }
        if (a.equals("A")) {
            i = 10;
        }
        if (a.equals("B")) {
            i = 11;
        }
        if (a.equals("C")) {
            i = 12;
        }
        if (a.equals("D")) {
            i = 13;
        }
        if (a.equals("E")) {
            i = 14;
        }
        if (a.equals("F")) {
            i = 15;
        }
        return i;
    }
    public class ByteActualLength {

        public  int returnActualLength(byte[] data) {
            int i = 0;
            for (; i < data.length; i++) {
                if (data[i] == '\0')
                    break;
            }
            return i;
        }

    }
}
