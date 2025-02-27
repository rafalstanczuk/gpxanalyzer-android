package com.itservices.gpxanalyzer.data.provider;

import android.util.Log;
import android.util.Pair;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.calculation.DataPrimitiveMapper;
import com.itservices.gpxanalyzer.data.gpx.calculation.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.gpx.calculation.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.statistics.TrendType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TrendBoundaryDataEntityProvider {


    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults) {

        //TODO: MOCK of the func.  GPXAN-21
        // TODO: Add filtering/processing for ascending/descending segments detection

        List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom( statisticResults );
        double[] triWeights = ExtremaSegmentDetector.generateWindowFunction(7, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0);
        double minAscAmp = 30;
        double minAscDerivative = 0.001;
        double minDescAmp = 30;
        double minDescDerivative = 0.001;
        List<Pair<Long, Long>> ascendingSegments =
                ExtremaSegmentDetector.detectAscendingSegments(primitiveList, minAscAmp, minAscDerivative,
                        ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, triWeights);

        List<Pair<Long, Long>> descendingSegments =
                ExtremaSegmentDetector.detectDescendingSegments(primitiveList, minDescAmp, minDescDerivative,
                        ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, triWeights);


        Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();
        int id=0;

        for (Pair<Long, Long> seg : ascendingSegments) {
            long startT = seg.first;
            long endT =   seg.second;

            long min = Math.min(startT, endT);
            long max = Math.max(startT, endT);

            startT = min;
            endT =  max;

            long finalStartT = startT;
            long finalEndT = endT;
            Vector<DataEntity> segmentDataEntityVector =
                    dataEntityVector
                            .stream()
                            .filter(entity -> (entity.getTimestampMillis() >= finalStartT && entity.getTimestampMillis() <= finalEndT) )
                            .collect(Collectors.toCollection(Vector::new));


            float deltaVal = Math.abs(
                    segmentDataEntityVector.get( 0                                  ).getValueList().get(statisticResults.getPrimaryDataIndex())
                    -
                    segmentDataEntityVector.get( segmentDataEntityVector.size() - 1 ).getValueList().get(statisticResults.getPrimaryDataIndex())
            );

            trendBoundaryDataEntities.add(
                    new TrendBoundaryDataEntity(id,
                            new TrendStatistics(TrendType.UP, deltaVal),
                            startT,
                            endT,
                            segmentDataEntityVector
                    ));
            id++;

            Log.d(ExtremaSegmentDetector.class.getSimpleName(), "Ascending segment from " + startT + " to " + endT + " deltaVal: +" + deltaVal+ " segmentDataEntityVector.size: " +segmentDataEntityVector.size());
        }

        for (Pair<Long, Long> seg : descendingSegments) {
            long startT = seg.first;
            long endT =   seg.second;

            long min = Math.min(startT, endT);
            long max = Math.max(startT, endT);

            startT = min;
            endT =  max;

            long finalStartT = startT;
            long finalEndT = endT;
            Vector<DataEntity> segmentDataEntityVector =
                    dataEntityVector
                            .stream()
                            .filter(entity -> (entity.getTimestampMillis() >= finalStartT && entity.getTimestampMillis() <= finalEndT) )
                            .collect(Collectors.toCollection(Vector::new));


            float deltaVal = Math.abs(
                    segmentDataEntityVector.get( 0                                  ).getValueList().get(statisticResults.getPrimaryDataIndex())
                            -
                            segmentDataEntityVector.get( segmentDataEntityVector.size() - 1 ).getValueList().get(statisticResults.getPrimaryDataIndex())
            );

            trendBoundaryDataEntities.add(
                    new TrendBoundaryDataEntity(id,
                            new TrendStatistics(TrendType.DOWN, deltaVal),
                            startT,
                            endT,
                            segmentDataEntityVector
                    ));

            id++;
            Log.d(ExtremaSegmentDetector.class.getSimpleName(), "Descending segment from " + startT + " to " + endT + " deltaVal: +" + deltaVal+ " segmentDataEntityVector.size: " +segmentDataEntityVector.size());
        }


        trendBoundaryDataEntities.sort(Comparator.comparingLong(TrendBoundaryDataEntity::beginTimestamp));

        AtomicInteger atomicIntegerId = new AtomicInteger(0);

        return trendBoundaryDataEntities.stream().map(
                entity -> {
                    TrendBoundaryDataEntity newEntity = new TrendBoundaryDataEntity(
                            atomicIntegerId.get(),
                            entity.trendStatistics(),
                            entity.beginTimestamp(),
                            entity.endTimestamp(),
                            entity.dataEntityVector());

                    atomicIntegerId.addAndGet(1);

                    Log.d(ExtremaSegmentDetector.class.getSimpleName(), newEntity.toString());

                    return newEntity;
                }
        ).collect(Collectors.toList());
    }
}
