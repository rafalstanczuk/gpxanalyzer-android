package com.itservices.gpxanalyzer.ui.storage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<File> fileList;
    private File selectedFile;
    private OnFileSelectedListener fileSelectedListener;

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    public FileAdapter(OnFileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    public void setFiles(List<File> files) {
        this.fileList = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = fileList.get(position);
        holder.fileName.setText(file.getName());

        // Highlight selected item
        holder.itemView.setSelected(file.equals(selectedFile));

        holder.itemView.setOnClickListener(v -> {
            selectedFile = file;
            notifyDataSetChanged(); // Refresh UI to highlight selection
            if (fileSelectedListener != null) {
                fileSelectedListener.onFileSelected(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (fileList != null) ? fileList.size() : 0;
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(android.R.id.text1);
        }
    }
}
