package com.itservices.gpxanalyzer.data.provider.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.room.TypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Converters {

    public static String fromFile(File file) {
        return file == null ? null :
                toBase64(file.getAbsolutePath());
    }

    public static File toFile(String fileAbsolutePathBase64) {
        return fileAbsolutePathBase64 == null ? null :
                new File(
                        fromBase64(fileAbsolutePathBase64)
                );
    }

    public static String toBase64FromByteArray(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static byte[] fromBase64ToByteArray(String string) {
        return Base64.decode(string, Base64.NO_WRAP);
    }


    public static String toBase64(String string) {
        return Base64.encodeToString(string.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    public static String fromBase64(String string) {
        return new String(Base64.decode(string, Base64.NO_WRAP), StandardCharsets.UTF_8);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    @TypeConverter
    public static String fromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return toBase64FromByteArray(stream.toByteArray());
    }
    
    @TypeConverter
    public static Bitmap toBitmap(String bytesBase64) {
        if (bytesBase64 == null) return null;

        byte[] decoded = fromBase64ToByteArray(bytesBase64);

        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        return bitmap;
    }
} 