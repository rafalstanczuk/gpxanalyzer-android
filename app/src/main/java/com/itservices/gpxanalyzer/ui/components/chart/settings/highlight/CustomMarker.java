package com.itservices.gpxanalyzer.ui.components.chart.settings.highlight;

import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE;
import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.ui.components.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.ui.components.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.raw.DataEntity;
import com.itservices.gpxanalyzer.data.cumulative.TrendType;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.databinding.CustomMarkerViewBinding;
import com.itservices.gpxanalyzer.utils.common.FormatNumberUtil;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;
import com.itservices.gpxanalyzer.utils.ui.TextViewUtil;
import com.itservices.gpxanalyzer.utils.ui.ViewUtil;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

/**
 * Custom {@link MarkerView} implementation for displaying details about a selected
 * {@link CurveEntry} on a {@link DataEntityLineChart}.
 * It inflates a custom layout (`custom_marker_view.xml`) and populates it with information
 * such as time, value, unit, trend statistics (ascent/descent details), and cumulative statistics.
 * The visibility and content of some sections (like trend/cumulative stats) depend on chart settings.
 */
@SuppressLint("ViewConstructor") // Hilt requires @Inject constructor
public class CustomMarker extends MarkerView {

    /** Layout resource for the marker view content. */
    private static final int layoutResource = R.layout.custom_marker_view;

    /** View binding instance for the custom marker layout. */
    CustomMarkerViewBinding binding;
    /** Reference to the chart settings, used to control visibility of some marker elements. */
    private LineChartSettings settings;

    /**
     * Constructor used by Hilt for dependency injection.
     * Inflates the custom layout using View Binding.
     *
     * @param context The activity context provided by Hilt.
     */
    @Inject
    public CustomMarker(@ActivityContext Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        binding = CustomMarkerViewBinding.inflate(inflater, this, true);

    }

    /**
     * Returns the offset for drawing the marker relative to the highlighted point.
     * Positions the marker slightly offset from the highlighted point.
     *
     * @return The MPPointF offset.
     */
    @Override
    public MPPointF getOffset() {
        return new MPPointF(getWidth() * 0.05f, getHeight() * 0.1f);
    }

    /**
     * Called every time the MarkerView is redrawn. Updates the content based on the highlighted Entry.
     * Extracts data from the {@link CurveEntry}, formats it, and sets the text of the various TextViews
     * within the marker layout (time, value, trend stats, cumulative stats).
     *
     * @param entry     The Entry selected on the chart.
     * @param highlight The corresponding highlight object.
     */
    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        DataEntityLineChart chartView = (DataEntityLineChart) getChartView();

        ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
                .getLastGesture();

        if (entry instanceof CurveEntry curveDataEntityEntry) {
            DataEntity dataEntity = curveDataEntityEntry.getDataEntity();

            SpannableStringBuilder timeLine = TextViewUtil.getSpannableStringBuilderWithBoldPostfix(
                    FormatNumberUtil.getFormattedTime(dataEntity.timestampMillis()), "h",
                    " ");

            String unitString = curveDataEntityEntry.getDataEntityWrapper().getUnit(dataEntity);

            SpannableStringBuilder valueLine = TextViewUtil.getSpannableStringBuilderWithBoldPostfix(
                    String.valueOf((int) curveDataEntityEntry.getY()), unitString, " ");

            binding.markerTextViewTime.setText(timeLine, TextView.BufferType.SPANNABLE);
            binding.markerTextViewValue.setText(valueLine, TextView.BufferType.SPANNABLE);

            setupTrendStatisticsLayout(curveDataEntityEntry.getTrendBoundaryDataEntity().trendStatistics(), unitString);

            setupCumulativeProcessedDataTypesLayout(dataEntity, curveDataEntityEntry.getDataEntityWrapper());
        }

