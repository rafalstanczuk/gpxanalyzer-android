package com.itservices.gpxanalyzer.data.extrema;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.data.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentTrendType;

import java.util.Comparator;
import java.util.List;

import io.reactivex.Single;

public class ExtremaSegmentListProvider {
    private static double[] windowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(9, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0.2);

    public static Single<List<Segment>> provide(StatisticResults statisticResults) {
        return Single.fromCallable(() -> {
            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(statisticResults);

            primitiveList.sort(Comparator.comparingLong(PrimitiveDataEntity::getTimestamp));

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();
            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunctionWeights);

            // TODO: parameters can be changed by USER  !!!
            SegmentThresholds segmentThresholds = getSegmentThresholds(statisticResults);

            List<Segment> extremaSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            extremaSegmentList
                    = segmentDetector.addMissingSegments(extremaSegmentList, segmentThresholds);

            return extremaSegmentList;
        });
    }

    @NonNull
    private static SegmentThresholds getSegmentThresholds(StatisticResults statisticResults) {
        double dMinMax = statisticResults.getDeltaMinMax();

        // TODO: parameters can be changed by USER  !!!
        SegmentThresholds segmentThresholds = new SegmentThresholds(dMinMax / 5, 0.001, dMinMax / 5, 0.001);
        return segmentThresholds;
    }
}
