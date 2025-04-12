package com.itservices.gpxanalyzer.utils.files;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * Requests file access permissions based on the Android version.
 */
public class PermissionUtils {
    private static final String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static final String[] storage_permissions_30_31 = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    public static boolean hasFileAccessPermissions(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 and above
                return hasAllPermissions(context, storage_permissions_33) && 
                       Environment.isExternalStorageManager();
            } else {
                // For Android 11-12
                return Environment.isExternalStorageManager() || 
                       hasAllPermissions(context, storage_permissions_30_31);
            }
        } else {
            // For Android 10 and below
            return hasAllPermissions(context, storage_permissions);
        }
    }

    private static boolean hasAllPermissions(Activity context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests file-access permissions for Android 7-14+, <29,  29, 30, 33 , 34+ (API 24 and below - up to 34 and above).
     */
    public static void requestFileAccessPermissions(ActivityResultLauncher<String[]> permissionLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 and above
                permissionLauncher.launch(storage_permissions_33);
            } else {
                // For Android 11-12
                permissionLauncher.launch(storage_permissions_30_31);
            }
        } else {
            // For Android 10 and below
            permissionLauncher.launch(storage_permissions);
        }
    }

    /**
     * Requests MANAGE_EXTERNAL_STORAGE permission for Android 11+.
     * This is required for accessing external SD cards and certain directories.
     */
    public static void requestManageExternalStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        }
    }
}