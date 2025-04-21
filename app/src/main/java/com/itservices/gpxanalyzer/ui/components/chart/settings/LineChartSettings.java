package com.itservices.gpxanalyzer.ui.components.chart.settings;

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
import com.itservices.gpxanalyzer.ui.components.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.ui.components.chart.settings.axis.AxisValueFormatter;
import com.itservices.gpxanalyzer.ui.components.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.ui.components.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.ui.components.chart.settings.highlight.CustomMarker;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manages and applies configuration settings for {@link DataEntityLineChart} instances.
 * This class centralizes the configuration for axes, grid, interactions, markers,
 * highlighting, and visual styles (like drawing icons or filled segments).
 */
public class LineChartSettings {
    /** Custom marker view displayed when a value is selected. Injected by Hilt. */
    private final CustomMarker customMarker;

    /** Formatter for X-axis values (time). Injected by Hilt. */
    private final HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter;

    /** Formatter for Y-axis values (data entity values like altitude, speed). Injected by Hilt. */
    private final AxisValueFormatter dataEntityAxisValueFormatter;

    /** Paint object used for drawing the chart grid background. */
    private final Paint paintGridBg = new Paint();

    /** Primary color resource used for styling (e.g., axis labels). */
    private final int primaryColor;

    /** Manages the horizontal limit lines displayed on the chart. */
    private LimitLinesBoundaries limitLinesBoundaries;

    // --- Configurable Flags --- //
    /** Flag controlling whether X-axis labels are displayed. */
    private boolean drawXLabels = true;
    /** Flag controlling chart deceleration behavior after drag gestures. */
    private boolean dragDecelerationEnabled = false;
    /** Flag controlling whether icons are displayed on data points. */
    private boolean drawIconsEnabled = false;
    /** Flag controlling whether ascent/descent segment filling is enabled. */
    private boolean drawAscDescSegEnabled = false;

    /** Weak reference to the chart this settings object is currently applied to. */
    private WeakReference<DataEntityLineChart> lineChartWeakRef;

    /**
     * Creates a new LineChartSettings instance.
     * Initializes components using Dagger/Hilt dependency injection.
     *
     * @param context                       Application context for resource access.
     * @param customMarker                  The marker view for highlighted points.
     * @param hourMinutesAxisValueFormatter Formatter for X-axis (time).
     * @param dataEntityAxisValueFormatter  Formatter for Y-axis (data value).
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

    /**
     * Static helper method to apply relevant settings directly to a {@link LineDataSet}.
     * Configures drawing of filled areas and icons based on the settings.
     * Also configures the appearance of the highlight line.
     *
     * @param lineDataSet The dataset to configure.
     * @param settings    The {@link LineChartSettings} containing the configuration.
     */
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
     * Checks if drawing icons on data points is enabled.
     *
     * @return {@code true} if icons are enabled, {@code false} otherwise.
     */
    public boolean isDrawIconsEnabled() {
        return drawIconsEnabled;
    }

    /**
     * Sets whether icons should be displayed on data points.
     *
     * @param drawIconsEnabled {@code true} to enable icon display, {@code false} to disable.
     */
    public void setDrawIconsEnabled(boolean drawIconsEnabled) {
        this.drawIconsEnabled = drawIconsEnabled;
    }

    /**
     * Sets the {@link LimitLinesBoundaries} manager for this chart.
     * Links the Y-axis formatter to the boundaries.
     *
     * @param limitLinesBoundaries The limit lines boundaries manager.
     */
    public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
        this.limitLinesBoundaries = limitLinesBoundaries;
        dataEntityAxisValueFormatter.setLimitLinesBoundaries(limitLinesBoundaries);
    }

    /**
     * Sets whether X-axis labels should be displayed.
     *
     * @param drawXLabels {@code true} to display X-axis labels, {@code false} to hide them.
     */
    public void setDrawXLabels(boolean drawXLabels) {
        this.drawXLabels = drawXLabels;
    }

    /**
     * Sets whether drag deceleration is enabled for the chart.
     *
     * @param dragDecelerationEnabled {@code true} to enable drag deceleration, {@code false} to disable.
     */
    public void setDragDecelerationEnabled(boolean dragDecelerationEnabled) {
        this.dragDecelerationEnabled = dragDecelerationEnabled;
    }

    /**
     * Applies all configured settings to the specified {@link DataEntityLineChart}.
     * This method configures axes, touch behavior, background, borders, markers, highlighting,
     * and other visual and interactive aspects of the chart.
     *
     * @param lineChart The {@link DataEntityLineChart} instance to apply settings to.
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
     * Configures the appearance and behavior of the chart's X-axis (bottom axis).
     * Sets position, granularity, label formatting (using {@link HourMinutesAxisValueFormatter}),
     * color, and whether labels/gridlines are drawn.
     *
     * @param lineChart The chart whose X-axis needs configuration.
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
     * Configures the chart's description label (typically shown in the bottom-right corner).
     * Disables the description in this implementation.
     *
     * @param lineChart The chart whose description needs configuration.
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
     * Configures the appearance and behavior of the chart's right Y-axis.
     * Disables the right Y-axis in this implementation.
     *
     * @param lineChart The chart whose right Y-axis needs configuration.
     */
    private void setupYAxisRight(DataEntityLineChart lineChart) {
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
    }

    /**
     * Configures the appearance and behavior of the chart's left Y-axis.
     * Sets label formatting (using {@link AxisValueFormatter}), color, position,
     * label count, and enables/disables the zero line and grid lines.
     * Applies limit lines configured via {@link LimitLinesBoundaries}.
     *
     * @param lineChart The chart whose left Y-axis needs configuration.
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
     * Checks if drawing filled ascent/descent segments is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isDrawAscDescSegEnabled() {
        return drawAscDescSegEnabled;
    }

    /**
     * Sets whether drawing filled ascent/descent segments is enabled.
     *
     * @param drawAscDescSegEnabled {@code true} to enable, {@code false} to disable.
     */
    public void setDrawAscDescSegEnabled(boolean drawAscDescSegEnabled) {
        this.drawAscDescSegEnabled = drawAscDescSegEnabled;
    }

    /**
     * Updates settings that might depend on the currently loaded data.
     * Specifically, updates the Y-axis formatter ({@link AxisValueFormatter}) with the new LineData.
     *
     * @param lineData The {@link LineData} currently loaded in the chart.
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

    /**
     * Gets the {@link ChartSlot} associated with the currently configured chart.
     *
     * @return The {@link ChartSlot}, or null if no chart is currently referenced.
     */
    public ChartSlot getChartSlot() {
        if (lineChartWeakRef == null || lineChartWeakRef.get() == null) {
            return null;
        }

        return lineChartWeakRef.get().getChartSlot();
    }
}
