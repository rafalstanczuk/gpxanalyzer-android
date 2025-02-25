package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.statistics.TrendType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TrendBoundaryDataEntityProvider {


    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    public static List<TrendBoundaryDataEntity> provide(StatisticResults statisticResults) {

        //TODO: MOCK of the func.  GPXAN-21
        // TODO: Add filtering/processing for ascending/descending segments detection

        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

        int ihalf = dataEntityVector.size() / 2;

        Vector<DataEntity> dataEntityVectorFirst = new Vector<>();
        Vector<DataEntity> dataEntityVectorSecond = new Vector<>();

        for(int i=0; i<dataEntityVector.size() ; i++) {

            if (i<ihalf) {
                dataEntityVectorFirst.add(dataEntityVector.get(i));
            } else {
                dataEntityVectorSecond.add(dataEntityVector.get(i));
            }

        }

        trendBoundaryDataEntities.add(
                new TrendBoundaryDataEntity(0,
                        new TrendStatistics(TrendType.UP, 20),
                        dataEntityVectorFirst.firstElement().getTimestampMillis(),
                        dataEntityVectorFirst.lastElement().getTimestampMillis(),
                        dataEntityVectorFirst
                ));


        dataEntityVectorSecond.add(0, dataEntityVectorFirst.lastElement());

        trendBoundaryDataEntities.add(
                new TrendBoundaryDataEntity(1,
                        new TrendStatistics(TrendType.DOWN, 40),
                        dataEntityVectorSecond.firstElement().getTimestampMillis(),
                        dataEntityVectorSecond.lastElement().getTimestampMillis(),
                        dataEntityVectorSecond
                ));

        return trendBoundaryDataEntities;
    }
}
