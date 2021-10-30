package com.example.mci.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class MiscUtils {
    public static void verifyPermissions(Activity activity) {
        verifyPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
        verifyPermission(activity, Manifest.permission.RECORD_AUDIO, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            verifyPermission(activity, Manifest.permission.CAPTURE_AUDIO_OUTPUT, 1);
        }
        verifyPermission(activity, Manifest.permission.ACCESS_WIFI_STATE, 1);
    }

    public static void verifyPermission(
            Activity activity, String manifestPermission, Integer requestCode
    ){
        if (ActivityCompat.checkSelfPermission(activity, manifestPermission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            manifestPermission
                    },
                    requestCode
            );
        }
    }
}
