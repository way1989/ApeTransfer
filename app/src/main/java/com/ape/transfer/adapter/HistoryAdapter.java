package com.ape.transfer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.activity.UserInfoActivity;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.Util;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<P2PFileInfo> mFileItems;
    private OnItemClickListener mListener;
    private int mDirection;

    public HistoryAdapter(Context context, int direction,
                          OnItemClickListener onItemClickListener) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mFileItems = new ArrayList<>();
        mListener = onItemClickListener;
        mDirection = direction;
    }

    public void setDatas(ArrayList<P2PFileInfo> musicItems) {
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
        if (mDirection == 0) {
            view = mInflater.inflate(R.layout.history_item_send, parent, false);
        } else {
            view = mInflater.inflate(R.layout.history_item_receive, parent, false);
        }
        return new ViewHolder(view);
    }

    private boolean lessThanStandard(long selfTime, long lastTime) {
        return (selfTime - lastTime) < (30 * 60 * 1000);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        P2PFileInfo item = mFileItems.get(position);
        holder.itemView.setTag(item);

        // 本条与上一条时间间隔不超过0.5小时就不显示本条时间
        long lastTime = 0;
        if (position > 0) {
            lastTime = item.createTime;
        }

        long selfTime = item.createTime;
        if (lessThanStandard(selfTime, lastTime)) {
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(Util.formatDateString(App.getContext(), selfTime));
        }

        holder.ivThumb.setImageResource(R.drawable.file_icon_default);
        holder.tvTitle.setText(item.name);
        holder.tvInfo.setText(Formatter.formatFileSize(App.getContext(), item.size));
        if (item.percent < 100) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(item.percent);
        } else {
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.tvInfo.setVisibility(View.VISIBLE);
        }
        if (mDirection == 0) {
            holder.ivAvatar.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
            holder.tvTo.setText(App.getContext().getString(R.string.format_to));
        } else {
            holder.tvFrom.setText(App.getContext().getString(R.string.format_from));
        }
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mFileItems.get(position).md5.hashCode();
    }

//    public void unChecked(ArrayList<P2PFileInfo> lists) {
//        if (mFileItems.containsAll(lists)) {
//            for (P2PFileInfo item : mFileItems) {
//                item.selected = false;
//            }
//        }
//        notifyDataSetChanged();
//    }

    public interface OnItemClickListener {
        void onItemClick(View v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvTime;
        public ImageView ivAvatar;
        public TextView tvFrom;
        public TextView tvTo;
        public ImageView ivThumb;
        public Button btnOperation;

        public TextView tvTitle;
        public TextView tvInfo;
        public TextView tvPrecent;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
            tvFrom = (TextView) itemView.findViewById(R.id.tv_from);
            tvTo = (TextView) itemView.findViewById(R.id.tv_to);
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            btnOperation = (Button) itemView.findViewById(R.id.btn_operate);

            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvInfo = (TextView) itemView.findViewById(R.id.tv_info);
            tvPrecent = (TextView) itemView.findViewById(R.id.tv_percent);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v);
        }
    }
}
