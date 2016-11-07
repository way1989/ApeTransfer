package com.ape.backuprestore.modules;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 16-7-16.
 */
public class MusicRestoreComposer extends Composer {
    private static final String TAG = "MusicRestoreComposer";
    private int mIndex;
    private List<String> mFileNameList;
    private String mDestPath;
    private String mZipFileName;

    /**
     * Creates a new <code>MusicRestoreComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public MusicRestoreComposer(Context context) {
        super(context);
    }

    /**
     * Describe <code>init</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    @Override
    public final boolean init() {

        String parentPath = (new File(mParentFolderPath)).getParent();
        if (parentPath == null) {
            Logger.e(TAG, "init() parentPath == null");
            return false;
        } else {
            mDestPath = parentPath + File.separator + RESTORE
                    + File.separator + Constants.ModulePath.FOLDER_MUSIC;
        }

        boolean result = false;
        mFileNameList = new ArrayList<>();
        String path = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_MUSIC;
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            try {
                mZipFileName = path + File.separator + Constants.ModulePath.NAME_MUSICZIP;
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

    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    @Override
    public final int getModuleType() {
        return ModuleType.TYPE_MUSIC;
    }

    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    @Override
    public final int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        Logger.d(TAG, "getCount():" + count);
        return count;
    }

    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    @Override
    public final boolean isAfterLast() {
        if (mDestPath == null) {
            Logger.e(TAG, "isAfterLast... mDestPath == null");
            return true;
        }

        boolean result = true;
        if (mFileNameList != null && mFileNameList.size() > 0) {
            result = (mIndex >= mFileNameList.size());
        }
        Logger.d(TAG, "isAfterLast():" + result);
        return result;
    }

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    @Override
    public final boolean implementComposeOneEntity() {
        boolean result = false;
        if (mDestPath == null) {
            Logger.e(TAG, "implementComposeOneEntity... mDestPath == null");
            return false;
        }

        Logger.d(TAG, "mDestPath:" + mDestPath);
        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            String musicName = mFileNameList.get(mIndex++);
            String destFileName = mDestPath + File.separator + musicName;
            File destFile = new File(destFileName);
            if (destFile.exists()) {
                return true;
            }
            try {
                Logger.d(TAG, "mDestFileName:" + destFileName);
                BackupZip.unZipFile(mZipFileName, musicName, destFileName);
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                Logger.d(TAG, "unZipFile failed");
            }
        }

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
            Logger.d(TAG, "onEnd mIndex = " + mIndex
                    + "sendBroadcast " + "isAfterLast() = " + isAfterLast());
        }
    }

}
