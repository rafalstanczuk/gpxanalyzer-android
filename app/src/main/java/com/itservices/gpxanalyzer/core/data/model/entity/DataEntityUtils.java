package com.itservices.gpxanalyzer.core.data.model.entity;

import java.util.Vector;

public class DataEntityUtils {
    public static long calculateDataFingerprint(Vector<DataEntity> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }

        // For very large datasets, we sample rather than check everything
        int dataSize = data.size();

        // Use sample size based on data size, max 10 samples
        int sampleSize = Math.min(10, dataSize);

        // Include data size as part of fingerprint
        long fingerprint = dataSize;

        // Sample distributed points throughout the data
        for (int i = 0; i < sampleSize; i++) {
            int index = (int) (((float) i / sampleSize) * dataSize);
            if (index < dataSize) {
                DataEntity entity = data.get(index);
                // Combine timestamp and first value if available
                fingerprint = 31 * fingerprint + entity.timestampMillis();
                if (!entity.getMeasures().isEmpty()) {
                    fingerprint = 31 * fingerprint + Float.floatToIntBits(entity.getMeasures().get(0).value());
                }
            }
        }

        // Also include first and last entries for better comparison
        fingerprint = 31 * fingerprint + data.firstElement().timestampMillis();
        fingerprint = 31 * fingerprint + data.lastElement().timestampMillis();

        return fingerprint;
    }
}
