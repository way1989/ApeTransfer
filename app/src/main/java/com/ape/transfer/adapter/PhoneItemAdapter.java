package com.ape.transfer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.activity.UserInfoActivity;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class PhoneItemAdapter extends RecyclerView.Adapter<PhoneItemAdapter.ViewHolder> {
    private ArrayList<P2PNeighbor> mNeighbors;
    private LayoutInflater mInflater;

    public PhoneItemAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mNeighbors = new ArrayList<>();
    }

    public void setDatas(ArrayList<P2PNeighbor> neighbors) {
        mNeighbors.clear();
        mNeighbors.addAll(neighbors);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.phone_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final P2PNeighbor neighbor = mNeighbors.get(position);
        holder.textView.setText(neighbor.alias);
        holder.ivAvatar.setImageResource(UserInfoActivity.HEAD[neighbor.icon]);
        holder.checkBox.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mNeighbors.size();
    }

    @Override
    public long getItemId(int position) {
        return mNeighbors.get(position).ip.hashCode();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAvatar;
        public ImageView checkBox;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
            checkBox = (ImageView) itemView.findViewById(R.id.checkBox);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}
