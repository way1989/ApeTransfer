package com.ape.transfer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
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
public class MusicItemAdapter extends RecyclerView.Adapter<MusicItemAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Cursor mCursor;

    public MusicItemAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);

    }

    public void setDatas(Cursor cursor) {
        if(cursor == null)
            return;
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void reset(){
//        mCursor.close();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.music_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mCursor == null || mCursor.getCount() < 1)
            return;
        holder.musicName.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
        holder.musicSize.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
        holder.checkBox.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mCursor == null || mCursor.getCount() < 1 ? 0 : mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView checkBox;
        public TextView musicName;
        public TextView musicSize;
        public TextView musicDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = (ImageView) itemView.findViewById(R.id.checkBox);
            musicName = (TextView) itemView.findViewById(R.id.tv_musicName);
            musicSize = (TextView) itemView.findViewById(R.id.tv_musicSize);
            musicDuration = (TextView) itemView.findViewById(R.id.tv_musicDuration);
        }
    }
}
