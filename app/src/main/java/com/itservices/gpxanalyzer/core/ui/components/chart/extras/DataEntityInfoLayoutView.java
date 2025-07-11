package com.itservices.gpxanalyzer.core.ui.components.chart.extras;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.itservices.gpxanalyzer.databinding.DataEntityChartInfoViewBinding;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A custom {@link LinearLayout} designed to display detailed information about a selected data point
 * on a chart, such as time, primary value, and its unit.
 * It likely acts as a marker view or info window associated with chart selections.
 * Uses View Binding (`DataEntityChartInfoViewBinding`) to access its child views.
 * Note: Parts of the inflation logic appear commented out in the source.
 */
public class DataEntityInfoLayoutView extends LinearLayout {

	/** View binding instance for the layout (data_entity_chart_info_view.xml). */
	DataEntityChartInfoViewBinding binding;

	/**
	 * Constructor used by Hilt for dependency injection.
	 *
	 * @param context The application context.
	 */
	@Inject
	public DataEntityInfoLayoutView(@ApplicationContext Context context) {
		super(context);
		inflateView(context);
	}

	/**
	 * Constructor called when inflating from XML.
	 *
	 * @param context The context the view is running in.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 */
	public DataEntityInfoLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflateView(context);
	}

	/**
	 * Constructor called when inflating from XML with a default style attribute.
	 *
	 * @param context      The context the view is running in.
	 * @param attrs        The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
	 */
	public DataEntityInfoLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflateView(context);
	}

	/**
	 * Constructor called when inflating from XML with default style attribute and resource.
	 *
	 * @param context      The context the view is running in.
	 * @param attrs        The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
	 * @param defStyleRes  A resource identifier of a style resource that supplies default values.
	 */
	public DataEntityInfoLayoutView(
		Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
	) {
		super(context, attrs, defStyleAttr, defStyleRes);
		inflateView(context);
	}

	/**
	 * Inflates the layout associated with this custom view using View Binding.
	 * (Note: Original manual inflation code is commented out).
	 *
	 * @param context The application context.
	 */
	private void inflateView(Context context) {
/*		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.data_entity_chart_info_view, this, true);*/

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		binding = DataEntityChartInfoViewBinding.inflate(inflater, this, true);
	}

	/**
	 * Sets the time text displayed in the info view.
	 *
	 * @param time The formatted time string.
	 */
	public void setTime(String time) {
		binding.textViewTime.setText(time);
	}

	/**
	 * Sets the primary value text displayed in the info view.
	 *
	 * @param value The formatted value string.
	 */
	public void setValue1(String value) {
		binding.textViewValue1.setText(value);
	}

	/**
	 * Sets the unit text for the primary value displayed in the info view.
	 *
	 * @param valueUnit The unit string (e.g., "m", "km/h").
	 */
	public void setValue1Unit(String valueUnit) {
		binding.textViewValue1Unit.setText(valueUnit);
	}


}