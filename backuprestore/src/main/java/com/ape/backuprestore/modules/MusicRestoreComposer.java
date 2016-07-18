package com.ape.backuprestore.modules;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by android on 16-7-16.
 */
public class MusicRestoreComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/MusicRestoreComposer";
    private static final String[] mProjection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA};
    private int mIndex;
    private File[] mFileList;
    private ArrayList<String> mExistFileList = null;
    private ArrayList<String> mFileNameList;
    private String mDestPath;
    private String mZipFileName;
    private boolean mImport;
    private String mDestFileName;

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
    public final boolean init() {
        boolean result = false;
        String path = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_MUSIC;
        mFileNameList = new ArrayList<String>();
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            try {
                mZipFileName = path + File.separator + Constants.ModulePath.NAME_MUSICZIP;
                File file = new File(mZipFileName);
                if (file != null && file.exists()) {
                    mFileNameList = (ArrayList<String>) BackupZip.getFileList(mZipFileName, true,
                            true, ".*");
                    String tmppath = (new File(mParentFolderPath)).getParent();
                    if (tmppath == null) {
                        return result;
                    } else {
                        mDestPath = tmppath.subSequence(0, tmppath.length() - 12)
                                + File.separator
                                + "Music"
                                + mParentFolderPath.subSequence(
                                mParentFolderPath.lastIndexOf(File.separator),
                                mParentFolderPath.length());
                    }
                    // mExistFileList = getExistFileList(mDestPath);
                    result = true;
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

    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getModuleType() {
        return ModuleType.TYPE_MUSIC;
    }

    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getCount() {
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

    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isAfterLast() {
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

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean implementComposeOneEntity() {

        boolean result = false;
        if (mDestPath == null) {
            // for old data
            if (mFileList != null) {
                mIndex++;
                result = true;
            }
            return result;
        }
        MyLogger.logD(CLASS_TAG, "mDestPath:" + mDestPath);
        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            // File file = mFileList[mIndex++];
            String musicName = mFileNameList.get(mIndex++);
            mDestFileName = mDestPath + File.separator + musicName;

            if (mImport) {
                File fileName = new File(mDestFileName);
                if (fileName.exists()) {
                    mDestFileName = rename(mDestFileName);
                }
            }
            try {
                MyLogger.logD(CLASS_TAG, "mDestFileName:" + mDestFileName);
                BackupZip.unZipFile(mZipFileName, musicName, mDestFileName);
//                Uri data = Uri.parse("file://" + mDestFileName);
//                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                MyLogger.logD(CLASS_TAG, "unzipfile failed");
            }

            result = true;
        }

        return result;

    }


    /**
     * Describe <code>onStart</code> method here.
     */
    public void onStart() {
        super.onStart();
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

    /**
     * Describe <code>onEnd</code> method here.
     */
    public void onEnd() {
        super.onEnd();
        if (mDestPath != null) {
            Uri data = Uri.parse("file://" + mDestPath);
            mContext.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
            MyLogger.logD(CLASS_TAG, "onEnd mIndex = " + mIndex
                    + "sendBroadcast " + "isAfterLast() = " + isAfterLast());
        }
    }

    private void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    int count = mContext.getContentResolver().delete(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Media.DATA + " like ?",
                            new String[]{file.getAbsolutePath()});
                    MyLogger.logD(CLASS_TAG, "deleteFolder():" + count + ":"
                            + file.getAbsolutePath());
                    file.delete();
                } catch (NullPointerException e) {
                    MyLogger.logD(CLASS_TAG, "deleteFolder: exception");
                }
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    this.deleteFolder(files[i]);
                }
            }
            file.delete();
        }
    }


    private String rename(String name) {
        String tmpName = name.subSequence(name.lastIndexOf(File.separator) + 1,
                name.length()).toString();
        String path = name.subSequence(0, name.lastIndexOf(File.separator) + 1)
                .toString();
        MyLogger.logD(CLASS_TAG, " rename:tmpName:" + tmpName);
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
            MyLogger.logD(CLASS_TAG, " rename:tmpFileName:" + tmpFileName);
            if (tmpFile.exists()) {
                continue;
            } else {
                MyLogger.logD(CLASS_TAG, " rename: rename:" + rename);
                break;
            }
        }
        return path + rename;

    }
}
