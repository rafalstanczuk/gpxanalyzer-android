package com.itservices.gpxanalyzer.domain.extrema;

import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.domain.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.domain.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.domain.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.domain.extrema.detector.Segment;
import com.itservices.gpxanalyzer.domain.extrema.detector.SegmentThresholds;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Vector;

import io.reactivex.Single;

public class ExtremaSegmentListMapper {

    public static Single<Vector<Segment>> mapFrom(DataEntityWrapper dataEntityWrapper) {
        return Single.fromCallable(() -> {
            //Log.i("ExtremaSegmentListProvider", "provide() called with: dataEntityWrapper.getPrimaryDataIndex() = [" + dataEntityWrapper.getPrimaryDataIndex() + "]");
            //Log.i("ExtremaSegmentListProvider", "provide() called with: dataEntityWrapper.getDataHash() = [" + dataEntityWrapper.getDataHash() + "]");
            //Log.i("ExtremaSegmentListProvider", "provide() called with: dataEntityWrapper.getData().size() = [" + dataEntityWrapper.getData().size() + "]");
            //Log.i("ExtremaSegmentListProvider", "provide() called with: dataEntityWrapper.getData().firstElement().timestampMillis() = [" + dataEntityWrapper.getData().firstElement().timestampMillis() + "]");
            //Log.i("ExtremaSegmentListProvider", "provide() called with: dataEntityWrapper.getData().lastElement().timestampMillis() = [" + dataEntityWrapper.getData().lastElement().timestampMillis() + "]");

            Vector<PrimitiveDataEntity> primitiveVector = DataPrimitiveMapper.mapFrom(dataEntityWrapper);

            //Log.i("ExtremaSegmentListProvider", "provide() primitiveVector = [" + primitiveVector.size() + "]");

            //Log.i("ExtremaSegmentListProvider", "provide() primitiveVector.firstElement().getTimestamp() = [" + primitiveVector.firstElement().getTimestamp() + "]");
            //Log.i("ExtremaSegmentListProvider", "provide() primitiveVector.lastElement().getTimestamp() = [" + primitiveVector.lastElement().getTimestamp() + "]");

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();

            double stdDev = getStandardDeviation(primitiveVector);

            double[] windowFunction = WaveletLagDataSmoother.computeAdaptiveWindowFunction(
                    primitiveVector, stdDev, ExtremaSegmentDetector.WindowType.GAUSSIAN);

            System.out.println("Optimal Adaptive Window Size (Wavelet-based): windowFunction: " + Arrays.toString(windowFunction));
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): size: " + windowFunction.length);
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): data size: " + primitiveVector.size());
            System.out.println("Optimal Adaptive Window Size (Wavelet-based): stdDev: " + stdDev);

            segmentDetector.preprocessAndFindExtrema(primitiveVector, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunction);

            SegmentThresholds segmentThresholds = new SegmentThresholds(stdDev * 0.2);

            Vector<Segment> extremaSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            //Log.i("ExtremaSegmentListProvider", "provide() detectSegmentsOneRun extremaSegmentList.firstElement().startTime() = [" + extremaSegmentList.firstElement().startTime() + "]" + dataEntityWrapper.getPrimaryDataIndex());
            //Log.i("ExtremaSegmentListProvider", "provide() detectSegmentsOneRun extremaSegmentList.lastElement().endTime() = [" + extremaSegmentList.lastElement().endTime() + "]" + dataEntityWrapper.getPrimaryDataIndex());

            extremaSegmentList
                    = segmentDetector.addMissingSegments(extremaSegmentList, segmentThresholds);

            //Log.i("ExtremaSegmentListProvider", "provide() addMissingSegments extremaSegmentList.firstElement().startTime() = [" + extremaSegmentList.firstElement().startTime() + "]" + dataEntityWrapper.getPrimaryDataIndex());
            //Log.i("ExtremaSegmentListProvider", "provide() addMissingSegments extremaSegmentList.lastElement().endTime() = [" + extremaSegmentList.lastElement().endTime() + "]" + dataEntityWrapper.getPrimaryDataIndex());

            return extremaSegmentList;
        });
    }

    /**
     * Computes the standard deviation of the values in the list of PrimitiveDataEntity.
     * Uses Apache Commons Math for high-precision calculations.
     *
     * @return The standard deviation of the values.
     */
    private static double getStandardDeviation(Vector<PrimitiveDataEntity> primitiveList) {
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
