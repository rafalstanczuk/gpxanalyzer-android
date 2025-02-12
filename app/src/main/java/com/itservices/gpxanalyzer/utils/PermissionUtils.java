package com.itservices.gpxanalyzer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

/**
 * Requests file access permissions based on the Android version.
 */
public class PermissionUtils {
    private static final String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String[] storage_permissions_30_31 = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    /**
     * Requests file-access permissions for Android 7-14+, <29,  29, 30, 33 , 34+ (API 24 and below - up to 34 and above).
     */
    public static void requestFileAccessPermissions(ActivityResultLauncher<String[]> permissionLauncher) {
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
            permissionLauncher.launch(storage_permissions_33);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            permissionLauncher.launch(storage_permissions_33);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ (Android 11+)
            permissionLauncher.launch(storage_permissions_30_31);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 (Android 10)
            permissionLauncher.launch(storage_permissions);
        } else {
            // API 24-28 (Android 7-9)
            permissionLauncher.launch(storage_permissions);
        }*/

        permissionLauncher.launch(getStoragePermissions());
    }


    public static String[] getStoragePermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
            permissions = storage_permissions_33;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = storage_permissions_33;
        }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = storage_permissions_30_31;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = storage_permissions;
        } else {
            permissions = storage_permissions;
        }
        return permissions;
    }

    private static boolean needsRequestPermissions(Activity activity, String[] permissions) {
        boolean out = false;

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                out = true;
            }
        }

        return out;
    }

    public static boolean hasFileAccessPermissions(Activity context) {
        return !needsRequestPermissions(context, getStoragePermissions());
    }
}