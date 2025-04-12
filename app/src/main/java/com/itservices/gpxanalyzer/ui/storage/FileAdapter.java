package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.databinding.ItemFileBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<GpxFileInfo> fileInfoList;
    private GpxFileInfo selectedFile;
    private final OnFileSelectedListener fileSelectedListener;

    private final SimpleDateFormat itemDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    public FileAdapter(OnFileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    public void setFiles(@NonNull List<GpxFileInfo> files) {
        files.sort(Comparator.comparingLong(GpxFileInfo::firstPointTimeMillis));
        this.fileInfoList = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileBinding binding = ItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Context context = holder.binding.getRoot().getContext();
        
        GpxFileInfo fileInfo = fileInfoList.get(position);
        holder.binding.fileName.setText(fileInfo.file().getName());
        
        // Format file size in kB
        long fileSizeInKB = fileInfo.fileSize() / 1024;
        holder.binding.fileSize.setText(String.format(Locale.getDefault(),
                context.getString(R.string.file_size_format_string_d_kb), fileSizeInKB));

        // Format last modified date
        String formattedDate = "N/A";
        long firstPointTimeMillis = fileInfo.firstPointTimeMillis();
        if (firstPointTimeMillis > 0) {
            try {
                Date date = new Date(firstPointTimeMillis);
                formattedDate = itemDateFormat.format(date);

                Log.d("onBindViewHolder", "onBindViewHolder() firstPointTimeMillis = [" + firstPointTimeMillis + "], formattedDate = [" + formattedDate + "]");

            } catch (Exception ignored) {
            }
        }
        holder.binding.fileCreated.setText(formattedDate);

        // Display GPX information
        holder.binding.creatorInfo.setText(String.format(Locale.getDefault(),
                context.getString(R.string.creator_format_string),
                fileInfo.creator()));
        holder.binding.authorInfo.setText(String.format(Locale.getDefault(),
                context.getString(R.string.author_format_string),
                fileInfo.authorName()));
        holder.binding.trackPointInfo.setText(String.format(Locale.getDefault(),
                context.getString(R.string.first_point_lat_s_lon_s_ele_s),
                fileInfo.firstPointLat(),
                fileInfo.firstPointLon(),
                fileInfo.firstPointEle()));

        // Update card state based on selection
        holder.binding.getRoot().setChecked(fileInfo.equals(selectedFile));
        holder.binding.getRoot().setStrokeWidth(fileInfo.equals(selectedFile) ? 2 : 1);

        holder.binding.getRoot().setOnClickListener(v -> {
            selectedFile = fileInfo;
            notifyDataSetChanged(); // Refresh UI to highlight selection
            if (fileSelectedListener != null) {
                fileSelectedListener.onFileSelected(fileInfo.file());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (fileInfoList != null) ? fileInfoList.size() : 0;
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        final ItemFileBinding binding;

        FileViewHolder(ItemFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
