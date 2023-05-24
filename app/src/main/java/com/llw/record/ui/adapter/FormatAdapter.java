package com.llw.record.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.llw.record.audio.EasyFormat;
import com.llw.record.databinding.ItemFormatBinding;

import java.util.List;
import java.util.Locale;

public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.ViewHolder> {

    private List<EasyFormat> formats;

    public FormatAdapter(List<EasyFormat> devices) {
        this.formats = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(ItemFormatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        viewHolder.binding.tvFormat.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                int position = viewHolder.getAdapterPosition();
                mOnItemClickListener.onItemClick(v, position);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EasyFormat easyFormat = formats.get(position);
        holder.binding.tvFormat.setText(String.format(Locale.getDefault(), "%d，%d，%d", easyFormat.getSampleRate(), easyFormat.getChannel(), easyFormat.getEncoding()));
    }

    @Override
    public int getItemCount() {
        return formats.size();
    }

    public List<EasyFormat> getFormats() {
        return formats;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemFormatBinding binding;

        public ViewHolder(ItemFormatBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
