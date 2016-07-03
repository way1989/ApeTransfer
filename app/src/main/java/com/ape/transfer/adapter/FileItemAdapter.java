package com.ape.transfer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.photopicker.GlideHelper;
import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.util.Util;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<FileItem> mFileItems;
    private OnItemClickListener mListener;
    private int mFileCategory;

    public FileItemAdapter(Context context, int fileCategory,
                           OnItemClickListener onItemClickListener) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mFileItems = new ArrayList<>();
        mListener = onItemClickListener;
        mFileCategory = fileCategory;
    }

    public void setDatas(ArrayList<FileItem> musicItems) {
        mFileItems = musicItems;
        notifyDataSetChanged();
    }

    public void reset() {
        mFileItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (mFileCategory) {
            case P2PConstant.TYPE.APP:
                view = mInflater.inflate(R.layout.app_item, parent, false);
                break;
            case P2PConstant.TYPE.VIDEO:
            case P2PConstant.TYPE.PIC:
                view = mInflater.inflate(R.layout.video_item, parent, false);
                break;
            default:
                view = mInflater.inflate(R.layout.music_item, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem item = mFileItems.get(position);
        holder.itemView.setTag(item);
        switch (mFileCategory) {
            case P2PConstant.TYPE.APP:
                holder.ivIcon.setImageDrawable(item.appLogo);
                holder.tvName.setText(item.appLabel);
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case P2PConstant.TYPE.VIDEO:
            case P2PConstant.TYPE.PIC:
                GlideHelper.loadCropResource(item.path, holder.ivIcon);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case P2PConstant.TYPE.ZIP:
                holder.ivIcon.setImageResource(R.drawable.file_icon_rar);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(App.getContext(), item.dateModified));
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case P2PConstant.TYPE.DOC:
                holder.ivIcon.setImageResource(R.drawable.file_icon_default);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(App.getContext(), item.dateModified));
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case P2PConstant.TYPE.MUSIC:
                holder.ivIcon.setImageResource(R.drawable.file_icon_music);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(App.getContext(), item.dateModified));
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            default:

                break;
        }
        holder.ivCheckBox.setVisibility(item.selected ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mFileItems.get(position).id;
    }

    public interface OnItemClickListener {
        void onItemClick(View v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivCheckBox;
        public ImageView ivIcon;
        public TextView tvName;
        public TextView tvSize;
        public TextView tvDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ivCheckBox = (ImageView) itemView.findViewById(R.id.iv_selected);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v);
        }
    }
}
