package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.azhon.jtt808.JTT808Manager;

public class MainActivity extends AppCompatActivity {
    private static final String PHONE = "88031113881";
    private static final String TERMINAL_ID = "88031113881";
    private static final String IP = "106.14.186.44";
    private static final int PORT = 7030;
    private  GPSInfo gpsInfo;
    private JTT808Manager manager;
    private final int UpdateGPS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionCheck.checkPermission(this);
        initJTT808();
        gpsInfo = new GPSInfo(this);
        mainHandler.sendEmptyMessage(UpdateGPS);
    }

    private void initJTT808() {
        manager = JTT808Manager.getInstance();
        // manager.setOnConnectionListener(this);
        manager.init(PHONE, TERMINAL_ID, IP, PORT);

    }

    private Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch ( msg.what ){
                case UpdateGPS:
                    manager.uploadLocation(gpsInfo.getLatitude(),gpsInfo.getLongitude(),gpsInfo.getSpeed(),0,0);
                    sendEmptyMessageDelayed(UpdateGPS,5000);
                    break;
            }

        }
    };


}