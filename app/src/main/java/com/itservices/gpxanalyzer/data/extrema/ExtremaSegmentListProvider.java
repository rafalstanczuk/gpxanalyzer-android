package com.itservices.gpxanalyzer.data.extrema;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;

public class ExtremaSegmentListProvider {
    private static final double[] alpineSkiWindowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(9, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0.2);

    public static Observable<List<Segment>> provide(DataEntityWrapper dataEntityWrapper) {
        return Observable.fromCallable(() -> {
            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(dataEntityWrapper);

            primitiveList.sort(Comparator.comparingLong(PrimitiveDataEntity::getTimestamp));

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();

            double stdDev = getStandardDeviation(primitiveList);


            // TODO: parameters can be changed by USER by for ex. select type of activity !!!
            SegmentThresholds segmentThresholds = getSegmentThresholds(dataEntityWrapper, stdDev);



            double[] windowFunction = WaveletLagDataSmoother.computeAdaptiveWindowFunction(
                    primitiveList, stdDev, ExtremaSegmentDetector.WindowType.GAUSSIAN);

            System.out.println("Optimal Adaptive Window Size (Wavelet-based): windowFunction: " + Arrays.toString(windowFunction));
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): size: " + windowFunction.length);
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): data size: " + primitiveList.size());

            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunction);



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

    @NonNull
    private static SegmentThresholds getSegmentThresholds(DataEntityWrapper dataEntityWrapper, double stdDev) {
        double dMinMax = dataEntityWrapper.getDeltaMinMax();

        System.out.println("getStandardDeviation " + stdDev);
        System.out.println("dMinMax " + dMinMax);

        // Adaptive min amplitude calculation based on both min-max range and standard deviation
        double minAscAmp = Math.max(dMinMax / 6, stdDev / 2);
        double minDescAmp = Math.max(dMinMax / 6, stdDev / 2);

        // Ensure minimal threshold values to avoid over-sensitivity to noise
        if (minAscAmp < 0.001) minAscAmp = 0.001;
        if (minDescAmp < 0.001) minDescAmp = 0.001;

        return new SegmentThresholds(minAscAmp, 0.001, minDescAmp, 0.001);
    }
}
