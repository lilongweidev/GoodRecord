package com.llw.record.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    //录制文件所在文件夹
    public static final String FOLDER_NAME = "GoodRecord";
    /**
     * 接收每一次录音的数据 ，文件夹中的数据以.pcm格式保存，以时间命名。
     */
    public static final String FOLDER_PCM = "PcmFile";

    @SuppressLint("StaticFieldLeak")
    private static volatile FileUtils mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private static FileOutputStream fos = null;
    private static String mPcmFilePath;

    private static File mFile;

    public FileUtils(Context context) {
        mContext = context;
    }

    public static FileUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FileUtils.class) {
                if (mInstance == null) {
                    mInstance = new FileUtils(context);
                }
            }
        }
        return mInstance;
    }

    public static File getFile() {
        return mFile;
    }

    public static void setFile(File mFile) {
        FileUtils.mFile = mFile;
    }

    /**
     * 创建文件夹，在获取文件读写权限之后进行
     */
    public static void createFolder() {
        String recordFolderPath = getLocalPath() + "/" + FOLDER_NAME + "/" + FOLDER_PCM;
        File receiveFolder = new File(recordFolderPath);
        if (!receiveFolder.exists()) {
            receiveFolder.mkdirs();//创建文件夹
        }
    }

    /**
     * 获取本地路径
     */
    @NonNull
    private static String getLocalPath() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                mContext.getExternalFilesDir(null).getAbsolutePath() : Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取当前日期和时间
     */
    public static String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date());
    }

    /**
     * 保存录音数据
     *
     * @param bytes
     * @param length
     */
    public static void saveData(byte[] bytes, int length) {
        if (mFile == null) {
            mPcmFilePath = getLocalPath() + "/" + FOLDER_NAME + "/" + FOLDER_PCM + "/" + getDateTime() + ".pcm";
            mFile = new File(mPcmFilePath);
            if (!mFile.exists()) {
                try {
                    mFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            //创建文件
            fos = new FileOutputStream(mFile, true);
            fos.write(bytes, 0, length);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "saveData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取本地录音文件
     */
    public static List<File> getLocalFiles() {
        List<File> fileList = new ArrayList<>();
        String recordFolderPath = getLocalPath() + "/" + FOLDER_NAME + "/" + FOLDER_PCM;
        File file = new File(recordFolderPath);
        if (file.exists()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                Collections.addAll(fileList, listFiles);
                //按文件创建时间排序
                Collections.sort(fileList, new FileComparator());
            } else {
                Log.e(TAG, "本地文件夹无法打开");
                return null;
            }
        } else {
            Log.e(TAG, "无法获取文件");
            return null;
        }
        return fileList;
    }

    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            if (file1.lastModified() > file2.lastModified()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
