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
public class PictureBackupComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/PictureBackupComposer";

    private static final String[] mProjection = new String[]{MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA};
    private static final Uri[] mPictureUriArray = {MediaStore.Images.Media.EXTERNAL_CONTENT_URI};
    private Cursor[] mPictureCursorArray = {null};

    private ArrayList<String> mFileNameList = null;
    private BackupZip mZipFileHandler;

    public PictureBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_PICTURE;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Cursor cur : mPictureCursorArray) {
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
        for (Cursor cur : mPictureCursorArray) {
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
        for (int i = 0; i < mPictureCursorArray.length; ++i) {
//            if (mPictureUriArray[i] == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
//                String path = SDCardUtils.getStoragePath(mContext);
//                if (path != null && !path.trim().equals("")) {
//                    String externalSDPath = "%"
//                            + path.subSequence(0, path.lastIndexOf(File.separator)) + "%";
//                    mPictureCursorArray[i] = mContext.getContentResolver().query(
//                            mPictureUriArray[i],
//                            mProjection, MediaStore.Images.Media.DATA + " not like ?",
//                            new String[]{
//                                    externalSDPath
//                            }, null);
//                }
//            } else {
                mPictureCursorArray[i] = mContext.getContentResolver().query(mPictureUriArray[i],
                        mProjection, null, null, null);
//            }

            if (mPictureCursorArray[i] != null) {
                mPictureCursorArray[i].moveToFirst();
                result = true;
            }
        }

        mFileNameList = new ArrayList<String>();

        MyLogger.logD(CLASS_TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    @Override
    protected boolean implementComposeOneEntity() {
        boolean result = false;
        for (int i = 0; i < mPictureCursorArray.length; ++i) {
            if (mPictureCursorArray[i] != null && !mPictureCursorArray[i].isAfterLast()) {
                int dataColumn = mPictureCursorArray[i]
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String data = mPictureCursorArray[i].getString(dataColumn);

                String destnationFileName = null;
                try {
                    String tmpName = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_PICTURE +
                            data.subSequence(data.lastIndexOf(File.separator), data.length()).toString();
                    destnationFileName = getDestinationName(tmpName);
                } catch (StringIndexOutOfBoundsException e) {
                    MyLogger.logD(CLASS_TAG, "data OutOfBoundsException:data" + data);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (destnationFileName != null) {
                    try {
                        // copyFile(data, destnationFileName);
                        mZipFileHandler.addFileByFileName(data, destnationFileName);
                        mFileNameList.add(destnationFileName);
                        result = true;
                    } catch (IOException e) {
                        MyLogger.logD(CLASS_TAG, "copy file fail");
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
                MyLogger.logD(CLASS_TAG, "pic:" + data + ",destName:" + destnationFileName);
                mPictureCursorArray[i].moveToNext();
                break;
            }
        }

        return result;
    }

    private String getDestinationName(String name) {
        if (!mFileNameList.contains(name)) {
            return name;
        } else {
            return rename(name);
        }
    }

    private String rename(String name) {
        String tmpName;
        int id = name.lastIndexOf(".");
        int id2, leftLen;
        for (int i = 1; i < (1 << 12); ++i) {
            leftLen = 255 - (1 + Integer.toString(i).length() + name.length() - id);
            id2 = id <= leftLen ? id : leftLen;
            tmpName = name.subSequence(0, id2) + "~" + i + name.subSequence(id, name.length());
            if (!mFileNameList.contains(tmpName)) {
                return tmpName;
            }
        }

        return null;
    }

    /**
     * Describe <code>onStart</code> method here.
     */
    public final void onStart() {
        super.onStart();
        if (getCount() > 0) {
            File path = new File(mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_PICTURE);
            if (!path.exists()) {
                path.mkdirs();
            }
            try {
                mZipFileHandler = new BackupZip(path + File.separator + Constants.ModulePath.NAME_PICTUREZIP);
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                e.printStackTrace();
            }
        }

    }

    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null && mFileNameList.size() > 0) {
            mFileNameList.clear();
        }

        for (Cursor cur : mPictureCursorArray) {
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
