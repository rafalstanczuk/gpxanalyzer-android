package com.itservices.gpxanalyzer.data.extrema;

import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Single;

public class ExtremaSegmentListProvider {

    public static Single<List<Segment>> provide(DataEntityWrapper dataEntityWrapper) {
        return Single.fromCallable(() -> {
            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(dataEntityWrapper);

            primitiveList.sort(Comparator.comparingLong(PrimitiveDataEntity::getTimestamp));

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();

            double stdDev = getStandardDeviation(primitiveList);

            double[] windowFunction = WaveletLagDataSmoother.computeAdaptiveWindowFunction(
                    primitiveList, stdDev, ExtremaSegmentDetector.WindowType.GAUSSIAN);

            System.out.println("Optimal Adaptive Window Size (Wavelet-based): windowFunction: " + Arrays.toString(windowFunction));
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): size: " + windowFunction.length);
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): data size: " + primitiveList.size());
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): stdDev: " + stdDev);

            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunction);

            SegmentThresholds segmentThresholds = new SegmentThresholds(stdDev * 0.2);

            List<Segment> extremaSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            extremaSegmentList
                    = segmentDetector.addMissingSegments(extremaSegmentList, segmentThresholds);

            return extremaSegmentList;
        });
    }

    /**
     * Computes the standard deviation of the values in the list of PrimitiveDataEntity.
     * Uses Apache Commons Math for high-precision calculations.
     *
     * @return The standard deviation of the values.
     */
    private static double getStandardDeviation(List<PrimitiveDataEntity> primitiveList) {
        if (primitiveList == null || primitiveList.isEmpty()) {
            return 0.0; // Return 0 if there are no values to avoid errors
        }

        int size = primitiveList.size();
        double[] values = new double[size];

        // Extract values into an array
        Arrays.setAll(values, i -> primitiveList.get(i).getValue());

        // Use Apache Commons Math StandardDeviation class
        StandardDeviation stdDev = new StandardDeviation(false); // 'false' means population std dev
        return stdDev.evaluate(values);
    }
}
