package com.itservices.gpxanalyzer.data.extrema;

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

            double dMinMax = statisticResults.getDeltaMinMax();

            //Log.d(TrendBoundaryProvider.class.getSimpleName(), " statisticResults Min Max " + statisticResults.getMinValue() + " to " + statisticResults.getMaxValue());
            //Log.d(TrendBoundaryProvider.class.getSimpleName(), " statisticResults dMinMax " + dMinMax);

            SegmentThresholds segmentThresholds = new SegmentThresholds(dMinMax / 5, 0.001, dMinMax / 5, 0.001);

            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();
            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunctionWeights);

            List<Segment> extremumSegmentList
                    = segmentDetector.detectSegmentsOneRun(segmentThresholds);

            Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

            //Log.d(TrendBoundaryProvider.class.getSimpleName(), "statisticResults.getDataEntityVector(): " + statisticResults.getDataEntityVector().size());

            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();
            int id = 0;
            float sumAscending = 0;
            float sumDescending = 0;

            TrendBoundaryDataEntity prevMissingBoundary = null;

            for (int iExtremum = 0; iExtremum < extremumSegmentList.size() - 1; iExtremum++) {

                Segment segment = extremumSegmentList.get(iExtremum);

                if (iExtremum > 1) {
                    Segment prevSegment = extremumSegmentList.get(iExtremum - 1);

                    Vector<DataEntity> missingSegment = detectAndGetMissingSegment(prevSegment, segment, dataEntityVector);

                    if (missingSegment != null) {
                        TrendBoundaryDataEntity missingBoundary = getMissingTrendBoundaryDataEntity(id++, missingSegment, prevMissingBoundary, statisticResults);
                        prevMissingBoundary = missingBoundary;

                        trendBoundaryDataEntities.add(missingBoundary);

                        //Log.d(TrendBoundaryProvider.class.getSimpleName(), TrendType.CONSTANT.name() + " missing segment from " + missingSegment.firstElement().timestampMillis() + " to " + missingSegment.lastElement() + " deltaValFlat: +" + missingBoundary.trendStatistics().deltaVal() + " missingSegmentDataEntityVector.size: " + missingSegment.size());
                    }
                }

                Vector<DataEntity> segmentDataEntityVector = mapIntoSegmentDataEntityVector(segment, dataEntityVector);

                DataEntity dataEntityStart = dataEntityVector.get(segment.startIndex());
                DataEntity dataEntityEnd = dataEntityVector.get(segment.endIndex());

                float deltaVal = Math.abs(
                        statisticResults.getValue(dataEntityStart)
                                -
                        statisticResults.getValue(dataEntityEnd)
                );

                TrendType trendType = TrendTypeMapper.map(segment.type());
                float trendTypeTotalSum = 0;
                switch (trendType) {
                    case UP -> {
                        sumAscending += deltaVal;
                        trendTypeTotalSum = sumAscending;
                    }
                    case CONSTANT -> {

                    }
                    case DOWN -> {
                        sumDescending += deltaVal;
                        trendTypeTotalSum = sumDescending;
                    }
                }

                trendBoundaryDataEntities.add(
                        new TrendBoundaryDataEntity(id++,
                                new TrendStatistics(trendType, deltaVal, trendTypeTotalSum),
                                segmentDataEntityVector
                        ));

                //Log.d(TrendBoundaryProvider.class.getSimpleName(), trendType.name() + " segment from " + segment.startTime + " to " + segment.endTime + " deltaVal: +" + deltaVal + " segmentDataEntityVector.size: " + segmentDataEntityVector.size());
            }

            //Log.d(TrendBoundaryProvider.class.getSimpleName(), "sumTestCount all segments: " + sumTestCount);
            //Log.d(TrendBoundaryProvider.class.getSimpleName(), "sumAscending all segments: " + sumAscending);
            //Log.d(TrendBoundaryProvider.class.getSimpleName(), "sumDescending all segments: " + sumDescending);

            return trendBoundaryDataEntities;
        });
    }

    private static TrendBoundaryDataEntity getMissingTrendBoundaryDataEntity(int id, Vector<DataEntity> missingSegmentDataEntityVector, @Nullable TrendBoundaryDataEntity prevMissingTrendBoundaryDataEntity, StatisticResults statisticResults) {

        float deltaValMissing = Math.abs(
                statisticResults.getValue(missingSegmentDataEntityVector.firstElement())
                        -
                statisticResults.getValue(missingSegmentDataEntityVector.lastElement())
        );

        float sumFlatDrift = 0.0f;

        if (prevMissingTrendBoundaryDataEntity != null) {
            sumFlatDrift = prevMissingTrendBoundaryDataEntity.trendStatistics().deltaVal();
        }

        sumFlatDrift += deltaValMissing;

        return new TrendBoundaryDataEntity(id,
                new TrendStatistics(TrendType.CONSTANT, deltaValMissing, sumFlatDrift),
                missingSegmentDataEntityVector
        );
    }

    @NonNull
    private static Vector<DataEntity> mapIntoSegmentDataEntityVector(Segment segment, Vector<DataEntity> dataEntityVector) {
        return getPartial(segment.startIndex(), segment.endIndex(), dataEntityVector);
    }

    private static Vector<DataEntity> detectAndGetMissingSegment(Segment prevSegment, Segment segment, Vector<DataEntity> allDataEntityVector) {
        if (prevSegment.endIndex() != segment.startIndex()) {
            return getPartial(prevSegment.endIndex(), segment.startIndex(), allDataEntityVector);
        }

        return null;
    }

    private static Vector<DataEntity> getPartial(int startIndex, int endIndex, Vector<DataEntity> allDataEntityVector) {
        Vector<DataEntity> entityVector = new Vector<>();

        for (int i = startIndex; i <= endIndex; i++) {
            entityVector.add(allDataEntityVector.get(i));
        }

        return entityVector;
    }
}
