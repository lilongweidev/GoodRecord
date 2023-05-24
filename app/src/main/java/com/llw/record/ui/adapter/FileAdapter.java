package com.llw.record.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.llw.record.databinding.ItemFileBinding;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * 录音文件适配器
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<File> mFiles;

    private OnItemClickListener mOnItemClickListener;

    private OnItemChildClickListener mOnItemChildClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.mOnItemChildClickListener = onItemChildClickListener;
    }

    public FileAdapter(List<File> mFiles) {
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileBinding binding = ItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        binding.getRoot().setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, viewHolder.getAdapterPosition());
            }
        });
        binding.btnClear.setOnClickListener(v -> {
            if (mOnItemChildClickListener != null) {
                mOnItemChildClickListener.onItemChildClick(v, viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = mFiles.get(position);
        holder.binding.tvFileName.setText(file.getName());
        holder.binding.tvFilePath.setText(String.format("路径：%s", file.getPath()));
        holder.binding.tvFileSize.setText(String.format(Locale.getDefault(), "大小：%d Bytes", file.length()));
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemFileBinding binding;

        public ViewHolder(ItemFileBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
