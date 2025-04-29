package com.itservices.gpxanalyzer.data.provider.file;

import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validator class for GpxFileInfo to check file existence and validity.
 */
public class GpxFileValidator {

    /**
     * Checks if the file in the GpxFileInfo exists.
     *
     * @param gpxFileInfo The GpxFileInfo to validate
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(GpxFileInfo gpxFileInfo) {
        if (gpxFileInfo == null) {
            return false;
        }

        File file = gpxFileInfo.file();
        return fileExists(file);
    }

    /**
     * Checks if the given file exists.
     *
     * @param file The file to check
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * Checks if the file exists given a file path.
     *
     * @param filePath The path of the file to check
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        return fileExists(new File(filePath));
    }

    /**
     * Validates a list of GpxFileInfo objects and filters out those with non-existent files.
     *
     * @param gpxFileInfos List of GpxFileInfo objects to validate
     * @return List containing only valid GpxFileInfo objects
     */
    public static List<GpxFileInfo> validateAndFilterFiles(List<GpxFileInfo> gpxFileInfos) {
        if (gpxFileInfos == null) {
            return new ArrayList<>();
        }

        return gpxFileInfos.stream()
                .filter(GpxFileValidator::fileExists)
                .collect(Collectors.toList());
    }
}