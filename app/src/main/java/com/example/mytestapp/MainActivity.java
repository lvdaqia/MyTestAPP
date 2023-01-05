package com.example.mytestapp;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.listener.OnConnectionListener;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, OnConnectionListener {
    private static String IP = "";              //ip
    private static int PORT ;                   //端口
    private static String PHONE = "";           //手机号
    private static String DEVICE_ID = "";       //终端ID
    private static String FACTORY_ID = "";      //制造商
    private static String DEVICE_MODEL = "";    //设备型号

    private static String TAG = "MainActivity";
    private boolean car_connect = false;
    private ImageView web,gps;
    private  GPSInfo gpsInfo;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private JTT808Manager manager;
    private static final int UpdateGPS = 1;
    private static final int ConnectSerVer = 2;
    private static final int Update_Car_Status = 3;
    private View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        gpsInfo = new GPSInfo(this);
        PermissionCheck.checkPermission(this);

        initView();
        initData();
        initJT808();
        mainHandler.removeMessages(UpdateGPS);
        mainHandler.sendEmptyMessage(UpdateGPS);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.car:
                showDialog();
                break;
            case R.id.OK_Button:
                Log.e("ldq","aaaa");
                break;
        }
    }
    private void showDialog(){
        //加载自定义布局并初始化控件
        view= LayoutInflater.from(MainActivity.this).inflate(R.layout.car_dialog, null);
        EditText ip_EditText,port_EditText,phone_EditText,deviceID_EditText,factory_EditText,deviceModel_EditText;
        ip_EditText = view.findViewById(R.id.ip_EditText);
        port_EditText = view.findViewById(R.id.port_EditText);
        phone_EditText = view.findViewById(R.id.phoneNumber_EditText);
        deviceID_EditText = view.findViewById(R.id.deviceID_EditText);
        factory_EditText = view.findViewById(R.id.factory_EditText);
        deviceModel_EditText = view.findViewById(R.id.deviceModel_EditText);
        ip_EditText.setText(IP);
        port_EditText.setText(String.valueOf(PORT));
        phone_EditText.setText(PHONE);
        deviceID_EditText.setText(DEVICE_ID);
        factory_EditText.setText(FACTORY_ID);
        deviceModel_EditText.setText(DEVICE_MODEL);
        ImageView car = view.findViewById(R.id.car);
        TextView car_status = view.findViewById(R.id.car_status);
        Button okbutton = view.findViewById(R.id.OK_Button);
        Button nobutton = view.findViewById(R.id.No_Button);

        if(car_connect) {
            car.setColorFilter(Color.GREEN);
            car_status.setText("已连接");
            car_status.setTextColor(Color.GREEN);
            okbutton.setText("重新连接");
            nobutton.setText("断开");
        }else {
            car.setColorFilter(Color.RED);
            car_status.setText("未连接");
            car_status.setTextColor(Color.RED);
            okbutton.setText("连接");
            nobutton.setText("取消");
        }

        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        final Dialog dialog= builder.create();
        dialog.show();
        Window window= dialog.getWindow();
        window.setContentView(view);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        WindowManager.LayoutParams params= window.getAttributes();
        params.height= LinearLayout.LayoutParams.WRAP_CONTENT;
        params.width= LinearLayout.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IP = ip_EditText.getText().toString();
                PORT = Integer.valueOf(port_EditText.getText().toString());
                PHONE = phone_EditText.getText().toString();
                DEVICE_ID = deviceID_EditText.getText().toString();
                FACTORY_ID = factory_EditText.getText().toString();
                DEVICE_MODEL = deviceModel_EditText.getText().toString();
                mainHandler.removeMessages(ConnectSerVer);
                mainHandler.sendEmptyMessage(ConnectSerVer);

            }
        });
        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( car_connect ){
                    manager.disconnect();
                }else {
                    dialog.dismiss();
                }
            }
        });
    }
    private void initView(){
        view= LayoutInflater.from(MainActivity.this).inflate(R.layout.car_dialog, null);
        web = (ImageView)findViewById(R.id.car);
        web.setOnClickListener(this);
        gps = (ImageView)findViewById(R.id.gps);
        gps.setOnClickListener(this);
    }
    private void initJT808() {
        if (manager != null){
            manager.disconnect();
        }
        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this);
        manager.init(PHONE, DEVICE_ID, IP, PORT);

    }
    private void initData(){
        sp = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        editor = sp.edit();
        IP = sp.getString("ip","106.14.186.44");
        PORT = sp.getInt("port",7030);
        PHONE = sp.getString("phone","13526798526");
        DEVICE_ID = sp.getString("device_id","13526798526");
        FACTORY_ID = sp.getString("factory_id","深圳市一九智能有限公司");
        DEVICE_MODEL = sp.getString("device_model","ycm028");
    }
    private void saveData(){
        editor.putString("ip",IP);
        editor.putInt("port",PORT);
        editor.putString("phone",PHONE);
        editor.putString("device_id",DEVICE_ID);
        editor.putString("factory_id",FACTORY_ID);
        editor.putString("device_model",DEVICE_MODEL);
        editor.commit();
    }
    private Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch ( msg.what ){
                case UpdateGPS:
                    manager.uploadLocation(gpsInfo.getLatitude(),gpsInfo.getLongitude(),gpsInfo.getSpeed(),0,0);
                    mainHandler.removeMessages(UpdateGPS);
                    sendEmptyMessageDelayed(UpdateGPS,5000);
                    break;
                case ConnectSerVer:
                    initJT808();
                    saveData();
                    break;
                case Update_Car_Status:
                    ImageView car = view.findViewById(R.id.car);
                    TextView car_status = view.findViewById(R.id.car_status);
                    Button okbutton = view.findViewById(R.id.OK_Button);
                    Button nobutton = view.findViewById(R.id.No_Button);
                    if(car_connect) {
                        car.setColorFilter(Color.GREEN);
                        car_status.setText("已连接");
                        car_status.setTextColor(Color.GREEN);
                        okbutton.setText("重新连接");
                        nobutton.setText("断开");
                    }else {
                        car.setColorFilter(Color.RED);
                        car_status.setText("未连接");
                        car_status.setTextColor(Color.RED);
                        okbutton.setText("连接");
                        nobutton.setText("取消");
                    }
                    break;
            }

        }
    };

    @Override
    public void onLocalConnectionSateChange(int state) {
    }

    @Override
    public void onConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                Log.d(TAG, "start regist server");
                manager.register(DEVICE_ID, DEVICE_MODEL);
                car_connect = true;
                mainHandler.removeMessages(Update_Car_Status);
                mainHandler.sendEmptyMessage(Update_Car_Status);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "server断开连接");
                car_connect = false;
                mainHandler.removeMessages(Update_Car_Status);
                mainHandler.sendEmptyMessage(Update_Car_Status);
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "server重连");
                car_connect = true;
                mainHandler.removeMessages(Update_Car_Status);
                mainHandler.sendEmptyMessage(Update_Car_Status);
                break;
            default:
                break;
        }
    }

    @Override
    public void on905ConnectionSateChange(int state) {

    }

    @Override
    public void receiveData(JTT808Bean jtt808Bean) {

    }

    @Override
    public void terminalParams(List<TerminalParamsBean> params) {

    }

    @Override
    public void audioVideoLive(String ip, int port, int channelNum, int dataType) {

    }

    @Override
    public void localAudioVideoLive(String ip, int port, int channelNum, int dataType) {

    }

    @Override
    public void audioVideoLive905(String ip, int port, int channelNum, int dataType) {

    }

    @Override
    public void audioVideoLiveControl(int channelNum, int control, int closeAudio, int switchStream) {

    }

    @Override
    public void audioVideoLocalLiveControl(int channelNum, int control, int closeAudio, int switchStream) {

    }

    @Override
    public void audioVideo905LiveControl(int channelNum, int control, int closeAudio, int switchStream) {

    }

    @Override
    public void localVideoList(byte[] flowNo, int channelNum, String startTIme, String stoptTIme, String alarmSign, int dataType, int bitstreamype, int storageType) {

    }

    @Override
    public void localVideoListLocal(byte[] flowNo, int channelNum, String startTIme, String stoptTIme, String alarmSign, int dataType, int bitstreamype, int storageType) {

    }

    @Override
    public void PlayBackAudioVideoLive(String ip, int port, int channelNum, int dataType, String starttime, String stoptime) {

    }

    @Override
    public void PlayBackLocalAudioVideoLive(String ip, int port, int channelNum, int dataType, String starttime, String stoptime) {

    }

    @Override
    public void PlayBackVideoAudioLiveControl(int channelNum, int control, int fastForwardAndBack, String dragPosition) {

    }

    @Override
    public void UpVedioFile(String user, String passwor, String ip, int port, int channelNum, int dataType, String startTime, String stopTime, String remotePath) {

    }

    @Override
    public void UpLocalVideoFile(String user, String passwor, String ip, int port, int channelNum, int dataType, String startTime, String stopTime, String remotePath) {

    }

    @Override
    public void tcpAlive() {

    }

    @Override
    public void tcpLocalAlive() {

    }

    @Override
    public void tcp905Alive() {

    }

    @Override
    public <T> void queryAtrribute(T a) {

    }

    @Override
    public <T> void textdelivery(T a, String ftpText) {

    }

    @Override
    public void terminalControl(int flag) {

    }
}