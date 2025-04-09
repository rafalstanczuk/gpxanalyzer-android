package com.itservices.gpxanalyzer.chart.settings;

import static com.github.mikephil.charting.charts.Chart.PAINT_GRID_BACKGROUND;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.settings.axis.AxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.chart.settings.highlight.CustomMarker;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manages and applies configuration settings for GPX data line charts.
 * <p>
 * This class encapsulates all the settings that can be applied to a {@link DataEntityLineChart},
 * including axis configuration, grid appearance, interactions (dragging, zooming, etc.),
 * and visual display options. It provides methods to customize chart behavior and appearance
 * based on application requirements.
 * <p>
 * The settings can be applied to any {@link DataEntityLineChart} instance and can be dynamically
 * modified to update chart appearance during runtime.
 * <p>
 * Key features include:
 * - Axis configuration (labels, granularity, formatters)
 * - Touch and gesture handling (dragging, zooming, pinch)
 * - Visual styling (colors, limit lines, grid)
 * - Interactive elements (markers, highlight behavior)
 */
public class LineChartSettings {
    /**
     * Custom marker view for displaying details when highlighting chart points
     */
    private final CustomMarker customMarker;

    /**
     * Formatter for converting X-axis values to hour:minute format
     */
    private final HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter;

    /**
     * Formatter for Y-axis values based on data entities
     */
    private final AxisValueFormatter dataEntityAxisValueFormatter;

    /**
     * Paint used for the chart background
     */
    private final Paint paintGridBg = new Paint();

    /**
     * Primary color used for various chart elements
     */
    private final int primaryColor;

    /**
     * Manages limit lines (horizontal reference lines) displayed on the chart
     */
    private LimitLinesBoundaries limitLinesBoundaries;

    /**
     * Flag controlling whether X-axis labels are displayed
     */
    private boolean drawXLabels = true;

    /**
     * Flag controlling chart deceleration behavior after drag gestures
     */
    private boolean dragDecelerationEnabled = false;

    /**
     * Flag controlling whether icons are displayed on data points
     */
    private boolean drawIconsEnabled = false;

    /**
     * Flag controlling whether ascent/descent segment filling is enabled
     */
    private boolean drawAscDescSegEnabled = false;

    /**
     * Weak reference to the chart this settings object is applied to
     */
    private WeakReference<DataEntityLineChart> lineChartWeakRef;

    /**
     * Creates a new LineChartSettings with default configuration.
     *
     * @param context                       Application context used to retrieve resources
     * @param customMarker                  The marker view to display when points are highlighted
     * @param hourMinutesAxisValueFormatter Formatter for the X-axis time values
     * @param dataEntityAxisValueFormatter  Formatter for the Y-axis data values
     */
    @Inject
    LineChartSettings(
            @ApplicationContext Context context, CustomMarker customMarker,
            HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter,
            AxisValueFormatter dataEntityAxisValueFormatter
    ) {
        primaryColor = ContextCompat.getColor(context, R.color.lineChartPrimary);
        paintGridBg.setStyle(Paint.Style.FILL);
        paintGridBg.setColor(ContextCompat.getColor(context, R.color.lineChartBackground));

        this.customMarker = customMarker;
        this.hourMinutesAxisValueFormatter = hourMinutesAxisValueFormatter;
        this.dataEntityAxisValueFormatter = dataEntityAxisValueFormatter;
    }

    public static void updateLineDataSetWithSettings(LineDataSet lineDataSet, LineChartSettings settings) {
        lineDataSet.setDrawFilled(settings.isDrawAscDescSegEnabled());
        lineDataSet.setDrawIcons(settings.isDrawIconsEnabled());

        if (settings.isDrawAscDescSegEnabled()) {
            lineDataSet.setHighLightColor(Color.BLACK);
            lineDataSet.enableDashedHighlightLine(30f, 5f, 0f);
            lineDataSet.setHighlightLineWidth(1f);
        } else {
            lineDataSet.setHighLightColor(Color.BLACK);
            lineDataSet.disableDashedHighlightLine();
            lineDataSet.setHighlightLineWidth(1f);
        }
    }

