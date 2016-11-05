package com.ape.backuprestore.modules;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.FileUtils;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 16-7-16.
 */
public class PictureRestoreComposer extends Composer {
    private static final String TAG = "PictureRestoreComposer";
    private int mIndex;
    private List<String> mFileNameList;
    private String mDestPath;
    private String mZipFileName;

    /**
     * Creates a new <code>PictureRestoreComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public PictureRestoreComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_PICTURE;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        Logger.d(TAG, "getCount():" + count);
        return count;
    }

    @Override
    public boolean init() {
        String parentPath = (new File(mParentFolderPath)).getParent();
        if (parentPath == null) {
            Logger.e(TAG, "init() parentPath == null result:" + false);
            return false;
        } else {
            mDestPath = parentPath + File.separator + RESTORE
                    + File.separator + Constants.ModulePath.FOLDER_PICTURE;
        }

        boolean result = false;
        mFileNameList = new ArrayList<>();
        String path = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_PICTURE;
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            try {
                mZipFileName = path + File.separator + Constants.ModulePath.NAME_PICTUREZIP;
                File file = new File(mZipFileName);
                if (file.exists()) {
                    mFileNameList = BackupZip.getFileList(mZipFileName, true, true, ".*");
                    result = mFileNameList.size() > 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        Logger.d(TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null && mFileNameList.size() > 0) {
            result = (mIndex >= mFileNameList.size());
        }

        Logger.d(TAG, "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean implementComposeOneEntity() {
        if (mDestPath == null) {
            Logger.e(TAG, "implementComposeOneEntity... mDestPath == null");
            return false;
        }

        boolean result = false;

        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            String picName = mFileNameList.get(mIndex++);
            String destFileName = mDestPath + picName;

            File destFile = new File(destFileName);
            if(destFile.exists()){
                return true;
            }
            try {
                BackupZip.unZipFile(mZipFileName, picName, destFileName);
                Logger.d(TAG, " insert database mDestFileName =" + destFileName);
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                e.printStackTrace();
            }
        }

        Logger.d(TAG, "implementComposeOneEntity:" + result);
        return result;
    }

    /**
     * Describe <code>onStart</code> method here.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (mDestPath != null) {
            File tmpFolder = new File(mDestPath);

            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }
        }

        Logger.d(TAG, "onStart()");
    }
    /**
     * Describe <code>onEnd</code> method here.
     */
    @Override
    public void onEnd() {
        super.onEnd();
        if (mDestPath != null) {
            Uri data = Uri.parse("file://" + mDestPath);
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
            Logger.d(TAG, "onEnd mIndex = " + mIndex + "sendBroadcast"
                    + ", isAfterLast() = " + isAfterLast());
        }
    }

}
