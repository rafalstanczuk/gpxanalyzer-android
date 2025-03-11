package com.itservices.gpxanalyzer.data.extrema;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.TrendStatistics;
import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.data.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentTrendType;
import com.itservices.gpxanalyzer.data.extrema.detector.TrendTypeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nullable;

import io.reactivex.Single;

public final class TrendBoundaryProvider {
    private static double[] windowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(9, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0.2);

    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static Single<List<TrendBoundaryDataEntity>> provide(StatisticResults statisticResults) {
        return Single.fromCallable(() -> {

            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(statisticResults);

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();
            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunctionWeights);

            // TODO: parameters can be changed by USER  !!!
            SegmentThresholds segmentThresholds = getSegmentThresholds(statisticResults);

            List<Segment> extremumSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            extremumSegmentList
                    = segmentDetector.addMissingSegments(extremumSegmentList, SegmentTrendType.CONSTANT);

            Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

            TrendBoundaryDataEntity prevConstantBoundary = null;
            TrendBoundaryDataEntity prevAscendingBoundary = null;
            TrendBoundaryDataEntity prevDescendingBoundary = null;

            int testN = 0;

            Log.d(TrendBoundaryProvider.class.getSimpleName(), "statisticResults.getDataEntityVector().size(): " + statisticResults.getDataEntityVector().size());

            for (Segment segment : extremumSegmentList) {
                Vector<DataEntity> segmentDataEntityVector = mapIntoSegmentDataEntityVector(segment, dataEntityVector);

                TrendType trendType = TrendTypeMapper.map(segment.type());

                TrendBoundaryDataEntity trendBoundaryDataEntity = null;
                int index = extremumSegmentList.indexOf(segment);

                switch (trendType) {
                    case UP -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevAscendingBoundary, statisticResults);
                        prevAscendingBoundary = trendBoundaryDataEntity;
                    }
                    case CONSTANT -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevConstantBoundary, statisticResults);
                        prevConstantBoundary = trendBoundaryDataEntity;
                    }
                    case DOWN -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevDescendingBoundary, statisticResults);
                        prevDescendingBoundary = trendBoundaryDataEntity;
                    }
                }

                trendBoundaryDataEntities.add(trendBoundaryDataEntity);

                testN +=trendBoundaryDataEntity.dataEntityVector().size();

                Log.d(TrendBoundaryProvider.class.getSimpleName(), trendBoundaryDataEntity.toString());
            }


            Log.d(TrendBoundaryProvider.class.getSimpleName(), "testN: " + (testN));

            return trendBoundaryDataEntities;
        });
    }

    private static float getDeltaVal(StatisticResults statisticResults, Vector<DataEntity> segmentDataEntityVector) {
        DataEntity dataEntityStart = segmentDataEntityVector.firstElement();
        DataEntity dataEntityEnd = segmentDataEntityVector.lastElement();

        return Math.abs(
                statisticResults.getValue(dataEntityStart)
                        -
                statisticResults.getValue(dataEntityEnd)
        );
    }

    @NonNull
    private static SegmentThresholds getSegmentThresholds(StatisticResults statisticResults) {
        double dMinMax = statisticResults.getDeltaMinMax();

        // TODO: parameters can be changed by USER  !!!
        SegmentThresholds segmentThresholds = new SegmentThresholds(dMinMax / 5, 0.001, dMinMax / 5, 0.001);
        return segmentThresholds;
    }

    private static TrendBoundaryDataEntity getTrendBoundaryDataEntity(
            int id,
            Vector<DataEntity> segmentDataEntityVector,
            TrendType trendType,
            @Nullable TrendBoundaryDataEntity prevTrendBoundaryDataEntity,
            StatisticResults statisticResults) {

        float deltaVal = getDeltaVal(statisticResults, segmentDataEntityVector);

        float sumDeltaVal = 0.0f;

        if (prevTrendBoundaryDataEntity != null) {
            sumDeltaVal = prevTrendBoundaryDataEntity.trendStatistics().sumCumulativeDeltaValIncluded();
        }

        sumDeltaVal += deltaVal;

        //Log.d("getTrendBoundaryDataEntity", "id ="+id + ", trendType =" + trendType.name() + ", deltaVal="+deltaVal + ", sumDeltaVal=" + sumDeltaVal );

        return new TrendBoundaryDataEntity(id,
                new TrendStatistics(trendType, deltaVal, sumDeltaVal),
                segmentDataEntityVector
        );
    }

    @NonNull
    private static Vector<DataEntity> mapIntoSegmentDataEntityVector(Segment segment, Vector<DataEntity> dataEntityVector) {
        return getPartial(segment.startIndex(), segment.endIndex(), dataEntityVector);
    }

    private static Vector<DataEntity> getPartial(int startIndex, int endIndex, Vector<DataEntity> allDataEntityVector) {
        Vector<DataEntity> entityVector = new Vector<>();

        for (int i = startIndex; i <= endIndex; i++) {
            entityVector.add(allDataEntityVector.get(i));
        }

        return entityVector;
    }
}
