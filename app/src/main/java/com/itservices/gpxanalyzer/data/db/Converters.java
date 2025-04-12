package com.itservices.gpxanalyzer.data.db;

import androidx.room.TypeConverter;
import java.io.File;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static String fromFile(File file) {
        return file == null ? null : file.getAbsolutePath();
    }

    @TypeConverter
    public static File toFile(String path) {
        return path == null ? null : new File(path);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
} 