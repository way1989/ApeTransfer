package com.ape.backuprestore.modules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;
import com.ape.backuprestore.utils.SDCardUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by android on 16-7-16.
 */
public class MusicBackupComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/MusicBackupComposer";
    private static final Uri[] mMusicUriArray = {
            //Audio.Media.INTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    };
    private static final String[] mProjection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA};
    private ArrayList<String> mNameList;
    private Cursor[] mMusicCursorArray = {
            //null,
            null};
    private BackupZip mZipFileHandler;

    public MusicBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_MUSIC;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Cursor cur : mMusicCursorArray) {
            if (cur != null && !cur.isClosed() && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        MyLogger.logD(CLASS_TAG, "getCount():" + count);
        return count;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        for (Cursor cur : mMusicCursorArray) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        MyLogger.logD(CLASS_TAG, "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean init() {
        boolean result = false;
        for (int i = 0; i < mMusicCursorArray.length; ++i) {
//            if (mMusicUriArray[i] == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
//                String path = SDCardUtils.getStoragePath(mContext);
//                if (path != null && !path.trim().equals("")) {
//                    String externalSDPath = "%"
//                            + path.subSequence(0, path.lastIndexOf(File.separator)) + "%";
//                    mMusicCursorArray[i] = mContext.getContentResolver().query(mMusicUriArray[i],
//                            mProjection, MediaStore.Audio.Media.DATA + " not like ?",
//                            new String[]{
//                                    externalSDPath
//                            }, null);
//                }
//            } else {
                mMusicCursorArray[i] = mContext.getContentResolver().query(mMusicUriArray[i],
                        mProjection, null, null, null);
//            }
            if (mMusicCursorArray[i] != null) {
                mMusicCursorArray[i].moveToFirst();
                result = true;
            }
        }

        mNameList = new ArrayList<>();
        MyLogger.logD(CLASS_TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    @Override
    protected boolean implementComposeOneEntity() {
        boolean result = false;
        for (int i = 0; i < mMusicCursorArray.length; ++i) {
            if (mMusicCursorArray[i] != null && !mMusicCursorArray[i].isAfterLast()) {
                int dataColumn = mMusicCursorArray[i].getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                String data = mMusicCursorArray[i].getString(dataColumn);
                String destFileName = null;
                try {
                    String tmpName = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_MUSIC +
                            data.subSequence(data.lastIndexOf(File.separator), data.length()).toString();
                    destFileName = getDestinationName(tmpName);
                    if (destFileName != null) {
                        try {
                            //copyFile(data, destFileName);
                            mZipFileHandler.addFileByFileName(data, destFileName);
                            mNameList.add(destFileName);
                            result = true;
                        } catch (IOException e) {
                            MyLogger.logD(CLASS_TAG, MyLogger.MUSIC_TAG + "copy file fail");
                            try {
                                MyLogger.logE(CLASS_TAG, "[implementComposeOneEntity] finish");
                                mZipFileHandler.finish();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (super.mReporter != null) {
                                super.mReporter.onErr(e);
                            }
                        }
                    }

                    MyLogger.logD(CLASS_TAG, data + ",destFileName:" + destFileName);
                } catch (StringIndexOutOfBoundsException e) {
                    MyLogger.logE(CLASS_TAG, MyLogger.MUSIC_TAG
                            + " StringIndexOutOfBoundsException");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                mMusicCursorArray[i].moveToNext();
                break;
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
        if (getCount() > 0) {
            File path = new File(mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_MUSIC);
            if (!path.exists()) {
                path.mkdirs();
            }
            try {
                mZipFileHandler = new BackupZip(path + File.separator + Constants.ModulePath.NAME_MUSICZIP);
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                e.printStackTrace();
            }
        }

    }

    /**
     * Describe <code>onEnd</code> method here.
     */
    @Override
    public void onEnd() {
        super.onEnd();
        if (mNameList != null && mNameList.size() > 0) {
            mNameList.clear();
        }

        for (Cursor cur : mMusicCursorArray) {
            if (cur != null) {
                cur.close();
                cur = null;
            }
        }

        if (mZipFileHandler != null) {
            try {
                mZipFileHandler.finish();
            } catch (IOException e) {
                e.printStackTrace();
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
            } finally {
                mZipFileHandler = null;
            }
        }
    }


    /**
     * Describe <code>getDestinationName</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String getDestinationName(String name) {
        if (!mNameList.contains(name)) {
            return name;
        } else {
            return rename(name);
        }
    }

    /**
     * Describe <code>rename</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String rename(String name) {
        String tmpName;
        int id = name.lastIndexOf(".");
        int id2, leftLen;
        for (int i = 1; i < (1 << 12); ++i) {
            leftLen = 255 - (1 + Integer.toString(i).length() + name.length() - id);
            id2 = id <= leftLen ? id : leftLen;
            tmpName = name.subSequence(0, id2) + "~" + i + name.subSequence(id, name.length());
            if (!mNameList.contains(tmpName)) {
                return tmpName;
            }
        }

        return null;
    }

    private void copyFile(String srcFile, String destFile) throws IOException {
        try {
            File f1 = new File(srcFile);
            if (f1.exists() && f1.isFile()) {
                InputStream inStream = new FileInputStream(srcFile);
                FileOutputStream outStream = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int byteRead = 0;
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                }
                outStream.flush();
                outStream.close();
                inStream.close();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
