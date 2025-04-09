package com.itservices.gpxanalyzer.chart;

import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Handles scaling operations for DataEntityLineChart instances.
 * <p>
 * This class is responsible for determining and applying appropriate Y-axis scales
 * based on the data being displayed in a chart. It considers both the actual data values
 * from the DataEntityWrapper and any limit lines that may be displayed on the chart
 * to calculate optimal minimum and maximum Y-axis values.
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Calculating appropriate Y-axis ranges based on data extremes</li>
 *   <li>Adding visual padding around data for better presentation</li>
 *   <li>Ensuring limit lines are visible within the chart's visible area</li>
 *   <li>Preventing excessive zoom that could hide important data features</li>
 *   <li>Adapting scale based on the specific type of GPX data being displayed</li>
 * </ul>
 * <p>
 * The scaler adds appropriate padding (offset) to the top and bottom of the visible range
 * to ensure that all data points and limit lines are visible with adequate spacing.
 * The typical padding is 10% of the data range, which provides enough visual space
 * without wasting too much of the chart area.
 * <p>
 * This class is package-private and intended for use only by components within the chart package.
 * It is typically used by the {@link DataEntityLineChart} to initialize appropriate scaling
 * when new data is loaded or when the chart configuration changes.
 */
class LineChartScaler {
    /** 
     * Default minimum Y-axis value when no data is available.
     * This provides a sensible starting point for empty charts.
     */
    private static final double DEFAULT_MIN_Y_VALUE = 0.0;
    
    /** 
     * Current minimum Y value, used in scale calculations.
     * This is updated whenever new data is processed.
     */
    private double minY = DEFAULT_MIN_Y_VALUE;
    
    /** 
     * The data entity wrapper containing the data to be visualized.
     * This provides access to the actual data points and statistics needed for scaling.
     */
    private DataEntityWrapper dataEntityWrapper = null;

    /** 
     * The limit lines boundaries to consider when scaling the chart.
     * These represent important thresholds or reference values that should be visible.
     */
    private LimitLinesBoundaries limitLinesBoundaries;

    /**
     * Creates a new LineChartScaler with default values.
     * <p>
     * Uses Dagger for dependency injection, allowing the scaler to be
     * instantiated without manual creation of dependencies.
     */
    @Inject
    LineChartScaler() {
    }

    /**
     * Sets the data entity wrapper to use for scaling calculations.
     * <p>
     * This method should be called before {@link #scale(DataEntityLineChart)}
     * to ensure the scaler has access to the data that will be displayed.
     * 
     * @param dataEntityWrapper The data entity wrapper containing the data points to scale
     */
    public void setDataEntityWrapper(DataEntityWrapper dataEntityWrapper) {
        this.dataEntityWrapper = dataEntityWrapper;
    }

    /**
     * Sets the limit lines boundaries to consider when scaling the chart.
     * <p>
     * Limit lines represent important thresholds or reference values that should
     * be visible on the chart. The scaler ensures that these lines are included
     * within the chart's visible area.
     * 
     * @param limitLinesBoundaries The limit lines boundaries to use
     */
    public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
        this.limitLinesBoundaries = limitLinesBoundaries;
    }

    /**
     * Calculates and applies appropriate Y-axis scaling to the given chart.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Calculates the data range from the data entity wrapper</li>
     *   <li>Adds a small offset to create visual padding</li>
     *   <li>Collects values from both data and limit lines to determine the overall scale</li>
     *   <li>Sets the minimum and maximum Y-axis values on the chart with appropriate padding</li>
     * </ol>
     * <p>
     * The method ensures that both the actual data range and any limit lines are
     * visible within the chart's Y-axis range. It adds extra padding at the top
     * of the chart to provide room for data labels and to improve
     * visual aesthetics.
     * <p>
     * If no data entity wrapper is set, this method has no effect to prevent
     * errors or inappropriate scaling.
     * 
     * @param lineChart The chart to scale
     */
    public void scale(DataEntityLineChart lineChart) {

        if (dataEntityWrapper == null)
            return;

        // Calculate data range and offset for padding
        double r = dataEntityWrapper.getMaxValue() - dataEntityWrapper.getMinValue();
        double o = r * 0.1f;
        minY = (dataEntityWrapper.getMinValue() - o);

        // Create list of values from statistics to consider in scaling
        List<Double> valYStatisticsList =
                Arrays.asList(
                        minY,
                        dataEntityWrapper != null ? dataEntityWrapper.getMinValue() : minY,
                        dataEntityWrapper != null ? dataEntityWrapper.getMaxValue() : minY
                );

        // Collect values from limit lines
        List<Double> limitLinesValues =
                limitLinesBoundaries.getLimitLineList()
                        .stream()
                        .map(limitLine -> (double) limitLine.getLimit())
                        .collect(Collectors.toList());

        // Combine all values to find overall min and max
        Vector<Double> valYList =
                new Vector<>(Arrays.asList(
                        minY,
                        dataEntityWrapper != null ? dataEntityWrapper.getMinValue() : minY,
                        dataEntityWrapper != null ? dataEntityWrapper.getMaxValue() : minY
                ));
        valYList.addAll(limitLinesValues);

        double minY = valYList.stream().min(Comparator.naturalOrder()).get();
        double maxY = valYList.stream().max(Comparator.naturalOrder()).get();

        double maxStatisticsY = valYStatisticsList.stream().max(Comparator.naturalOrder()).get();

        if (maxY > 1 && maxY > minY) {
            //lineChart.setVisibleXRangeMaximum(lineChart.getXRange());

            // Calculate range and additional offset for visual padding
            double range = maxY - minY;
            double offset = range * 0.025f;

            // Apply calculated values to chart's Y-axis
            YAxis leftAxis = lineChart.getAxisLeft();
            //leftAxis.setAxisMinimum((float) (minY + 3.0f * offset));
            leftAxis.resetAxisMinimum();
            leftAxis.setSpaceBottom(0);

            // Ensure adequate headroom above the highest value
            if (PrecisionUtil.isGreaterEqual((float) maxStatisticsY, (float) maxY, PrecisionUtil.NDIG_PREC_COMP)) {
                leftAxis.setAxisMaximum((float) (maxStatisticsY + 2.0f * offset));
            } else {
                leftAxis.setAxisMaximum((float) (maxY + 2.0f * offset));
            }
        }
    }
}

