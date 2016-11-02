package com.ape.transfer.p2p.util;


import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.APP;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.BACKUP;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.DOCUMENT;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.MUSIC;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.PICTURE;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.VIDEO;
import static com.ape.transfer.p2p.util.Constant.TYPE_NAME.ZIP;

/**
 * Created by way on 2016/10/21.
 */
public class Constant {

    public static final int BUFFER_LENGTH = 8192;
    public static final int PORT = 10000;
    public static final int FILE_TRANSFER_PORT = 10010;

    public static final String FORMAT = "gbk";

    public static final String MSG_SEPARATOR = "\0";

    public static final String MULTI_ADDRESS = "192.168.43.255";//手机的ip几乎都是192.168.43.xx

    public static final String FILE_SHARE_SAVE_PATH = "ApeTransfer";

    public static final int MAXIMUM_POOL_SIZE = 4;
    public static final String[] TYPE_DIR = {APP, PICTURE, VIDEO, ZIP, DOCUMENT, MUSIC, BACKUP};

    public interface TYPE_NAME {
        String APP = "App";
        String PICTURE = "Picture";
        String VIDEO = "Video";
        String ZIP = "Zip";
        String DOCUMENT = "Document";
        String MUSIC = "Music";
        String BACKUP = "Backup";
    }

    public interface TYPE {
        int APP = 0;
        int PIC = 1;
        int VIDEO = 2;
        int ZIP = 3;
        int DOC = 4;
        int MUSIC = 5;
        int BACKUP = 6;
    }


    public interface UI {
        int ADD_NEIGHBOR = 1000;
        int REMOVE_NEIGHBOR = 1001;
        int STOP = 1002;
    }

    public interface Command {
        int ON_LINE = 0;
        int OFF_LINE = 1;
        int ON_LINE_ANS = 2;

        int SEND_FILE_REQ = 3;
        int RECEIVE_FILE_ACK = 4;
        int SEND_FILE_START = 5;

        int SEND_TCP_ESTABLISHED = 6;
        int SEND_LINK_ERROR = 7;
        int SEND_PERCENTS = 8;
        int SEND_OVER = 9;

        int RECEIVE_TCP_ESTABLISHED = 10;
        int RECEIVE_PERCENT = 11;
        int RECEIVE_OVER = 12;

        int RECEIVE_ABORT_SELF = 13;
        int SEND_ABORT_SELF = 14;

        int ALL_SEND_OVER = 15;
    }

    public interface Src {
        int MANAGER = 90;
        int COMMUNICATE = 91;
        int SEND_TCP_THREAD = 92;
        int RECEIVE_TCP_THREAD = 92;
    }

    public interface Recipient {
        int NEIGHBOR = 100;
        int FILE_SEND = 101;
        int FILE_RECEIVE = 102;
    }
}
