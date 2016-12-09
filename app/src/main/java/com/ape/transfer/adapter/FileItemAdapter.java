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
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.FileIconLoader;
import com.ape.transfer.util.GlideHelper;
import com.ape.transfer.util.Util;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {
    private FileIconLoader mFileIconLoader;
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
        mFileIconLoader = new FileIconLoader(context);
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
            case Constant.TYPE.APP:
                view = mInflater.inflate(R.layout.item_app, parent, false);
                break;
            case Constant.TYPE.VIDEO:
            case Constant.TYPE.PIC:
                view = mInflater.inflate(R.layout.item_image_video, parent, false);
                break;
            default:
                view = mInflater.inflate(R.layout.item_normal, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem item = mFileItems.get(position);
        holder.itemView.setTag(item);
        switch (mFileCategory) {
            case Constant.TYPE.APP:
                //holder.ivIcon.setImageResource(R.drawable.file_icon_apk);
                mFileIconLoader.loadIcon(holder.ivIcon, item.path, item.id, Constant.TYPE.APP);
                holder.tvName.setText(item.fileName);
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case Constant.TYPE.VIDEO:
            case Constant.TYPE.PIC:
                GlideHelper.loadResource(item.path, holder.ivIcon);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case Constant.TYPE.ZIP:
                holder.ivIcon.setImageResource(R.drawable.file_icon_rar);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(item.dateModified));
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case Constant.TYPE.DOC:
                holder.ivIcon.setImageResource(R.drawable.file_icon_default);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(item.dateModified));
                holder.tvSize.setText(Formatter.formatFileSize(App.getContext(), item.size));
                break;
            case Constant.TYPE.MUSIC:
                holder.ivIcon.setImageResource(R.drawable.file_icon_music);
                holder.tvName.setText(Util.getNameFromFilename(Util.getNameFromFilepath(item.path)));
                holder.tvDuration.setText(Util.formatDateString(item.dateModified));
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
        return mFileItems.get(position).path.hashCode();
    }

    public void unChecked(ArrayList<FileItem> lists) {
        if (mFileItems.containsAll(lists)) {
            for (FileItem item : mFileItems) {
                item.selected = false;
            }
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivCheckBox;
        ImageView ivIcon;
        TextView tvName;
        TextView tvSize;
        TextView tvDuration;

        ViewHolder(View itemView) {
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
            //update ui
            FileItem item = (FileItem) v.getTag();
            int index = mFileItems.indexOf(item);
            boolean isSelected = item.selected;
            item.selected = !isSelected;
            notifyItemChanged(index);

            mListener.onItemClick(v);
        }
    }
}
