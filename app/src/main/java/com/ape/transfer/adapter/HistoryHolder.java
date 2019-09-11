package com.ape.transfer.adapter;

import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.activity.UserInfoActivity;
import com.ape.transfer.model.HistoryTransfer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.core.P2PManager;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Util;
import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;

/**
 * Created by android on 16-10-31.
 */

public class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        HistoryTransfer.ProgressListener {
    private TextView tvTime;
    private ImageView ivAvatar;
    private TextView tvFrom;
    private TextView tvTo;
    private ImageView ivThumb;
    private Button btnOperation;

    private TextView tvTitle;
    private TextView tvInfo;
    //TextView tvPercent;
    private NumberProgressBar progressBar;

    public HistoryHolder(View itemView) {
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
        //tvPercent = (TextView) itemView.findViewById(R.id.tv_percent);
        progressBar = (NumberProgressBar) itemView.findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        TransferFile file = (TransferFile) v.getTag();
        //还没下载完就直接返回
        if (file.position < file.size)
            return;
        String filePath;
        if (file.direction == TransferFile.Direction.DIRECTION_SEND) {
            filePath = file.path;
        } else {
            String path = P2PManager.getSavePath(file.type);
            filePath = path + File.separator + file.name;
        }
        Intent intent = new Intent();
        switch (file.type) {
            case Constant.TYPE.APP:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath),
                        "application/vnd.android.package-archive");
                break;
            case Constant.TYPE.PIC:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "image/*");
                break;
            case Constant.TYPE.VIDEO:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "video/*");
                break;
            case Constant.TYPE.ZIP:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "zip/*");
                break;
            case Constant.TYPE.DOC:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "doc/*");
                break;
            case Constant.TYPE.MUSIC:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "music/*");
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        try {
            v.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setData(HistoryTransfer item) {
        item.setProgressListener(this);
        itemView.setTag(item.transferFile);

        tvTime.setVisibility(View.VISIBLE);
        tvTime.setText(Util.formatDateString(item.transferFile.createTime));

        updateThumb(ivThumb, item.transferFile);
        tvTitle.setText(item.transferFile.name);
        tvInfo.setText(Formatter.formatFileSize(App.getApp(), item.transferFile.size));
        updateProgressUI(item.transferFile);
        ivAvatar.setImageResource(UserInfoActivity.HEAD[item.peer.avatar]);
        if (item.transferFile.direction == TransferFile.Direction.DIRECTION_SEND) {
            tvTo.setText(App.getApp().getString(R.string.format_to, item.peer.alias));
        } else {
            tvFrom.setText(App.getApp().getString(R.string.format_from, item.peer.alias));
        }
        btnOperation.setVisibility(View.GONE);//取消功能暂未实现
    }

    private void updateThumb(ImageView ivThumb, TransferFile file) {
        switch (file.type) {
            case Constant.TYPE.APP:
                ivThumb.setImageResource(R.drawable.file_icon_apk);
                break;
            case Constant.TYPE.PIC:
                ivThumb.setImageResource(R.drawable.file_icon_default);
                break;
            case Constant.TYPE.VIDEO:
                ivThumb.setImageResource(R.drawable.file_icon_video);
                break;
            case Constant.TYPE.ZIP:
                ivThumb.setImageResource(R.drawable.file_icon_rar);
                break;
            case Constant.TYPE.DOC:
                ivThumb.setImageResource(R.drawable.file_icon_doc);
                break;
            case Constant.TYPE.MUSIC:
                ivThumb.setImageResource(R.drawable.file_icon_music);
                break;
            default:
                ivThumb.setImageResource(R.drawable.file_icon_default);
                break;
        }
    }

    private void updateProgressUI(TransferFile file) {
        if (file.position < file.size) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(getPercent(file));
            //holder.tvPercent.setText(getPercent(item) + "%");
            tvInfo.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            //holder.tvPercent.setVisibility(View.INVISIBLE);
            tvInfo.setVisibility(View.VISIBLE);
        }
    }

    private int getPercent(TransferFile fileInfo) {
        return (int) ((100.0f * fileInfo.position) / fileInfo.size);
    }

    @Override
    public void updateProgress(TransferFile file) {
        updateProgressUI(file);
    }
}
