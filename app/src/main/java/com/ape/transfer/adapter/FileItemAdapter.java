package com.ape.transfer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.model.FileItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by android on 16-6-28.
 */
public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<FileItem> mMusicItems;
    private OnItemClickListener mListener;

    public FileItemAdapter(Context context, OnItemClickListener onItemClickListener) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mMusicItems = new ArrayList<>();
        mListener = onItemClickListener;
    }

    public void setDatas(ArrayList<FileItem> musicItems) {
        if (musicItems == null)
            return;
        mMusicItems = musicItems;
        notifyDataSetChanged();
    }

    public void reset() {
//        mMusicItems.close();
        mMusicItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.music_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem musicItem = mMusicItems.get(position);
        holder.itemView.setTag(musicItem);
        holder.musicName.setText(new File(musicItem.path).getName());
        holder.musicSize.setText(Formatter.formatFileSize(App.getContext(), musicItem.size));
        holder.musicDuration.setText(formatTime(musicItem.dateModified));
        holder.checkBox.setVisibility(musicItem.selected ? View.VISIBLE : View.GONE);
    }

    private String formatTime(long dateModified) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return format.format(new Date(dateModified));
    }

    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mMusicItems.get(position).id;
    }

    public interface OnItemClickListener {
        void onItemClick(View v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView checkBox;
        public TextView musicName;
        public TextView musicSize;
        public TextView musicDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            checkBox = (ImageView) itemView.findViewById(R.id.iv_selected);
            musicName = (TextView) itemView.findViewById(R.id.tv_musicName);
            musicSize = (TextView) itemView.findViewById(R.id.tv_musicSize);
            musicDuration = (TextView) itemView.findViewById(R.id.tv_musicDuration);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v);
        }
    }
}
