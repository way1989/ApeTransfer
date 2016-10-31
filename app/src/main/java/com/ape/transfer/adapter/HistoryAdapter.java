package com.ape.transfer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ape.transfer.R;
import com.ape.transfer.model.HistoryTransfer;
import com.ape.transfer.p2p.beans.TransferFile;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
    private LayoutInflater mInflater;
    private ArrayList<HistoryTransfer> mTransfers;
    private int mDirection;

    public HistoryAdapter(Context context, int direction) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mTransfers = new ArrayList<>();
        mDirection = direction;
    }

    public void setData(ArrayList<HistoryTransfer> transfers) {
        mTransfers = transfers;
        notifyDataSetChanged();
    }

    public void reset() {
        mTransfers.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @Override
    public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (mDirection == TransferFile.Direction.DIRECTION_SEND) {
            view = mInflater.inflate(R.layout.history_item_send, parent, false);
        } else {
            view = mInflater.inflate(R.layout.history_item_receive, parent, false);
        }
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryHolder holder, int position) {
        HistoryTransfer data = mTransfers.get(position);
        holder.setData(data);
    }

    @Override
    public int getItemCount() {
        return mTransfers.size();
    }

    @Override
    public long getItemId(int position) {
        return mTransfers.get(position).transferFile.hashCode();
    }

    public void updateItem(HistoryTransfer transfer) {
        int index = mTransfers.indexOf(transfer);
        if (index != -1) {
            HistoryTransfer eventTransfer = mTransfers.get(index);
            eventTransfer.transferFile.position = transfer.transferFile.position;
            eventTransfer.updateProgress();
        }
    }

}
