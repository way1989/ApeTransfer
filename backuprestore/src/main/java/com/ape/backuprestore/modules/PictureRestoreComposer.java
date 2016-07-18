package com.ape.backuprestore.modules;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.FileUtils;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by android on 16-7-16.
 */
public class PictureRestoreComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/PictureRestoreComposer";
    private static final String[] PROJECTION = new String[]{
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
    private int mIndex;
    private Object mLock = new Object();
    private File[] mFileList;
    private ArrayList<String> mFileNameList;
    private String mDestPath;
    private String mZipFileName;
    private boolean mImport;
    private String mDestFileName;
    private MediaScannerConnection mMediaScannerConnection;
    private boolean mIsMediaScannerConnected;


    /**
     * Creates a new <code>PictureRestoreComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public PictureRestoreComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_PICTURE;
    }

    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        if (count == 0 && mFileList != null) {
            count = mFileList.length;
        }
        MyLogger.logD(CLASS_TAG, "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        String path = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_PICTURE;
        mFileNameList = new ArrayList<String>();
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            try {
                mZipFileName = path + File.separator + Constants.ModulePath.NAME_PICTUREZIP;
                File file = new File(mZipFileName);
                if (file != null && file.exists()) {
                    mFileNameList = (ArrayList<String>) BackupZip.getFileList(mZipFileName, true,
                            true, ".*");
                    String tmppath = (new File(mParentFolderPath)).getParent();
                    if (tmppath != null) {
                        mDestPath = tmppath.subSequence(0, tmppath.length() - 12)
                                + File.separator
                                + "Pictures"
                                + mParentFolderPath.subSequence(
                                mParentFolderPath.lastIndexOf(File.separator),
                                mParentFolderPath.length());
                        MyLogger.logD(CLASS_TAG, "mDestPath:" + mDestPath);
                        result = true;
                    }
                } else {
                    // for old datas
                    mFileList = folder.listFiles();
                    result = true;
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

        MyLogger.logD(CLASS_TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null && mFileNameList.size() > 0) {
            result = (mIndex >= mFileNameList.size()) ? true : false;
        } else if (mFileList != null) {
            // for old data
            result = (mIndex >= mFileList.length) ? true : false;
        }

        MyLogger.logD(CLASS_TAG, "isAfterLast():" + result);
        return result;
    }

    public boolean implementComposeOneEntity() {
        boolean result = false;
        if (mDestPath == null) {
            // for old data
            if (mFileList != null) {
                mIndex++;
                result = true;
            }
            return result;
        }
        synchronized (mLock) {
            while (!mIsMediaScannerConnected) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            String picName = mFileNameList.get(mIndex++);
            mDestFileName = mDestPath + picName;
            if (mImport) {
                File fileName = new File(mDestFileName);
                if (fileName.exists()) {
                    mDestFileName = rename(mDestFileName);
                }
            }
            try {
                BackupZip.unZipFile(mZipFileName, picName, mDestFileName);
                MyLogger.logD(CLASS_TAG, " insert database mDestFileName ="
                        + mDestFileName);
                FileUtils.scanPathforMediaStore(mDestFileName, mContext);
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                result = false;
                e.printStackTrace();
            }
        }

        MyLogger.logD(CLASS_TAG, "implementComposeOneEntity:" + result);
        return result;
    }

    private void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    int count = mContext.getContentResolver().delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + " like ?",
                            new String[]{file.getAbsolutePath()});
                    MyLogger.logD(CLASS_TAG, "deleteFolder():" + count + ":"
                            + file.getAbsolutePath());
                    file.delete();
                } catch (NullPointerException e) {
                    MyLogger.logD(CLASS_TAG, "deleteFolder: exception");
                }

            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; ++i) {
                        this.deleteFolder(files[i]);
                    }
                }
            }
            file.delete();
        }
    }

    /**
     * Describe <code>onStart</code> method here.
     */
    public void onStart() {
        super.onStart();
        mMediaScannerConnection = new MediaScannerConnection(this.mContext,
                new ScanCompletedListener());
        mMediaScannerConnection.connect();
        // && super.checkedCommand())
        if (mDestPath != null) {
            File tmpFolder = new File(mDestPath);
            deleteFolder(tmpFolder);

            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }
        } else {
            mImport = true;
        }

        MyLogger.logD(CLASS_TAG, "onStart()");
    }

    private String rename(String name) {
        String tmpName = name.subSequence(name.lastIndexOf(File.separator) + 1,
                name.length()).toString();
        String path = name.subSequence(0, name.lastIndexOf(File.separator) + 1)
                .toString();
        MyLogger.logD(CLASS_TAG, " rename:tmpName  ==== " + tmpName);
        String rename = null;
        File tmpFile = null;
        int id = tmpName.lastIndexOf(".");
        int id2;
        int leftLen;
        for (int i = 1; i < (1 << 12); ++i) {
            leftLen = 255 - (1 + Integer.toString(i).length()
                    + tmpName.length() - id);
            id2 = id <= leftLen ? id : leftLen;
            rename = tmpName.subSequence(0, id2) + "~" + i
                    + tmpName.subSequence(id, tmpName.length());
            tmpFile = new File(path + rename);
            String tmpFileName = tmpFile.getAbsolutePath();
            MyLogger.logD(CLASS_TAG, " rename:tmpFileName == " + tmpFileName);
            if (tmpFile.exists()) {
                continue;
            } else {
                MyLogger.logD(CLASS_TAG, " rename: rename === " + rename);
                break;
            }
        }
        return path + rename;
    }

    /**
     * Describe class <code>ScanCompletedListener</code> here.
     *
     * @author 1
     */
    class ScanCompletedListener implements MediaScannerConnection.MediaScannerConnectionClient {
        @Override
        public void onScanCompleted(String path, Uri uri) {
            MyLogger.logD(CLASS_TAG, mIndex + "nScanCompleted");
            if (uri != null) {
                MyLogger.logD(CLASS_TAG, mIndex + path + "insert to db successed!");
            } else {
                MyLogger.logD(CLASS_TAG, mIndex + path + "insert to db fail");
            }
        }

        @Override
        public void onMediaScannerConnected() {
            synchronized (mLock) {
                mIsMediaScannerConnected = true;
                MyLogger.logD(CLASS_TAG, "MediaScannerConnected");
                mLock.notifyAll();
            }
        }
    }
}
