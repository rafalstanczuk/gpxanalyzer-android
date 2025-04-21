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
 * Utility class for handling Android storage permissions across different API levels.
 * Provides methods to check if necessary permissions are granted and to request them.
 * It accounts for changes in storage permission models introduced in Android 10 (Q), 11 (R), and 13 (Tiramisu).
 */
public class PermissionUtils {
    /** Permissions required for Android 9 (Pie) and below (API <= 28). */
    private static final String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /** Permissions required for Android 11 (R) and 12 (S) (API 30-32). Includes MANAGE_EXTERNAL_STORAGE. */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private static final String[] storage_permissions_30_31 = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    /** Permissions required for Android 13 (Tiramisu) and above (API >= 33). Uses granular media permissions. */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    /**
     * Checks if the application currently holds the necessary file access permissions
     * based on the device's Android version.
     *
     * - **Android 13 (Tiramisu) and above:** Checks for granular media permissions (IMAGES, AUDIO, VIDEO)
     *   AND if the app has All Files Access (`Environment.isExternalStorageManager()`).
     * - **Android 11 (R) and 12 (S):** Checks if the app has All Files Access (`Environment.isExternalStorageManager()`)
     *   OR if it has `READ_EXTERNAL_STORAGE` and `MANAGE_EXTERNAL_STORAGE`.
     * - **Android 10 (Q) and below:** Checks for `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`.
     *
     * @param context The current {@link Activity} context.
     * @return {@code true} if all required permissions for the current Android version are granted, {@code false} otherwise.
     */
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

    /**
     * Helper method to check if all permissions in a given array are granted.
     *
     * @param context     The current {@link Activity} context.
     * @param permissions An array of permission strings to check.
     * @return {@code true} if all permissions in the array are granted, {@code false} otherwise.
     */
    private static boolean hasAllPermissions(Activity context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests the appropriate set of file access permissions using the provided Activity Result Launcher.
     * The specific permissions requested depend on the device's Android API level:
     * - Android 13+: Requests granular media permissions (`storage_permissions_33`).
     * - Android 11-12: Requests `READ_EXTERNAL_STORAGE` and `MANAGE_EXTERNAL_STORAGE` (`storage_permissions_30_31`).
     *   (Note: `MANAGE_EXTERNAL_STORAGE` often requires a separate intent, see {@link #requestManageExternalStoragePermission(Activity)}).
     * - Android 10 and below: Requests legacy `READ/WRITE_EXTERNAL_STORAGE` (`storage_permissions`).
     *
     * @param permissionLauncher The {@link ActivityResultLauncher} registered to handle the permission request result
     *                           (typically using {@code ActivityResultContracts.RequestMultiplePermissions}).
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
     * For Android 11 (R) and above, explicitly requests the "All Files Access" permission
     * (`MANAGE_EXTERNAL_STORAGE`) by launching the system settings screen for the app.
     * This is necessary for broad access to external storage outside of app-specific directories or MediaStore.
     * This method only launches the intent if the permission is not already granted.
     *
     * @param activity The current {@link Activity} context used to launch the settings intent.
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