        super.refreshContent(entry, highlight);
    }

    /**
     * Sets up the layout section displaying cumulative processed data statistics.
     * Visibility depends on whether ascent/descent segments are enabled in settings.
     *
     * @param dataEntity        The selected data entity.
     * @param dataEntityWrapper The wrapper for the dataset.
     */
    private void setupCumulativeProcessedDataTypesLayout(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper) {
        ViewUtil.setVisibility(binding.cumulativeProcessedDataTypesLayout, settings.isDrawAscDescSegEnabled());
        if (settings.isDrawAscDescSegEnabled()) {
            setupCumulativeProcessedDataTypes(dataEntity, dataEntityWrapper);
        }
    }

    /**
     * Populates the TextViews related to cumulative statistics (e.g., total ascent/descent, segment ascent/descent).
     *
     * @param dataEntity        The selected data entity.
     * @param dataEntityWrapper The wrapper for the dataset.
     */
    private void setupCumulativeProcessedDataTypes(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper) {
        fillTextViewWithValueUnit(dataEntity, dataEntityWrapper,
                FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                binding.fromSegmentStartSumRealDeltaCumulativeValue);

        fillTextViewWithValueUnit(dataEntity, dataEntityWrapper,
                ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                binding.allSumRealDeltaCumulativeValue);
    }

    /**
     * Helper method to fill a TextView with a formatted cumulative statistic value and its unit.
     *
     * @param dataEntity                The selected data entity.
     * @param dataEntityWrapper         The wrapper for the dataset.
     * @param cumulativeProcessedDataType The type of cumulative data to display.
     * @param textView                  The TextView to populate.
     */
    private static void fillTextViewWithValueUnit(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper, CumulativeProcessedDataType cumulativeProcessedDataType, TextView textView) {

        CumulativeStatistics cumulativeStatistics = dataEntityWrapper.getCumulativeStatistics(dataEntity, cumulativeProcessedDataType);

        float value = cumulativeStatistics.value();
        String unit = cumulativeStatistics.unit();

        String valueString =
                String.format(Locale.getDefault(), "%.2f", value);

        textView.setText(TextViewUtil.getSpannableStringBuilderWithBoldPostfix(valueString, " " + unit, " "));
    }

    /**
     * Sets up the layout section displaying trend statistics (ascent/descent).
     * Visibility depends on whether ascent/descent segments are enabled in settings.
     *
     * @param trendStatistics The trend statistics associated with the selected entry.
     * @param unitString      The unit string for the displayed value.
     */
    private void setupTrendStatisticsLayout(TrendStatistics trendStatistics, String unitString) {
        ViewUtil.setVisibility(binding.trendStatisticsLayout, settings.isDrawAscDescSegEnabled());
        if (settings.isDrawAscDescSegEnabled()) {
            setupAscDescSegStatistics(trendStatistics, unitString);
        }
    }

    private void setupAscDescSegStatistics(TrendStatistics trendStatistics, String unitString) {

        TrendType trendType = trendStatistics.trendType();
        String text = "";
        switch (trendType) {
            case UP -> {
                text = "+ ";
            }
            case CONSTANT -> {
                text = "  ";
            }
            case DOWN -> {
                text = "- ";
            }
        }
        text +=
                String.format(Locale.getDefault(), "%.2f", trendStatistics.absDeltaVal());


        binding.markerTextViewDeltaValue.setText(TextViewUtil.getSpannableStringBuilderWithBoldPostfix(text, " " + unitString, " "));
        binding.markerTextViewDeltaValue.setBackgroundColor(ColorUtil.setAlphaInIntColor(trendType.getFillColor(), 128));

        String numberString =
                String.format(Locale.getDefault(), "%d.", trendStatistics.n());
        binding.markerTextViewNumber.setText(TextViewUtil.getSpannableStringBuilderWithBoldPostfix(null, numberString, " "));

        binding.trendTypeImageView.setImageResource(trendType.getDrawableId());
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        DataEntityLineChart chartView = (DataEntityLineChart) getChartView();

        ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
                .getLastGesture();


        RectF contentRect = getChartView().getViewPortHandler().getContentRect();

        MPPointF pointFCenter = getChartView().getViewPortHandler().getContentCenter();



/*		if (chartView.isFullyZoomedOut() ) {
			//drawMarker(posX, posY, canvas);
		} else {*/

        switch (chartGesture) {
            case ROTATE:
            case DOUBLE_TAP:
            case LONG_PRESS:
            case SINGLE_TAP: {

                drawMarker(posX, (contentRect.top + contentRect.bottom) * 0.25f, canvas);

                break;
            }
            case NONE:
            case X_ZOOM:
            case Y_ZOOM:
            case PINCH_ZOOM:
            case FLING:
            case DRAG:
                drawMarker(posX, (contentRect.top + contentRect.bottom) * 0.25f, canvas);
                break;
        }
        //}
    }

    private void drawMarker(float posX, float posY, Canvas canvas) {
        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);

        canvas.restoreToCount(saveId);
    }

    public void setSettings(final LineChartSettings settings) {
        this.settings = settings;
    }
}