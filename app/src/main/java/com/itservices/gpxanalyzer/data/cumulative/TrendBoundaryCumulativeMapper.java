package com.itservices.gpxanalyzer.data.cumulative;

import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE;
import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.model.entity.DataEntity;
import com.itservices.gpxanalyzer.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.extrema.detector.Segment;
import com.itservices.gpxanalyzer.data.mapper.TrendTypeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nullable;

/**
 * Maps GPX data segments into trend boundaries with cumulative statistics.
 * <p>
 * This utility class is responsible for analyzing GPX data to identify and process
 * trend boundaries (coherent segments like ascents, descents, or flat sections).
 * It calculates various statistics for each boundary and updates data entities
 * with cumulative values, allowing for visualization and analysis of trends
 * within the GPX track.
 * <p>
 * The mapper processes extrema segments (identified by change points in the data),
 * converts them to trend boundaries, and calculates both segment-specific and
 * track-wide cumulative statistics.
 */
public final class TrendBoundaryCumulativeMapper {

    /**
     * Maps data entities into trend boundaries based on a specific trend type to highlight.
     * <p>
     * This method is a placeholder for future implementation that would highlight
     * specific trend types (such as ascents or descents) in the data.
     *
     * @param dataEntityWrapper The wrapper containing the data entities to process
     * @param trendTypeToHighlight The specific trend type to highlight
     * @return A list of trend boundary data entities
     */
    public static List<TrendBoundaryDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper, TrendType trendTypeToHighlight) {
        List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

        return trendBoundaryDataEntities;
    }

    /**
     * Maps data entities into trend boundaries based on extrema segments.
     * <p>
     * This method processes a list of extrema segments (identified by change points
     * in the data), converts them to trend boundaries, and calculates both
     * segment-specific and track-wide cumulative statistics for each data entity.
     *
     * @param dataEntityWrapper The wrapper containing the data entities to process
     * @param extremaSegmentList The list of extrema segments to process
     * @return A list of trend boundary data entities with cumulative statistics
     */
    public static List<TrendBoundaryDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper, Vector<Segment> extremaSegmentList) {

            Vector<DataEntity> dataEntityVector = dataEntityWrapper.getData();

            //Log.d("TrendBoundaryDataEntity", "mapFrom() called with: dataEntityVector = [" + dataEntityVector.size() + "]");

            List<TrendBoundaryDataEntity> trendBoundaryDataEntities = new ArrayList<>();

            TrendBoundaryDataEntity prevConstantBoundary = null;
            TrendBoundaryDataEntity prevAscendingBoundary = null;
            TrendBoundaryDataEntity prevDescendingBoundary = null;

            //Log.d(TrendBoundaryMapper.class.getSimpleName(), "dataEntityWrapper.getDataEntityVector().size(): " + dataEntityWrapper.getData().size());

            for (Segment segment : extremaSegmentList) {
                Pair<Vector<DataEntity>, List<Object>>  segmentDataPair = mapIntoSegmentDataEntityVector(segment, dataEntityVector);

                TrendType trendType = TrendTypeMapper.map(segment.type());

                TrendBoundaryDataEntity trendBoundaryDataEntity = null;
                int index = extremaSegmentList.indexOf(segment);

                switch (trendType) {
                    case UP -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataPair, trendType, prevAscendingBoundary, dataEntityWrapper);
                        prevAscendingBoundary = trendBoundaryDataEntity;
                    }
                    case CONSTANT -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataPair, trendType, prevConstantBoundary, dataEntityWrapper);
                        prevConstantBoundary = trendBoundaryDataEntity;
                    }
                    case DOWN -> {
                        trendBoundaryDataEntity = getTrendBoundaryDataEntity(index, segmentDataPair, trendType, prevDescendingBoundary, dataEntityWrapper);
                        prevDescendingBoundary = trendBoundaryDataEntity;
                    }
                }

                trendBoundaryDataEntities.add(trendBoundaryDataEntity);

                //Log.d(TrendBoundaryMapper.class.getSimpleName(), trendBoundaryDataEntity.toString());
            }

            return trendBoundaryDataEntities;
    }

    /**
     * Calculates the absolute delta value for a segment.
     * <p>
     * This method computes the absolute difference between the values
     * of the first and last data entities in a segment.
     *
     * @param dataEntityWrapper The wrapper containing the data entities
     * @param segmentDataEntityVector The vector of data entities in the segment
     * @return The absolute difference between first and last values
     */
    private static float getDeltaValAbs(DataEntityWrapper dataEntityWrapper, Vector<DataEntity> segmentDataEntityVector) {
        DataEntity dataEntityStart = segmentDataEntityVector.firstElement();
        DataEntity dataEntityEnd = segmentDataEntityVector.lastElement();

        return Math.abs(
                dataEntityWrapper.getValue(dataEntityStart)
                        -
                        dataEntityWrapper.getValue(dataEntityEnd)
        );
    }

    /**
     * Creates a trend boundary data entity from a segment with the appropriate statistics.
     * <p>
     * This method generates a new trend boundary data entity by calculating trend
     * statistics and processing cumulative statistics for each data entity in the segment.
     *
     * @param id The identifier for the new trend boundary
     * @param segmentDataPair The pair containing the data entities and extra data for the segment
     * @param trendType The type of trend (UP, DOWN, or CONSTANT)
     * @param prevTrendBoundaryDataEntity The previous trend boundary of the same type, or null if none
     * @param dataEntityWrapper The wrapper containing context for the data
     * @return A new trend boundary data entity with appropriate statistics
     */
    private static TrendBoundaryDataEntity getTrendBoundaryDataEntity(
            int id,
            Pair<Vector<DataEntity>, List<Object>> segmentDataPair,
            TrendType trendType,
            @Nullable TrendBoundaryDataEntity prevTrendBoundaryDataEntity,
            DataEntityWrapper dataEntityWrapper) {

        Vector<DataEntity> segmentDataEntityVector = segmentDataPair.first;


        TrendStatistics trendStatistics
                = createTrendStatisticsFor(
                        trendType, segmentDataEntityVector, prevTrendBoundaryDataEntity, dataEntityWrapper);

        addEveryDataEntityCumulativeStatistics(trendType, segmentDataEntityVector, prevTrendBoundaryDataEntity, dataEntityWrapper);

        //Log.d("getTrendBoundaryDataEntity", "id ="+id + ", trendType =" + trendType.name() + ", absDeltaVal="+absDeltaVal + ", sumDeltaVal=" + sumDeltaVal );

        return new TrendBoundaryDataEntity(id,
                trendStatistics,
                segmentDataEntityVector,
                segmentDataPair.second
        );
    }

    /**
     * Creates trend statistics for a segment based on its trend type and previous statistics.
     * <p>
     * This method calculates the absolute delta value for the segment and combines it
     * with statistics from previous segments of the same trend type to create cumulative
     * trend statistics.
     *
     * @param trendType The type of trend (UP, DOWN, or CONSTANT)
     * @param segmentDataEntityVector The vector of data entities in the segment
     * @param prevTrendBoundaryDataEntity The previous trend boundary of the same type, or null if none
     * @param dataEntityWrapper The wrapper containing context for the data
     * @return Trend statistics for the segment
     */
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

    /**
     * Adds cumulative statistics to each data entity in a segment.
     * <p>
     * This method calculates and stores both segment-relative and track-wide
     * cumulative statistics for each data entity in the segment, enabling
     * visualization and analysis of cumulative changes.
     *
     * @param trendType The type of trend (UP, DOWN, or CONSTANT)
     * @param segmentDataEntityVector The vector of data entities in the segment
     * @param prevTrendBoundaryDataEntity The previous trend boundary of the same type, or null if none
     * @param dataEntityWrapper The wrapper containing context for the data
     */
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

        //Log.d(TrendBoundaryCumulativeMapper.class.getSimpleName(),
        //        trendType.name() +" add called with: cumulativeAllSumValue cumulativeStatisticsFirst.value() = [" + cumulativeAllSumValue + "]");

        for (int i = 1; i < segmentDataEntityVector.size(); i++) {

            DataEntity dataEntityToUpdate = segmentDataEntityVector.get(i);
            DataEntity prevDataEntity = segmentDataEntityVector.get(i - 1);

            float delta =
                    dataEntityWrapper.getValue( dataEntityToUpdate )
                            -
                    dataEntityWrapper.getValue( prevDataEntity );

            cumulativeFromSegmentStartValue += delta;
            float total = cumulativeAllSumValue + cumulativeFromSegmentStartValue;

            //Log.d(TrendBoundaryCumulativeMapper.class.getSimpleName(),
            //        trendType.name() +" add called with: total = [" + total + "]");

            dataEntityWrapper.putCumulativeStatistics(dataEntityToUpdate, FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                    new CumulativeStatistics(cumulativeFromSegmentStartValue, accuracy, unit));

            dataEntityWrapper.putCumulativeStatistics(dataEntityToUpdate, ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                    new CumulativeStatistics(total, accuracy, unit));
        }
    }

    /**
     * Maps a segment to a vector of data entities and their extra data.
     * <p>
     * This method extracts the data entities within the start and end indices
     * of a segment from the complete data entity vector.
     *
     * @param segment The segment containing start and end indices
     * @param dataEntityVector The complete vector of data entities
     * @return A pair containing the segment's data entities and their extra data
     */
    @NonNull
    private static Pair<Vector<DataEntity>, List<Object>> mapIntoSegmentDataEntityVector(Segment segment, Vector<DataEntity> dataEntityVector) {
        return getPartial(segment.startIndex(), segment.endIndex(), dataEntityVector);
    }

    /**
     * Extracts a partial vector of data entities and their extra data.
     * <p>
     * This method creates a new vector containing only the data entities
     * within the specified index range, along with their extra data.
     *
     * @param startIndex The starting index (inclusive)
     * @param endIndex The ending index (inclusive)
     * @param allDataEntityVector The complete vector of data entities
     * @return A pair containing the extracted data entities and their extra data
     */
    private static Pair<Vector<DataEntity>, List<Object>> getPartial(int startIndex, int endIndex, Vector<DataEntity> allDataEntityVector) {
        //Log.d("TrendBoundaryDataEntity", "getPartial() called with: startIndex = [" + startIndex + "], endIndex = [" + endIndex + "]");
        Vector<DataEntity> entityVector = new Vector<>();
        List<Object> extraDataList = new ArrayList<>();

        for (int i = startIndex; i <= endIndex; i++) {
            DataEntity dataEntity = allDataEntityVector.get(i);

            entityVector.add(dataEntity);
            extraDataList.add(dataEntity.getExtraData());
        }

        return new Pair<>(entityVector, extraDataList);
    }
}
