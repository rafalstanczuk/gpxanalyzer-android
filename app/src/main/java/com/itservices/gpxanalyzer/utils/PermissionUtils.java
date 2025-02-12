package com.itservices.gpxanalyzer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

/**
 * Requests file access permissions based on the Android version.
 */
public class PermissionUtils {
    public static int STORAGE_PERMISSION_REQUEST_CODE = 101;

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
     * Requests file-access permissions for Android 7-9, 29, 30, 33 , 34+ (API 24-34 and above).
     */
    public static void requestFileAccessPermissions(Activity activity, ActivityResultLauncher<String[]> permissionLauncher) {
        Log.d("Permissions", "requestFileAccessPermissions() called with: activity = [" + activity + "], permissionLauncher = [" + permissionLauncher + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
            permissionLauncher.launch(storage_permissions_33);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            permissionLauncher.launch(storage_permissions_33);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ (Android 11+)
            permissionLauncher.launch(storage_permissions_30_31);


            //requestWritePermission(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 (Android 10)
            //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            permissionLauncher.launch(storage_permissions);
        } else {
            // API 24-28 (Android 7-9)
            /*ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_REQUEST_CODE);*/
            permissionLauncher.launch(storage_permissions);

        }
    }


    public static String[] getStoragePermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = storage_permissions_33;
        }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = storage_permissions_30_31;
        } else {
            permissions = storage_permissions;
        }
        return permissions;
    }

    private static boolean needsRequestPermissions(Activity activity, String[] permissions) {
        Log.d("sasa", "needsRequestPermissions() called with: activity = [" + activity + "], permissions = [" + permissions + "]");
        boolean out = false;

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                out = true;
            }
        }

        return out;
    }


    /**
     * Checks if required file permissions are granted.
     */
    public static boolean hasFileAccessPermissions(Activity context) {
/*        ActivityCompat.requestPermissions(
                context,
                getStoragePermissions(),
                STORAGE_PERMISSION_REQUEST_CODE
        );*/

        return !needsRequestPermissions(context, getStoragePermissions());

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return android.os.Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }*/
    }
}