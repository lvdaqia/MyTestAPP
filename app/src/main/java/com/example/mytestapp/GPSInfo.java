package com.example.mytestapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class GPSInfo {
    private LocationManager mLocationManager;
    private Location location;
    private Context mContext;
    @SuppressLint("MissingPermission")

    public GPSInfo(Context context){
        mContext = context;
        if( mLocationManager == null ) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (mLocationManager.isLocationEnabled()) {
            String bestProvider = mLocationManager.getBestProvider(getCriteria(), true);
            //mLocationManager.registerGnssStatusCallback(mGNSSCallback);
            mLocationManager.requestLocationUpdates(bestProvider, 500, 50, mLocationListener);
            location = mLocationManager.getLastKnownLocation(bestProvider);
        }

    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria._COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }


    private LocationListener mLocationListener = new  LocationListener() {
        /**
         * 当手机位置发生改变的时候 调用的方法
         */
        public void onLocationChanged(Location location) {
            String latitude ="latitude "+ location.getLatitude(); //weidu
            String longtitude = "longtitude "+ location.getLongitude(); //jingdu
            Log.e("ldq","纬度： "+latitude);
            Log.e("ldq","经度： "+longtitude);
        }
    };

    //获取纬度
    public  double getLatitude(){
        return location.getLatitude();
    }

    //获取经度
    public  double getLongitude(){
        return location.getLongitude();
    }

    public float getSpeed(){
        return location.getSpeed()*3.6f*10;
    }
}
