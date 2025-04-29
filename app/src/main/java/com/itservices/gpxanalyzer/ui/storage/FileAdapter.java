package com.itservices.gpxanalyzer.ui.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.databinding.ItemFileBinding;
import com.itservices.gpxanalyzer.ui.utils.StringUtils;
import com.itservices.gpxanalyzer.utils.ui.CoordinateFormatter;
import com.itservices.gpxanalyzer.utils.ui.TextViewUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} for displaying a list of GPX files ({@link FileInfoItem}) with their metadata.
 * Each item shows file details, GPX metadata (creator, author, first point), and a map miniature preview.
 * Handles item selection and notifies a listener when a file is selected.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private static final String TAG = FileAdapter.class.getSimpleName();
    private final FileSelectorViewModel viewModel;
    private final LifecycleOwner viewLifecycleOwner;

    /** The list of file information items currently displayed by the adapter. */
    private List<FileInfoItem> fileInfoItemList = new ArrayList<>();

    /**
     * Constructs a new {@code FileAdapter}.
     */
    public FileAdapter(FileSelectorViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
        this.viewModel = viewModel;
        this.viewLifecycleOwner = viewLifecycleOwner;
    }

    /**
     * Configures the TextViews within the ViewHolder to display GPX file information.
     *
     * @param holder      The {@link FileViewHolder} containing the TextViews.
     * @param gpxFileInfo The {@link GpxFileInfo} data object.
     * @param context     The application context for resolving string resources.
     */
    private void configureTextViews(@NonNull FileViewHolder holder, GpxFileInfo gpxFileInfo, Context context) {
        //Log.d(FileAdapter.class.getSimpleName(), "configureTextViews() called with: holder = [" + holder + "], gpxFileInfo = [" + gpxFileInfo + "], context = [" + context + "]");

        String fileName = gpxFileInfo.file().getName();
        holder.binding.fileName.setText(fileName);

        configureFirstPointContainer(holder, gpxFileInfo, context);

        holder.binding.creatorInfo.setText(
                TextViewUtil.getSpannableStringBuilderWithBoldPrefix(
                        context.getString(R.string.creator_prefix), 
                        gpxFileInfo.creator(),
                        "\t"
                ), TextView.BufferType.SPANNABLE
        );
        holder.binding.authorInfo.setText(
                TextViewUtil.getSpannableStringBuilderWithBoldPrefix(
                        context.getString(R.string.author_prefix),
                        gpxFileInfo.authorName(),
                        "\t"
                ), TextView.BufferType.SPANNABLE
        );

        // Format file size in kB at the bottom
        long fileSizeInKB = gpxFileInfo.fileSize() / 1024;
        holder.binding.fileSize.setText(String.format(Locale.getDefault(),
                context.getString(R.string.file_size_format_string_d_kb), fileSizeInKB));
    }

    private void configureFirstPointContainer(FileViewHolder holder, GpxFileInfo gpxFileInfo, Context context) {
        ItemFileBinding binding = holder.binding;

        Location location = gpxFileInfo.firstPointLocation();
        if (location != null) {
            String dateStr = StringUtils.getFormattedDateMillisDate(location.getTime());
            String timeStr = StringUtils.getFormattedTimeMillisDate(location.getTime());
            SpannableStringBuilder dateTime
                    = TextViewUtil.getSpannableStringBuilderWithBoldPrefix(dateStr, timeStr, " ");

            SpannableStringBuilder latDms = CoordinateFormatter.formatLatitudeDMS(
                    String.valueOf(location.getLatitude()), context);
            SpannableStringBuilder lonDms = CoordinateFormatter.formatLongitudeDMS(
                    String.valueOf(location.getLongitude()), context);
            SpannableStringBuilder elevation
                    = TextViewUtil.getSpannableStringBuilderWithBoldPostfix(
                            String.valueOf(location.getAltitude()), context.getString(R.string.elevation_unit), " ");

            binding.trackPointInfoGeocodedTextview.setText(gpxFileInfo.geoCodedLocation());
            binding.trackPointInfoDatetimeTextview.setText(dateTime, TextView.BufferType.SPANNABLE);
            binding.trackPointInfoLatitudeTextview.setText(latDms, TextView.BufferType.SPANNABLE);
            binding.trackPointInfoLongitudeTextview.setText(lonDms, TextView.BufferType.SPANNABLE);
            binding.trackPointInfoAltitudeTextview.setText(elevation, TextView.BufferType.SPANNABLE);
        } else {
            // Clear the text views if no location data is available
            binding.trackPointInfoGeocodedTextview.setText("");
            binding.trackPointInfoDatetimeTextview.setText("");
            binding.trackPointInfoLatitudeTextview.setText("");
            binding.trackPointInfoLongitudeTextview.setText("");
            binding.trackPointInfoAltitudeTextview.setText("");
        }
    }

    /**
     * Updates the list of files displayed by the adapter.
     * Sorts the incoming list by the timestamp of the first point in the GPX file.
     * Notifies the adapter that the data set has changed.
     *
     * @param fileInfoItemList The new list of {@link FileInfoItem}s to display.
     */
    @SuppressLint("NotifyDataSetChanged") // Intentional full refresh after sorting/setting new list
    public void setFiles(@NonNull List<FileInfoItem> fileInfoItemList) {
        fileInfoItemList.sort(Comparator.comparingLong(item -> item.fileInfo().firstPointLocation().getTime()));

        this.fileInfoItemList = fileInfoItemList;

        notifyDataSetChanged();
    }

    /**
     * Creates a new {@link FileViewHolder} by inflating the item layout (`item_file.xml`)
     * using {@link ItemFileBinding}.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new FileViewHolder.
     */
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], viewType = [" + viewType + "]");

        ItemFileBinding binding = ItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FileViewHolder(binding);
    }

    /**
     * Binds data from a {@link FileInfoItem} at a specific position to the given {@link FileViewHolder}.
     * Delegates the actual binding logic to the private {@link #bind(FileViewHolder, int, FileInfoItem, Context)} method.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");

        Context context = holder.binding.getRoot().getContext();

        FileInfoItem fileInfoItem = fileInfoItemList.get(position);
        holder.bind(fileInfoItem, viewModel, viewLifecycleOwner);

        bind(holder, position, fileInfoItem, context);
    }

    /**
     * Performs the detailed binding of a {@link FileInfoItem} to a {@link FileViewHolder}.
     * Configures text views, sets the map miniature image (or placeholder), updates the selection state (checked, stroke width),
     * and sets the click listener to handle item selection.
     *
     * @param holder       The ViewHolder to bind data to.
     * @param position     The adapter position of the item.
     * @param fileInfoItem The {@link FileInfoItem} data.
     * @param context      The application context.
     */
    private void bind(@NonNull FileViewHolder holder, int position, FileInfoItem fileInfoItem, Context context) {
        configureTextViews(holder, fileInfoItem.fileInfo(), context);

        ImageView imageView = holder.binding.mapMiniatureImageView;
        if (fileInfoItem.fileInfo().miniatureBitmap().get() != null) {
            imageView.setImageBitmap(fileInfoItem.fileInfo().miniatureBitmap().get());
        } else {
            imageView.setImageResource(R.drawable.ic_menu_mapmode);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items.
     */
    @Override
    public int getItemCount() {
        return (fileInfoItemList != null) ? fileInfoItemList.size() : 0;
    }

    /**
     * Interface definition for a callback to be invoked when a file is selected in the adapter.
     */
    public interface OnFileSelectedListener {
        /**
         * Called when a file item has been selected by the user.
         *
         * @param file The {@link File} object corresponding to the selected item.
         */
        void onFileSelected(File file);
    }

    /**
     * ViewHolder class for displaying individual GPX file items.
     * Holds the {@link ItemFileBinding} instance for the layout.
     */
    public static class FileViewHolder extends RecyclerView.ViewHolder {
        /** Data binding instance for the item layout (item_file.xml). */
        final ItemFileBinding binding;

        /**
         * Constructs a new {@code FileViewHolder}.
         *
         * @param binding The data binding instance for the item layout.
         */
        FileViewHolder(ItemFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FileInfoItem fileInfoItem, FileSelectorViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
            binding.setLifecycleOwner(viewLifecycleOwner);
            binding.setViewModel(viewModel);
            binding.setItem(fileInfoItem);
        }
    }
}
