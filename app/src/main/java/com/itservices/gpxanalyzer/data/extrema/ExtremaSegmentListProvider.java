package com.itservices.gpxanalyzer.data.extrema;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;

import java.util.Comparator;
import java.util.List;

import io.reactivex.Single;

public class ExtremaSegmentListProvider {
    private static final double[] alpineSkiWindowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(9, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0.2);

    public static Single<List<Segment>> provide(DataEntityWrapper dataEntityWrapper) {
        return Single.fromCallable(() -> {
            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(dataEntityWrapper);

            primitiveList.sort(Comparator.comparingLong(PrimitiveDataEntity::getTimestamp));

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();
            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, alpineSkiWindowFunctionWeights);

            // TODO: parameters can be changed by USER by for ex. select type of activity !!!
            SegmentThresholds segmentThresholds = getAlpineSkiSegmentThresholds(dataEntityWrapper);

            List<Segment> extremaSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            extremaSegmentList
                    = segmentDetector.addMissingSegments(extremaSegmentList, segmentThresholds);

            return extremaSegmentList;
        });
    }

    @NonNull
    private static SegmentThresholds getAlpineSkiSegmentThresholds(DataEntityWrapper dataEntityWrapper) {
        double dMinMax = dataEntityWrapper.getDeltaMinMax();

        // TODO: parameters can be changed by USER  !!!
        SegmentThresholds segmentThresholds = new SegmentThresholds(dMinMax / 5, 0.001, dMinMax / 5, 0.001);
        return segmentThresholds;
    }
}
