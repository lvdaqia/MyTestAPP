package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.azhon.jtt808.JTT808Manager;

public class MainActivity extends Activity {
    private LinearLayout mainLinearLayout;
    private MyImageView gps;
    private static final String PHONE = "88031113881";
    private static final String TERMINAL_ID = "88031113881";
    private static final String IP = "106.14.186.44";
    private static final int PORT = 7030;
    private  GPSInfo gpsInfo;
    private JTT808Manager manager;
    private static final int UpdateGPS = 1;
    private static final int GPS_UI = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mainLinearLayout = ( LinearLayout )findViewById(R.id.main_UI);
        gps = (MyImageView) findViewById(R.id.gps);
        gps.setOnClickIntent(new MyImageView.OnViewClickListener()
        {

            @Override
            public void onViewClick(MyImageView view)
            {
                mainHandler.sendEmptyMessage(2);
            }
        });


        PermissionCheck.checkPermission(this);
        init();

        mainHandler.removeMessages(UpdateGPS);
        mainHandler.sendEmptyMessage(UpdateGPS);
    }

    private void init() {
        manager = JTT808Manager.getInstance();
        // manager.setOnConnectionListener(this);
        manager.init(PHONE, TERMINAL_ID, IP, PORT);
        gpsInfo = new GPSInfo(this);
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
                case GPS_UI:

                    break;
            }

        }
    };


}