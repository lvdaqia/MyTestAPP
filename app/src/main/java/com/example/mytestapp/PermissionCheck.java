package com.example.mytestapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionCheck {
    private static final String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSIONS_RESULT_CODE = 121;// 自定义
    /**
     * 申请权限
     */



    public static void checkPermission(Activity mContext) {
        // SDK版本 大于或等于23 动态申请权限

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        // 检查 未授权 的 权限
        List<String> pl = new ArrayList<>();
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(mContext,permission)
                    != PackageManager.PERMISSION_GRANTED){
                pl.add(permission);
            }
        }
        // 申请权限
        if(pl.size() > 0){
            ActivityCompat.requestPermissions(mContext, pl.toArray(new String[0]),PERMISSIONS_RESULT_CODE);
        }

    }



}
