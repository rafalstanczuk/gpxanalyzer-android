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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.Single;

public class TrendBoundaryDataEntityProvider {


    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static Single<List<TrendBoundaryDataEntity>> provide(StatisticResults statisticResults) {
        return Single.fromCallable(() -> {

            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(statisticResults);
            double[] windowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(7, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0);


            double dMinMax = statisticResults.getMaxValue() - statisticResults.getMinValue();

            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), " statisticResults Min Max " + statisticResults.getMinValue() + " to " + statisticResults.getMaxValue() );
            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), " statisticResults dMinMax " + dMinMax );



            double minAscAmp = dMinMax/3; //20;
            double minAscDerivative = 0.01;
            double minDescAmp = dMinMax/3; //20;
            double minDescDerivative = 0.01;



            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();

            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunctionWeights);

            List<Pair<Long, Long>> ascendingSegments =
                    segmentDetector.detectAscendingSegments(minAscAmp, minAscDerivative);

            List<Pair<Long, Long>> descendingSegments =
                    segmentDetector.detectDescendingSegments(minDescAmp, minDescDerivative);


            Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "sumTestCount statisticResults.getDataEntityVector(): " + statisticResults.getDataEntityVector().size());


            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();
            int id = 0;

            int sumTestCount=0;

            float sumAscending=0;
            float sumDescending=0;

            for (Pair<Long, Long> seg : ascendingSegments) {
                long startT = seg.first;
                long endT = seg.second;

                long min = Math.min(startT, endT);
                long max = Math.max(startT, endT);

                startT = min;
                endT = max;

                long finalStartT = startT;
                long finalEndT = endT;
                Vector<DataEntity> segmentDataEntityVector =
                        dataEntityVector
                                .stream()
                                .filter(entity -> (entity.getTimestampMillis() >= finalStartT && entity.getTimestampMillis() <= finalEndT))
                                .collect(Collectors.toCollection(Vector::new));


                float deltaVal = Math.abs(
                        segmentDataEntityVector.get(0).getValueList().get(statisticResults.getPrimaryDataIndex())
                                -
                                segmentDataEntityVector.get(segmentDataEntityVector.size() - 1).getValueList().get(statisticResults.getPrimaryDataIndex())
                );

                sumAscending+=deltaVal;

                trendBoundaryDataEntities.add(
                        new TrendBoundaryDataEntity(id,
                                new TrendStatistics(TrendType.UP, deltaVal, sumAscending),
                                startT,
                                endT,
                                segmentDataEntityVector
                        ));
                id++;

                sumTestCount+=segmentDataEntityVector.size();

                Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "Ascending segment from " + startT + " to " + endT + " deltaVal: +" + deltaVal + " segmentDataEntityVector.size: " + segmentDataEntityVector.size());
            }

            for (Pair<Long, Long> seg : descendingSegments) {
                long startT = seg.first;
                long endT = seg.second;

                long min = Math.min(startT, endT);
                long max = Math.max(startT, endT);

                startT = min;
                endT = max;

                long finalStartT = startT;
                long finalEndT = endT;
                Vector<DataEntity> segmentDataEntityVector =
                        dataEntityVector
                                .stream()
                                .filter(entity -> (entity.getTimestampMillis() >= finalStartT && entity.getTimestampMillis() <= finalEndT))
                                .collect(Collectors.toCollection(Vector::new));


                float deltaVal = Math.abs(
                        segmentDataEntityVector.get(0).getValueList().get(statisticResults.getPrimaryDataIndex())
                                -
                                segmentDataEntityVector.get(segmentDataEntityVector.size() - 1).getValueList().get(statisticResults.getPrimaryDataIndex())
                );

                sumDescending+=deltaVal;

                trendBoundaryDataEntities.add(
                        new TrendBoundaryDataEntity(id,
                                new TrendStatistics(TrendType.DOWN, deltaVal, sumDescending),
                                startT,
                                endT,
                                segmentDataEntityVector
                        ));

                id++;

                sumTestCount+=segmentDataEntityVector.size();

                Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "Descending segment from " + startT + " to " + endT + " deltaVal: +" + deltaVal + " segmentDataEntityVector.size: " + segmentDataEntityVector.size());
            }


            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "sumTestCount all segments: " + sumTestCount);

            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "sumAscending all segments: " + sumAscending);
            Log.d(TrendBoundaryDataEntityProvider.class.getSimpleName(), "sumDescending all segments: " + sumDescending);

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

                        Log.d(ExtremaSegmentDetector.class.getSimpleName(), "t1 - t2: " + entity.beginTimestamp() + " - " + entity.endTimestamp() );

                        return newEntity;
                    }
            ).collect(Collectors.toList());
        });
    }
}
