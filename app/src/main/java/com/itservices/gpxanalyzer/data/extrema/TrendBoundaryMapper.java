package com.itservices.gpxanalyzer.data.extrema;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.TrendStatistics;
import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.data.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.extrema.detector.TrendTypeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nullable;

public final class TrendBoundaryMapper {

    public static List<TrendBoundaryDataEntity> mapFrom(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static List<TrendBoundaryDataEntity> mapFrom(StatisticResults statisticResults, List<Segment> extremaSegmentList) {

            Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

            TrendBoundaryDataEntity prevConstantBoundary = null;
            TrendBoundaryDataEntity prevAscendingBoundary = null;
            TrendBoundaryDataEntity prevDescendingBoundary = null;

            int testN = 0;

            Log.d(TrendBoundaryMapper.class.getSimpleName(), "statisticResults.getDataEntityVector().size(): " + statisticResults.getDataEntityVector().size());

            for (Segment segment : extremaSegmentList) {
                Vector<DataEntity> segmentDataEntityVector = mapIntoSegmentDataEntityVector(segment, dataEntityVector);

                TrendType trendType = TrendTypeMapper.map(segment.type());

                TrendBoundaryDataEntity trendBoundaryDataEntity = null;
                int index = extremaSegmentList.indexOf(segment);

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

                testN += trendBoundaryDataEntity.dataEntityVector().size();

                Log.d(TrendBoundaryMapper.class.getSimpleName(), trendBoundaryDataEntity.toString());
            }


            Log.d(TrendBoundaryMapper.class.getSimpleName(), "testN: " + (testN));

            return trendBoundaryDataEntities;
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
