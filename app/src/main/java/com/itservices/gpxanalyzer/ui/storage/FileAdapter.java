package com.itservices.gpxanalyzer.ui.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
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
    /** Listener to be notified when a file item is selected by the user. */
    private final OnFileSelectedListener fileSelectedListener;
    /** The list of file information items currently displayed by the adapter. */
    private List<FileInfoItem> fileInfoItemList = new ArrayList<>();
    /** The currently selected file item. */
    private FileInfoItem selectedFileInfoItem;

    /**
     * Constructs a new {@code FileAdapter}.
     *
     * @param listener The listener to be notified upon file selection.
     */
    public FileAdapter(OnFileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    /**
     * Configures the TextViews within the ViewHolder to display GPX file information.
     *
     * @param holder      The {@link FileViewHolder} containing the TextViews.
     * @param gpxFileInfo The {@link GpxFileInfo} data object.
     * @param context     The application context for resolving string resources.
     */
    private static void configureTextViews(@NonNull FileViewHolder holder, GpxFileInfo gpxFileInfo, Context context) {
        Log.d(FileAdapter.class.getSimpleName(), "configureTextViews() called with: holder = [" + holder + "], gpxFileInfo = [" + gpxFileInfo + "], context = [" + context + "]");

        // Set filename - most prominent info at the top
        String fileName = gpxFileInfo.file().getName();
        holder.binding.fileName.setText(fileName);

        // Format and set the trackPointInfo with styled text (date and coordinates)
        holder.binding.trackPointInfo.setText(formatTrackPointInfo(gpxFileInfo, context));

        // Display creator and author info (secondary metadata)
        holder.binding.creatorInfo.setText(
                TextViewUtil.getSpannableStringBuilderWithBoldPrefix(
                        context.getString(R.string.creator_prefix), 
                        gpxFileInfo.creator()
                )
        );
        holder.binding.authorInfo.setText(
                TextViewUtil.getSpannableStringBuilderWithBoldPrefix(
                        context.getString(R.string.author_prefix),
                        gpxFileInfo.authorName()
                )
        );

        // Format file size in kB at the bottom
        long fileSizeInKB = gpxFileInfo.fileSize() / 1024;
        holder.binding.fileSize.setText(String.format(Locale.getDefault(),
                context.getString(R.string.file_size_format_string_d_kb), fileSizeInKB));

        // firstPointDateTime is now redundant as we include it in trackPointInfo
        // but we'll set it anyway in case visibility is changed later
        holder.binding.firstPointDateTime.setText(
                StringUtils.getFormattedTimeMillisDate(
                        gpxFileInfo.firstPointTimeMillis()
                )
        );
    }

    /**
     * Formats track point information with styling according to requirements.
     * Displays date/time, coordinates, and elevation in a structured format.
     *
     * @param gpxFileInfo The GPX file information to format
     * @param context Context for accessing resources
     * @return SpannableStringBuilder containing the formatted text
     */
    @NonNull
    private static SpannableStringBuilder formatTrackPointInfo(GpxFileInfo gpxFileInfo, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        // Format date/time in italic and add it first with larger font
        String dateTimeStr = StringUtils.getFormattedTimeMillisDate(gpxFileInfo.firstPointTimeMillis());
        SpannableString dateTime = new SpannableString(dateTimeStr);
        dateTime.setSpan(new StyleSpan(Typeface.NORMAL), 0, dateTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(dateTime);
        builder.append("\n");
        
        // Create the "Start: " prefix in bold
        SpannableStringBuilder startPrefix = TextViewUtil.getSpannableStringBuilderWithBoldPrefix(
                context.getString(R.string.location_start) + " ", ""
        );
        builder.append(startPrefix);

        builder.append("\n\t\t");
        // Format latitude in DMS and append it
        SpannableStringBuilder latDms = CoordinateFormatter.formatLatitudeDMS(gpxFileInfo.firstPointLat(), context);
        builder.append(latDms);
        
        // Add comma separator
        builder.append(context.getString(R.string.coordinates_separator));
        builder.append("\t\t");
        
        // Format longitude in DMS and append it
        SpannableStringBuilder lonDms = CoordinateFormatter.formatLongitudeDMS(gpxFileInfo.firstPointLon(), context);
        builder.append(lonDms);
        
        // Add elevation with bold "m asl." unit
        builder.append(" (");
        builder.append(gpxFileInfo.firstPointEle());
        
        SpannableString mUnit = new SpannableString(context.getString(R.string.elevation_unit));
        mUnit.setSpan(new StyleSpan(Typeface.BOLD), 0, mUnit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(mUnit);
        builder.append(")");
        
        return builder;
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
        fileInfoItemList.sort(Comparator.comparingLong(item -> item.fileInfo().firstPointTimeMillis()));

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
        Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], viewType = [" + viewType + "]");

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
        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");

        Context context = holder.binding.getRoot().getContext();

        FileInfoItem fileInfoItem = fileInfoItemList.get(position);
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

        // Directly set the bitmap if available
        ImageView imageView = holder.binding.mapMiniatureImageView;
        if (fileInfoItem.fileInfo().miniatureBitmap().get() != null) {
            imageView.setImageBitmap(fileInfoItem.fileInfo().miniatureBitmap().get());
        } else {
            // Set placeholder if no bitmap (should have been generated beforehand now)
            imageView.setImageResource(R.drawable.ic_menu_mapmode);
        }

        // Update card state based on selection
        holder.binding.getRoot().setChecked(fileInfoItem.equals(selectedFileInfoItem));
        holder.binding.getRoot().setStrokeWidth(fileInfoItem.equals(selectedFileInfoItem) ? 2 : 1);

        holder.binding.getRoot().setOnClickListener(v -> {
            FileInfoItem previouslySelectedItem = selectedFileInfoItem;
            int previouslySelectedIndex = -1;
            if (previouslySelectedItem != null) {
                 previouslySelectedIndex = fileInfoItemList.indexOf(previouslySelectedItem);
            }

            selectedFileInfoItem = fileInfoItem;
            notifyItemChanged(position); // Notify current item changed
            if (previouslySelectedIndex != -1) {
                 notifyItemChanged(previouslySelectedIndex); // Notify previous item changed
            }

            if (fileSelectedListener != null) {
                fileSelectedListener.onFileSelected(fileInfoItem.fileInfo().file());
            }
        });
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
    }
}
