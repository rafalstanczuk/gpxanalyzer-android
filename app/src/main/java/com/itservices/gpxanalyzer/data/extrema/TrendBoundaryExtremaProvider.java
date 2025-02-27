package com.itservices.gpxanalyzer.data.extrema;

import android.util.Log;

import com.itservices.gpxanalyzer.data.TrendStatistics;
import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.data.TrendBoundaryDataEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.reactivex.Single;

public final class TrendBoundaryExtremaProvider {


    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static Single<List<TrendBoundaryDataEntity>> provide(StatisticResults statisticResults) {
        return Single.fromCallable(() -> {

            List<PrimitiveDataEntity> primitiveList = DataPrimitiveMapper.mapFrom(statisticResults);
            double[] windowFunctionWeights = ExtremaSegmentDetector.generateWindowFunction(5, ExtremaSegmentDetector.WindowType.GAUSSIAN, 0);


            double dMinMax = statisticResults.getMaxValue() - statisticResults.getMinValue();

            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), " statisticResults Min Max " + statisticResults.getMinValue() + " to " + statisticResults.getMaxValue());
            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), " statisticResults dMinMax " + dMinMax);


            double minAscAmp = dMinMax / 5; //20;
            double minAscDerivative = 0.001;
            double minDescAmp = dMinMax / 5; //20;
            double minDescDerivative = 0.001;


            ExtremaSegmentDetector segmentDetector = new ExtremaSegmentDetector();
            segmentDetector.preprocessAndFindExtrema(primitiveList, ExtremaSegmentDetector.DEFAULT_MAX_VALUE_ACCURACY, windowFunctionWeights);

            List<ExtremaSegmentDetector.Segment> etremumSegmentList
                    = segmentDetector.detectSegmentsOneRun(minAscAmp, minAscDerivative, minDescAmp, minDescDerivative);

            Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), "statisticResults.getDataEntityVector(): " + statisticResults.getDataEntityVector().size());


            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();
            int id = 0;
            int sumTestCount = 0;
            float sumAscending = 0;
            float sumDescending = 0;
            float sumFlatDrift = 0;

            int prevLastIndex = 0;

            for (ExtremaSegmentDetector.Segment segment : etremumSegmentList) {




                if (prevLastIndex != segment.startIndex){
                    Vector<DataEntity> missingSegmentDataEntityVector = new Vector<>();

                    for (int i = prevLastIndex; i <= segment.startIndex; i++) {
                        missingSegmentDataEntityVector.add(dataEntityVector.get(i));
                    }

                    DataEntity dataEntityStart = missingSegmentDataEntityVector.get(0);
                    DataEntity dataEntityEnd = missingSegmentDataEntityVector.get( missingSegmentDataEntityVector.size() - 1);

                    float deltaValFlat = Math.abs(
                            statisticResults.getValue(dataEntityStart)
                                    -
                            statisticResults.getValue(dataEntityEnd)
                    );

                    sumFlatDrift += deltaValFlat;

                    trendBoundaryDataEntities.add(
                            new TrendBoundaryDataEntity(id,
                                    new TrendStatistics(TrendType.CONSTANT, deltaValFlat, sumFlatDrift),
                                    dataEntityStart.timestampMillis(),
                                    dataEntityEnd.timestampMillis(),
                                    missingSegmentDataEntityVector
                            ));
                    id++;

                    sumTestCount += missingSegmentDataEntityVector.size();

                    Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), TrendType.CONSTANT.name() + " missing segment from " + dataEntityStart.timestampMillis() + " to " + dataEntityEnd.timestampMillis() + " deltaValFlat: +" + deltaValFlat + " missingSegmentDataEntityVector.size: " + missingSegmentDataEntityVector.size());

                }




                Vector<DataEntity> segmentDataEntityVector = new Vector<>();
                for (int i = segment.startIndex; i <= segment.endIndex; i++) {
                    segmentDataEntityVector.add(dataEntityVector.get(i));
                }
                prevLastIndex = segment.endIndex;


                float deltaVal = Math.abs(
                        segmentDataEntityVector.get(0).valueList().get(statisticResults.getPrimaryDataIndex())
                                -
                                segmentDataEntityVector.get(segmentDataEntityVector.size() - 1).valueList().get(statisticResults.getPrimaryDataIndex())
                );


                TrendType trendType = TrendTypeMapper.map(segment.type);
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
                        new TrendBoundaryDataEntity(id,
                                new TrendStatistics(trendType, deltaVal, trendTypeTotalSum),
                                segment.startTime,
                                segment.endTime,
                                segmentDataEntityVector
                        ));
                id++;

                sumTestCount += segmentDataEntityVector.size();

                Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), trendType.name() + " segment from " + segment.startTime + " to " + segment.endTime + " deltaVal: +" + deltaVal + " segmentDataEntityVector.size: " + segmentDataEntityVector.size());
            }

            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), "sumTestCount all segments: " + sumTestCount);
            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), "sumAscending all segments: " + sumAscending);
            Log.d(TrendBoundaryExtremaProvider.class.getSimpleName(), "sumDescending all segments: " + sumDescending);

            return trendBoundaryDataEntities;
        });
    }
}
