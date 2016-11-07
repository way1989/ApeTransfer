package com.ape.backuprestore.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author way
 */
public class BackupZip {
    private static final String TAG = "BackupZip";
    ZipOutputStream mOutZip;
    private String mZipFile;
    private Boolean hasFinished = false;

    /**
     * @param zipfile
     * @throws IOException
     */
    public BackupZip(String zipfile) throws IOException {
        createZipFile(zipfile);
        mZipFile = zipfile;
    }

    /**
     * @param zipFileString  zipFileString
     * @param bContainFolder bContainFolder
     * @param bContainFile   bContainFile
     * @return List<String> List<String>
     * @throws IOException IOException
     */
    public static List<String> getFileList(String zipFileString, boolean bContainFolder,
                                           boolean bContainFile) throws IOException {

        Logger.i(TAG, "GetFileList");

        List<String> fileList = new ArrayList<>();
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;

        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            Logger.d(TAG, szName);

            if (zipEntry.isDirectory()) {

                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                if (bContainFolder) {
                    fileList.add(szName);
                }

            } else {
                if (bContainFile) {
                    fileList.add(szName);
                }
            }
        }

        inZip.close();
        if (fileList.size() > 0) {
            Collections.sort(fileList);
            Collections.reverse(fileList);
        }
        return fileList;
    }

    /**
     * @param zipFileString  zipFileString
     * @param bContainFolder bContainFolder
     * @param bContainFile
     * @param tmpString
     * @return List<String>.
     * @throws IOException
     */
    public static List<String> getFileList(String zipFileString, boolean bContainFolder,
                                           boolean bContainFile, String tmpString) throws IOException {

        Logger.i(TAG, "GetFileList");

        List<String> fileList = new ArrayList<>();

        ZipFile zf = new ZipFile(zipFileString);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        ZipEntry zipEntry;
        String szName;
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement();
            szName = zipEntry.getName();
            Logger.d(TAG, szName + ", zipEntry.isDirectory() = " + zipEntry.isDirectory());

            if (zipEntry.isDirectory()) {

                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                if (bContainFolder) {
                    if (tmpString == null) {
                        fileList.add(szName);
                    } else if (szName.matches(tmpString)) {
                        fileList.add(szName);
                    }
                }

            } else {
                if (bContainFile) {
                    if (tmpString == null) {
                        fileList.add(szName);
                    } else if (szName.matches(tmpString)) {
                        fileList.add(szName);
                    }
                }
            }
        }

        zf.close();
        if (fileList.size() > 0) {
            Collections.sort(fileList);
            Collections.reverse(fileList);
        }
        return fileList;
    }

    /**
     * @param zipFileString
     * @param fileString
     * @return
     */
    public static String readFile(String zipFileString, String fileString) {
        Logger.i(TAG, "getFile");
        ByteArrayOutputStream baos;
        String content = null;
        try {
            ZipFile zipFile = new ZipFile(zipFileString);
            ZipEntry zipEntry = zipFile.getEntry(fileString);
            if (zipEntry != null) {
                InputStream is = zipFile.getInputStream(zipEntry);
                baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[512];
                while ((len = is.read(buffer, 0, 512)) != -1) {
                    baos.write(buffer, 0, len);
                }
                content = baos.toString();
                is.close();
            }

            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return content;
    }

    /**
     * @param zipFileString
     * @param fileString
     * @return
     */
    public static byte[] readFileContent(String zipFileString, String fileString) {
        Logger.i(TAG, "getFile");
        ByteArrayOutputStream baos = null;
        try {
            ZipFile zipFile = new ZipFile(zipFileString);
            ZipEntry zipEntry = zipFile.getEntry(fileString);
            if (zipEntry != null) {
                InputStream is = zipFile.getInputStream(zipEntry);
                baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[512];
                while ((len = is.read(buffer, 0, 512)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
            }

            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (baos != null) {
            return baos.toByteArray();
        }
        return null;
    }

    /**
     * @param zipFileName
     * @return
     * @throws IOException
     */
    public static ZipFile getZipFileFromFileName(String zipFileName) throws IOException {
        return new ZipFile(zipFileName);
    }

    /**
     * @param zipFile
     * @param fileString
     * @return
     */
    public static String readFile(ZipFile zipFile, String fileString) {
        Logger.i(TAG, "getFile");
        ByteArrayOutputStream baos;
        String content = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry(fileString);
            if (zipEntry != null) {
                InputStream is = zipFile.getInputStream(zipEntry);
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buffer = new byte[512];
                while ((len = is.read(buffer, 0, 512)) != -1) {
                    baos.write(buffer, 0, len);
                }
                content = baos.toString();
                is.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return content;
    }

    /**
     * @param zipFile
     * @param fileString
     * @return
     */
    public static byte[] readFileContent(ZipFile zipFile, String fileString) {
        Logger.i(TAG, "getFile");
        ByteArrayOutputStream baos = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry(fileString);
            if (zipEntry != null) {
                InputStream is = zipFile.getInputStream(zipEntry);
                baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[512];
                while ((len = is.read(buffer, 0, 512)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return baos == null ? null : baos.toByteArray();
    }

    /**
     * @param zipFileName
     * @param srcFileName
     * @param destFileName
     * @throws IOException
     */
    public static void unZipFile(String zipFileName, String srcFileName, String destFileName)
            throws IOException {
        File destFile = new File(destFileName);
        if (!destFile.exists()) {
            File tmpDir = destFile.getParentFile();
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            destFile.createNewFile();
        }

        try (FileOutputStream out = new FileOutputStream(destFile)) {
            ZipFile zipFile = new ZipFile(zipFileName);
            ZipEntry zipEntry = zipFile.getEntry(srcFileName);
            if (zipEntry != null) {
                InputStream is = zipFile.getInputStream(zipEntry);
                int len;
                byte[] buffer = new byte[512];
                while ((len = is.read(buffer, 0, 512)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }

                is.close();
            }
            zipFile.close();
        } catch (IOException e) {
            // e.printStackTrace();
            throw e;
        }
    }

    /**
     * @param zipFileString zipFileString
     * @param outPathString outPathString
     * @throws IOException IOException
     */
    public static void unZipFolder(String zipFileString, String outPathString) throws IOException {
        // Logger.i(TAG, "UnZipFolder(String, String)");
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;

        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();

            if (zipEntry.isDirectory()) {

                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();

            } else {

                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[512];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        } // end of while

        inZip.close();

    } // end of func

    /**
     * @param srcFileString
     * @param zipFileString
     * @throws IOException
     */
    public static void zipFolder(String srcFileString, String zipFileString) throws IOException {
        Logger.i(TAG, "zipFolder(String, String)");

        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));

        File file = new File(srcFileString);

        zipFiles(file.getParent() + File.separator, file.getName(), outZip);

        outZip.finish();
        outZip.close();

    } // end of func

    private static void zipFiles(String folderString, String fileString,
                                 ZipOutputStream zipOutputSteam) throws IOException {
        Logger.i(TAG, "ZipFiles(String, String, ZipOutputStream)");

        if (zipOutputSteam == null) {
            return;
        }

        File file = new File(folderString + fileString);

        if (file.isFile()) {

            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[1024];

            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }

            zipOutputSteam.closeEntry();
            inputStream.close();
        } else {

            String fileList[] = file.list();

            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }

            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam);
            } // end of for

        } // end of if

    } // end of func

    /**
     * @param srcFileString
     * @param zipFileString
     * @throws IOException
     */
    public static void zipOneFile(String srcFileString, String zipFileString) throws IOException {
        Logger.i(TAG, "zipFolder(String, String)");

        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));

        File file = new File(srcFileString);

        zipFiles(file.getParent() + File.separator, file.getName(), outZip);

        outZip.finish();
        outZip.close();
    }

    public String getZipFileName() {
        return mZipFile;
    }

    /**
     * @param zipFileString
     * @throws IOException
     */
    public void createZipFile(String zipFileString) throws IOException {
        mOutZip = new ZipOutputStream(new FileOutputStream(zipFileString));
        Logger.i(TAG, "createZipFile zipFileString = " + zipFileString);
    }

    /**
     * @param fileName
     * @param fileContent
     * @throws IOException
     */
    public void addFile(String fileName, String fileContent) throws IOException {
        if (fileContent != null && fileContent.length() > 0) {
            addFile(fileName, fileContent.getBytes());
        }
    }

    /**
     * @param fileName
     * @param fileContent
     * @throws IOException
     */
    public void addFile(String fileName, byte[] fileContent) throws IOException {
        if (fileContent != null && fileContent.length > 0) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            mOutZip.putNextEntry(zipEntry);
            mOutZip.write(fileContent, 0, fileContent.length);
            mOutZip.closeEntry();
        }
    }

    /**
     * @param srcFileName
     * @param desFileName
     * @throws IOException
     */
    public void addFileByFileName(String srcFileName, String desFileName) throws IOException {
        Logger.d(TAG, "addFileByFileName:" + "srcFile:" + srcFileName + ",desFile:"
                + desFileName);

        ZipEntry zipEntry = new ZipEntry(desFileName);
        File file = new File(srcFileName);
        FileInputStream inputStream = new FileInputStream(file);
        mOutZip.putNextEntry(zipEntry);

        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            mOutZip.write(buffer, 0, len);
        }

        inputStream.close();
    }

    /**
     * @param srcPath
     * @param desPath
     * @throws IOException
     */
    public void addFolder(String srcPath, String desPath) throws IOException {
        File dir = new File(srcPath);

        // Log.d("BACKUP", "addFolder," +
        // "srcFile:" + srcPath +
        // ",desFile:" + desPath +
        // ",dir.isDirectory():" + dir.isDirectory());

        if (dir.isDirectory()) {
            File[] fileArray = dir.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    addFolder(srcPath + File.separator + file.getName(), desPath + File.separator
                            + file.getName());
                }
            } else {
                // Log.d("BACKUP", "addFolder, empty folder:" + srcPath);
            }

        } else {
            try {
                addFileByFileName(srcPath, desPath);
            } catch (IOException e) {
                Logger.e(TAG, "IOException in addFolder");
            }
        }
    }

    /**
     * @throws IOException
     */
    public void finish() throws IOException {
        if (!hasFinished) {
            Logger.e(TAG, "Finish");
            hasFinished = true;
            mOutZip.finish();
            mOutZip.close();
        } else {
            Logger.e(TAG, "Already finish, do nothing");
        }
    }
}