    /**
     * Checks if icon display is enabled for data points.
     * Icons provide visual indicators at data points on the chart.
     *
     * @return True if icons should be displayed on data points, false otherwise
     */
    public boolean isDrawIconsEnabled() {
        return drawIconsEnabled;
    }

    /**
     * Sets whether icons should be displayed on data points.
     * Icons can enhance the visualization by marking specific points on the chart.
     *
     * @param drawIconsEnabled True to enable icon display, false to disable
     */
    public void setDrawIconsEnabled(boolean drawIconsEnabled) {
        this.drawIconsEnabled = drawIconsEnabled;
    }

    /**
     * Sets the limit lines boundaries for the chart.
     * These boundaries determine the horizontal reference lines displayed on the chart,
     * which can help visualize thresholds or ranges of interest.
     *
     * @param limitLinesBoundaries The limit lines boundaries to use
     */
    public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
        this.limitLinesBoundaries = limitLinesBoundaries;
        dataEntityAxisValueFormatter.setLimitLinesBoundaries(limitLinesBoundaries);
    }

    /**
     * Sets whether X-axis labels should be displayed.
     * X-axis labels typically represent time values in the GPX data.
     *
     * @param drawXLabels True to display X-axis labels, false to hide them
     */
    public void setDrawXLabels(boolean drawXLabels) {
        this.drawXLabels = drawXLabels;
    }

    /**
     * Sets whether drag deceleration is enabled for the chart.
     * When enabled, the chart will continue to scroll with decreasing speed after a drag gesture ends,
     * providing a more fluid user experience.
     *
     * @param dragDecelerationEnabled True to enable drag deceleration, false to disable
     */
    public void setDragDecelerationEnabled(boolean dragDecelerationEnabled) {
        this.dragDecelerationEnabled = dragDecelerationEnabled;
    }

    /**
     * Applies all settings to the specified line chart.
     * This method configures all aspects of the chart according to the current settings state,
     * including axes, touch handling, visual appearance, and markers.
     *
     * @param lineChart The chart to apply settings to
     */
    public void setChartSettingsFor(DataEntityLineChart lineChart) {

        this.lineChartWeakRef = new WeakReference<>(lineChart);

        lineChart.setDragDecelerationEnabled(dragDecelerationEnabled);
        lineChart.setPaint(paintGridBg, PAINT_GRID_BACKGROUND);
        lineChart.setAutoScaleMinMaxEnabled(false);
        lineChart.setDrawGridBackground(true);

        lineChart.setNoDataText("Load data to show here.");
        lineChart.setNoDataTextColor(Color.RED);

        customMarker.setChartView(lineChart);
        customMarker.setSettings(this);
        lineChart.setMarker(customMarker);
        lineChart.setDrawBorders(false);
        lineChart.setMaxHighlightDistance(10000.0f);

        lineChart.setHideLastHighlightedIfAgainSelected(false); //!!!!

        lineChart.resetZoom();
        lineChart.setMaxVisibleValueCount(20000);
        lineChart.setTouchEnabled(true);

        lineChart.setDragYEnabled(false);
        lineChart.setScaleYEnabled(false);

        lineChart.setDragXEnabled(true);
        lineChart.setScaleXEnabled(true);

        lineChart.setPinchZoom(false);

        setupYAxisRight(lineChart);
        setupYAxisLeft(lineChart);
        setupXAxis(lineChart);
        setupDescriptions(lineChart);
    }

    /**
     * Configures the X-axis of the chart.
     * Sets up position, granularity, formatting, and appearance of the X-axis,
     * which typically represents time values in GPX data.
     *
     * @param lineChart The chart whose X-axis should be configured
     */
    private void setupXAxis(DataEntityLineChart lineChart) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(drawXLabels);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(HourMinutesAxisValueFormatter.GRANULARITY);
        xAxis.setLabelCount(HourMinutesAxisValueFormatter.LABEL_COUNT, false);
        xAxis.setValueFormatter(hourMinutesAxisValueFormatter);
        xAxis.setLabelRotationAngle(HourMinutesAxisValueFormatter.LABEL_ROTATION_ANGLE);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10f);
    }

    /**
     * Configures the chart descriptions and legend.
     * Currently disables both description and legend to focus on the chart data,
     * though commented code shows how a description could be added if needed.
     *
     * @param lineChart The chart whose descriptions should be configured
     */
    private void setupDescriptions(DataEntityLineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
/*
		lineChart.getDescription().setEnabled(true);

		Description description = new Description();
		description.setText(descriptionString);
		description.setTextColor(Color.BLACK);
		description.setTextSize(12);
		description.setEnabled(true);
		description.setTextAlign(Paint.Align.LEFT);
		description.setPosition(10, 20);

		lineChart.setDescription(description);*/
    }

    /**
     * Configures the right Y-axis of the chart.
     * Currently disables the right Y-axis completely, as the application
     * only uses the left Y-axis for data visualization.
     *
     * @param lineChart The chart whose right Y-axis should be configured
     */
    private void setupYAxisRight(DataEntityLineChart lineChart) {
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
    }

    /**
     * Configures the left Y-axis of the chart.
     * Sets up formatting, appearance, and limit lines for the left Y-axis,
     * which typically represents measurement values (elevation, speed, etc.) in GPX data.
     *
     * @param lineChart The chart whose left Y-axis should be configured
     */
    private void setupYAxisLeft(DataEntityLineChart lineChart) {
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setDrawGridLines(false);
//		yAxisLeft.setGridDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
//		yAxisLeft.setAxisLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
        yAxisLeft.setGranularityEnabled(false);
        yAxisLeft.setValueFormatter(dataEntityAxisValueFormatter);
        yAxisLeft.setEnabled(true);
        yAxisLeft.setTextColor(primaryColor);
        yAxisLeft.resetAxisMinimum();
        yAxisLeft.setSpaceBottom(0);

        yAxisLeft.removeAllLimitLines();

        if (yAxisLeft.getLimitLines().isEmpty()) {
            limitLinesBoundaries.addLimitLinesInto(yAxisLeft);
        }
    }

    /**
     * Checks if ascent/descent segment filling is enabled.
     * When enabled, the area between the line and the axis is filled with color,
     * making it easier to visualize elevation changes or other metrics.
     *
     * @return True if ascent/descent segment filling is enabled, false otherwise
     */
    public boolean isDrawAscDescSegEnabled() {
        return drawAscDescSegEnabled;
    }

    /**
     * Sets whether ascent/descent segment filling is enabled.
     * When enabled, areas between the line and the axis will be filled with color,
     * enhancing the visual representation of elevation changes or similar metrics.
     *
     * @param drawAscDescSegEnabled True to enable segment filling, false to disable
     */
    public void setDrawAscDescSegEnabled(boolean drawAscDescSegEnabled) {
        this.drawAscDescSegEnabled = drawAscDescSegEnabled;
    }

    /**
     * Updates the settings for the specified line data.
     * Applies current visual settings to all datasets in the line data,
     * ensuring consistent appearance across the chart.
     *
     * @param lineData The line data to update settings for
     */
    public void updateSettingsFor(LineData lineData) {
        List<ILineDataSet> lineDataSetList = lineData.getDataSets();
        lineDataSetList.forEach(
                iLineDataSet -> {
                    LineDataSet lineDataSet = (LineDataSet) iLineDataSet;
                    updateLineDataSetWithSettings(lineDataSet, LineChartSettings.this);
                }
        );
    }

    public ChartSlot getChartSlot() {
        if (lineChartWeakRef == null || lineChartWeakRef.get() == null) {
            return null;
        }

        return lineChartWeakRef.get().getChartSlot();
    }
}
