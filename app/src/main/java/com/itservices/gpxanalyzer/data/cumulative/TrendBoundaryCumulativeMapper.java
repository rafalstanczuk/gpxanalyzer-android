package com.itservices.gpxanalyzer.data.cumulative;

import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE;
import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.TrendTypeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nullable;

public final class TrendBoundaryCumulativeMapper {

    public static List<TrendBoundaryDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static List<TrendBoundaryDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper, List<Segment> extremaSegmentList) {

            Vector<DataEntity> dataEntityVector = dataEntityWrapper.getData();

            Log.d("TrendBoundaryDataEntity", "mapFrom() called with: dataEntityVector = [" + dataEntityVector.size() + "]");

            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

            TrendBoundaryDataEntity prevConstantBoundary = null;
            TrendBoundaryDataEntity prevAscendingBoundary = null;
            TrendBoundaryDataEntity prevDescendingBoundary = null;

            //Log.d(TrendBoundaryMapper.class.getSimpleName(), "dataEntityWrapper.getDataEntityVector().size(): " + dataEntityWrapper.getData().size());

            for (Segment segment : extremaSegmentList) {
                Vector<DataEntity> segmentDataEntityVector = mapIntoSegmentDataEntityVector(segment, dataEntityVector);

                TrendType trendType = TrendTypeMapper.map(segment.type());

                TrendBoundaryDataEntity trendBoundaryDataEntity = null;
                int index = extremaSegmentList.indexOf(segment);

                switch (trendType) {
                    case UP -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevAscendingBoundary, dataEntityWrapper);
                        prevAscendingBoundary = trendBoundaryDataEntity;
                    }
                    case CONSTANT -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevConstantBoundary, dataEntityWrapper);
                        prevConstantBoundary = trendBoundaryDataEntity;
                    }
                    case DOWN -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataEntityVector, trendType, prevDescendingBoundary, dataEntityWrapper);
                        prevDescendingBoundary = trendBoundaryDataEntity;
                    }
                }

                trendBoundaryDataEntities.add(trendBoundaryDataEntity);

                //Log.d(TrendBoundaryMapper.class.getSimpleName(), trendBoundaryDataEntity.toString());
            }

            return trendBoundaryDataEntities;
    }

    private static float getDeltaValAbs(DataEntityWrapper dataEntityWrapper, Vector<DataEntity> segmentDataEntityVector) {
        DataEntity dataEntityStart = segmentDataEntityVector.firstElement();
        DataEntity dataEntityEnd = segmentDataEntityVector.lastElement();

        return Math.abs(
                dataEntityWrapper.getValue(dataEntityStart)
                        -
                        dataEntityWrapper.getValue(dataEntityEnd)
        );
    }

    private static TrendBoundaryDataEntity getTrendBoundaryDataEntity(
            int id,
            Vector<DataEntity> segmentDataEntityVector,
            TrendType trendType,
            @Nullable TrendBoundaryDataEntity prevTrendBoundaryDataEntity,
            DataEntityWrapper dataEntityWrapper) {

        TrendStatistics trendStatistics
                = createTrendStatisticsFor(
                        trendType, segmentDataEntityVector, prevTrendBoundaryDataEntity, dataEntityWrapper);

        addEveryDataEntityCumulativeStatistics(trendType, segmentDataEntityVector, prevTrendBoundaryDataEntity, dataEntityWrapper);

        //Log.d("getTrendBoundaryDataEntity", "id ="+id + ", trendType =" + trendType.name() + ", absDeltaVal="+absDeltaVal + ", sumDeltaVal=" + sumDeltaVal );

        return new TrendBoundaryDataEntity(id,
                trendStatistics,
                segmentDataEntityVector
        );
    }

    private static TrendStatistics createTrendStatisticsFor(TrendType trendType, Vector<DataEntity> segmentDataEntityVector, TrendBoundaryDataEntity prevTrendBoundaryDataEntity, DataEntityWrapper dataEntityWrapper) {
        float deltaValAbs = getDeltaValAbs(dataEntityWrapper, segmentDataEntityVector);

        float sumDeltaSegmentsFirstLastVal = 0.0f;
        int n = 0;

        if (prevTrendBoundaryDataEntity != null) {
            sumDeltaSegmentsFirstLastVal = prevTrendBoundaryDataEntity.trendStatistics().sumCumulativeAbsDeltaValIncluded();

            n = prevTrendBoundaryDataEntity.trendStatistics().n();
        }

        // Cumulative sum of abs deltas first->last value inside segment
        sumDeltaSegmentsFirstLastVal += deltaValAbs;

        // The count of this trend-type
        n++;

        return new TrendStatistics(trendType, deltaValAbs, sumDeltaSegmentsFirstLastVal, n);
    }

    private static void addEveryDataEntityCumulativeStatistics(TrendType trendType, Vector<DataEntity> segmentDataEntityVector, TrendBoundaryDataEntity prevTrendBoundaryDataEntity, DataEntityWrapper dataEntityWrapper) {

        DataEntity first;
        CumulativeStatistics cumulativeStatisticsFirst;

        if (prevTrendBoundaryDataEntity!= null ) {
            first = prevTrendBoundaryDataEntity.dataEntityVector().lastElement();
            cumulativeStatisticsFirst = dataEntityWrapper.getCumulativeStatistics(first, ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE);
        } else {
            cumulativeStatisticsFirst = new CumulativeStatistics();
            first = segmentDataEntityVector.firstElement();
        }

        String unit = dataEntityWrapper.getUnit(first);
        float accuracy = dataEntityWrapper.getAccuracy(first);

        float cumulativeFromSegmentStartValue = 0.0f;
        float cumulativeAllSumValue = cumulativeStatisticsFirst.value();

        Log.d(TrendBoundaryCumulativeMapper.class.getSimpleName(),
                trendType.name() +" add called with: cumulativeAllSumValue cumulativeStatisticsFirst.value() = [" + cumulativeAllSumValue + "]");

        for (int i = 1; i < segmentDataEntityVector.size(); i++) {

            DataEntity dataEntityToUpdate = segmentDataEntityVector.get(i);
            DataEntity prevDataEntity = segmentDataEntityVector.get(i - 1);

            float delta =
                    dataEntityWrapper.getValue( dataEntityToUpdate )
                            -
                    dataEntityWrapper.getValue( prevDataEntity );

            cumulativeFromSegmentStartValue += delta;
            float total = cumulativeAllSumValue + cumulativeFromSegmentStartValue;

            Log.d(TrendBoundaryCumulativeMapper.class.getSimpleName(),
                    trendType.name() +" add called with: total = [" + total + "]");

            dataEntityWrapper.putCumulativeStatistics(dataEntityToUpdate, FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                    new CumulativeStatistics(cumulativeFromSegmentStartValue, accuracy, unit));

            dataEntityWrapper.putCumulativeStatistics(dataEntityToUpdate, ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                    new CumulativeStatistics(total, accuracy, unit));
        }
    }

    @NonNull
    private static Vector<DataEntity> mapIntoSegmentDataEntityVector(Segment segment, Vector<DataEntity> dataEntityVector) {
        return getPartial(segment.startIndex(), segment.endIndex(), dataEntityVector);
    }

    private static Vector<DataEntity> getPartial(int startIndex, int endIndex, Vector<DataEntity> allDataEntityVector) {
        Log.d("TrendBoundaryDataEntity", "getPartial() called with: startIndex = [" + startIndex + "], endIndex = [" + endIndex + "]");
        Vector<DataEntity> entityVector = new Vector<>();

        for (int i = startIndex; i <= endIndex; i++) {
            entityVector.add(allDataEntityVector.get(i));
        }

        return entityVector;
    }
}
