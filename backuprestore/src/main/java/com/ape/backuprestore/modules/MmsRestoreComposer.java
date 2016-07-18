package com.ape.backuprestore.modules;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by android on 16-7-16.
 */
public class MmsRestoreComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/MmsRestoreComposer";
    private ArrayList<MmsXmlInfo> mRecordList;
    private int mIndex;
    private long mTime;
    private Object mLock = new Object();
    private ArrayList<MmsRestoreContent> mPduList = null;
    private ArrayList<MmsRestoreContent> mTmpPduList = null;

    /**
     *
     * @param context
     */
    public MmsRestoreComposer(Context context) {
        super(context);
    }

    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int getModuleType() {
        return ModuleType.TYPE_MMS;
    }

    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int getCount() {
        int count = 0;
        if (mRecordList != null) {
            count = mRecordList.size();
        }

        MyLogger.logD(CLASS_TAG, "getCount():" + count);
        return count;
    }

    /**
     * Describe <code>init</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean init() {
        boolean result = false;
        mTmpPduList = new ArrayList<MmsRestoreContent>();
        String path = mParentFolderPath + File.separator +  Constants.ModulePath.FOLDER_MMS + File.separator
                + Constants.ModulePath.MMS_XML;
        MyLogger.logD(CLASS_TAG, "init():path:" + path);
        String content = getXmlInfo(path);
        if (content != null) {
            mRecordList = MmsXmlParser.parse(content);
            result = true;
        } else {
            mRecordList = new ArrayList<MmsXmlInfo>();
        }
        mIndex = 0;
        mTime = System.currentTimeMillis();

        MyLogger.logD(CLASS_TAG, "init():" + result);
        return result;
    }

    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isAfterLast() {
        boolean result = true;
        if (mRecordList != null) {
            result = (mIndex >= mRecordList.size()) ? true : false;
        }

        MyLogger.logD(CLASS_TAG, "isAfterLast():" + result);
        return result;
    }

    /**
     * Describe <code>composeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean composeOneEntity() {
        return implementComposeOneEntity();
    }

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean implementComposeOneEntity() {
        boolean result = false;
        if ((mRecordList != null) && (mIndex < mRecordList.size())) {
            MmsXmlInfo record = mRecordList.get(mIndex++);
            String msgBox = record.getMsgBox();
            Uri mMsgUri = getMsgBoxUri(msgBox);

            String simId = "-1";

            MyLogger.logD(CLASS_TAG, "mIdx:" + (mIndex) + ",mMsgUri:" + mMsgUri.toString()
                    + ",simId:" + simId);

            String pduFileName = record.getID();
            String fileName = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_MMS
                    + File.separator + pduFileName;
            MyLogger.logD(CLASS_TAG, "fileName:" + fileName);
            byte[] pduByteArray = readFileContent(fileName);
            if (pduByteArray != null) {
                result = true;
            }
            if (result) {
                MyLogger.logD(CLASS_TAG, "readFileContent finish, result:" + result);
                MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG + "MmsRestoreThread parse begin");
                MmsRestoreContent tmpContent = new MmsRestoreContent();
                tmpContent.mMsgUri = mMsgUri;
                tmpContent.mMsgInfo.put("locked", record.getIsLocked());
                tmpContent.mMsgInfo.put("read", record.getIsRead());
                tmpContent.mMsgInfo.put("sub_id", simId);
                if (mIndex == mRecordList.size()) {
                    tmpContent.mMsgInfo.put("index", "0");
                    MyLogger.logD(CLASS_TAG, "this is last mms");
                } else {
                    tmpContent.mMsgInfo.put("index", "" + mIndex);
                    MyLogger.logD(CLASS_TAG, "this is tmpContent.mNumber=" + mIndex);
                }

                MyLogger.logI(CLASS_TAG, "mMsgUri = " + mMsgUri);
                if (mMsgUri == Telephony.Mms.Inbox.CONTENT_URI) {
                    try {
                        tmpContent.mRetrieveConf = (RetrieveConf) new PduParser(pduByteArray,
                                shouldParseContentDisposition())
                                .parse(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (tmpContent.mRetrieveConf == null) {
                            tmpContent.mIndConf = (NotificationInd) new PduParser(pduByteArray,
                                    shouldParseContentDisposition())
                                    .parse(true);
                        }
                    }
                } else if (mMsgUri == Telephony.Mms.Sent.CONTENT_URI) {
                    try {
                        tmpContent.mSendConf = (SendReq) new PduParser(pduByteArray,
                                shouldParseContentDisposition()).parse(true);
                    } catch (Exception e) {
                        MyLogger.logI(CLASS_TAG, " error!!!");
                        result = false;
                    }
                }
                mTmpPduList.add(tmpContent);
            }

            if (mIndex % Constants.NUMBER_IMPORT_MMS_EACH == 0 || isAfterLast()) {
                if (mPduList != null) {
                    synchronized (mLock) {
                        try {
                            MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG +
                                    "wait for MmsRestoreThread");
                            while (mPduList != null) {
                                mLock.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mPduList = mTmpPduList;
                new MmsRestoreThread().start();
                if (!isAfterLast()) {
                    mTmpPduList = new ArrayList<MmsRestoreContent>();
                }
            }
        }

        return result;
    }

    /*    *//**
     * Describe <code>deleteAllPhoneMms</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    /*
     * private boolean deleteAllPhoneMms() { boolean result = false; if
     * (mContext != null) { int count =
     * mContext.getContentResolver().delete(Uri.parse(Constants.URI_MMS),
     * "msg_box <> ?", new String[] { Constants.MESSAGE_BOX_TYPE_INBOX }); count
     * += mContext.getContentResolver().delete(Uri.parse(Constants.URI_MMS),
     * "date < ?", new String[] { Long.toString(mTime) });
     *
     * MyLogger.logD(CLASS_TAG, "deleteAllPhoneMms():" + count +
     * " mms deleted!"); result = true; }
     *
     * return result; }
     *//**
     * Describe <code>onStart</code> method here.
     *
     */
    /*
     * public void onStart() { super.onStart(); deleteAllPhoneMms();
     *
     * MyLogger.logD(CLASS_TAG, "onStart()"); }
     */

    /**
     * Describe <code>onEnd</code> method here.
     *
     */
    public void onEnd() {
        if (mPduList != null) {
            synchronized (mLock) {
                try {
                    MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG + "wait for MmsRestoreThread");
                    while (mPduList != null) {
                        mLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onEnd();

        MyLogger.logD(CLASS_TAG, "onEnd()");
    }

    /**
     * Describe <code>getMsgBoxUri</code> method here.
     *
     * @param msgBox
     *            a <code>String</code> value
     * @return an <code>Uri</code> value
     */
    private Uri getMsgBoxUri(String msgBox) {
        if (msgBox.equals(Constants.MESSAGE_BOX_TYPE_INBOX)) {
            return Telephony.Mms.Inbox.CONTENT_URI;
        } else if (msgBox.equals(Constants.MESSAGE_BOX_TYPE_SENT)) {
            return Telephony.Mms.Sent.CONTENT_URI;
        } else if (msgBox.equals(Constants.MESSAGE_BOX_TYPE_DRAFT)) {
            return Telephony.Mms.Draft.CONTENT_URI;
        } else if (msgBox.equals(Constants.MESSAGE_BOX_TYPE_OUTBOX)) {
            return Telephony.Mms.Outbox.CONTENT_URI;
        }

        return Telephony.Mms.Inbox.CONTENT_URI;
    }

    /**
     *
     * @author mtk81330
     *
     */
    private class MmsRestoreThread extends Thread {
        @Override
        public void run() {
            for (int j = 0; (mPduList != null) && (j < mPduList.size()); ++j) {
                MmsRestoreContent content = mPduList.get(j);
                Uri mMsgUri = content.mMsgUri;
                HashMap<String, String> msgInfo = content.mMsgInfo;

                PduPersister persister = PduPersister.getPduPersister(mContext);
                RetrieveConf retrieveConf = content.mRetrieveConf;
                NotificationInd indConf = content.mIndConf;
                SendReq sendConf = content.mSendConf;

                if (retrieveConf != null || indConf != null || sendConf != null) {
                    MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG + "MmsRestoreThread parse finish");
                    Uri tmpUri = null;
                    try {
                        if (mMsgUri == Telephony.Mms.Inbox.CONTENT_URI) {
                            if (retrieveConf != null || indConf != null) {
                                tmpUri = persister.persistEx(retrieveConf != null ? retrieveConf
                                        : indConf, mMsgUri, true, msgInfo);
                            } else {
                                MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG +
                                        "retrieveConf and indConf is null");
                            }
                        } else {
                            if (sendConf != null) {
                                tmpUri = persister.persistEx(sendConf, mMsgUri, msgInfo);
                            } else {
                                MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG + "sendConf is null");
                            }
                        }
                        MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG +
                                "MmsRestoreThread persist finish");
                    } catch (MmsException e) {
                        MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG +
                                "MmsRestoreThread MmsException");
                        e.printStackTrace();
                    } catch (Exception e) {
                        MyLogger.logD(CLASS_TAG, MyLogger.MMS_TAG + "MmsRestoreThread Exception");
                        e.printStackTrace();
                    } finally {
                        increaseComposed(tmpUri != null ? true : false);
                    }
                } else {
                    if (mReporter != null) {
                        mReporter.onErr(new IOException());
                    }
                }
            }

            synchronized (mLock) {
                mPduList = null;
                mLock.notifyAll();
            }
        }
    }

    /**
     *
     * @author mtk81330
     *
     */
    private class MmsRestoreContent {
        Uri mMsgUri;
        HashMap<String, String> mMsgInfo = new HashMap<String, String>() ;
        RetrieveConf mRetrieveConf = null;
        NotificationInd mIndConf = null;
        SendReq mSendConf = null;
    }


    /**
     * Describe <code>readFileContent</code> method here.
     *
     * @param fileName
     *            a <code>String</code> value
     * @return a <code>byte[]</code> value
     */
    private byte[] readFileContent(String fileName) {
        try {
            InputStream is = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[512];
            while ((len = is.read(buffer, 0, 512)) != -1) {
                baos.write(buffer, 0, len);
            }

            is.close();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Describe <code>getXmlInfo</code> method here.
     *
     * @param fileName
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String getXmlInfo(String fileName) {
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[512];
            while ((len = is.read(buffer, 0, 512)) != -1) {
                baos.write(buffer, 0, len);
            }

            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * shouldParseContentDisposition.
     *
     * @return boolean
     */
    public static boolean shouldParseContentDisposition() {
        boolean result = true;
        try {
            Bundle bundle = SmsManager.getDefault().getCarrierConfigValues();
            if (bundle != null) {
                result = bundle.getBoolean(SmsManager.MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION,
                        true);
            }
        } catch (NullPointerException e) {
            MyLogger.logE(CLASS_TAG, "shouldParseContentDisposition : " + e.getMessage());
        }
        return result;
    }
}
