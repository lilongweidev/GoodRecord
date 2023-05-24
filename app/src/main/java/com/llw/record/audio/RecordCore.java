package com.llw.record.audio;

import static com.llw.record.audio.Constant.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.llw.record.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecordCore {

    public static final String TAG = RecordCore.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static volatile RecordCore mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    //音频格式集合
    private List<EasyFormat> easyFormats;
    //是否录制
    private boolean isRecording;
    //是否播放
    private boolean isPlaying;

    private int poolSize = 50;
    // 创建线程池对象，设置核心线程和最大线程数为2  , 主要就是录制 和 播放
    private final ExecutorService recordThreadPool = Executors.newScheduledThreadPool(poolSize);
    private final ExecutorService playThreadPool = Executors.newScheduledThreadPool(poolSize);
    private Future<?> playFuture;
    private Future<?> recordFuture;
    //录音
    private AudioRecord mAudioRecord;
    //播音
    private AudioTrack mAudioTrack;
    //播放大小
    private int playSize;
    //采样率
    private int mSampleRate;
    //输入通道
    private int mInChannel;
    //输出通道
    private int mOutChannel;
    //编码
    private int mEncodingBit;
    //数据回调
    private RecordCallback mCallback;

    public RecordCore(Context context) {
        mContext = context;
    }

    public static RecordCore getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RecordCore.class) {
                if (mInstance == null) {
                    mInstance = new RecordCore(context);
                }
            }
        }
        return mInstance;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setCallback(RecordCallback mCallback) {
        this.mCallback = mCallback;
    }

    /**
     * 获取默认的音频格式列表  未用
     */
    public List<EasyFormat> getAudioFormats() {
        if (easyFormats == null) {
            easyFormats = new ArrayList<>();
            //easyFormats.add(new EasyFormat(SAMPLE_RATE_8_BIT, CHANNEL_IN_MONO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_8_BIT, CHANNEL_IN_STEREO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_16_BIT, CHANNEL_IN_MONO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_16_BIT, CHANNEL_IN_STEREO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_24_BIT, CHANNEL_IN_MONO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_24_BIT, CHANNEL_IN_STEREO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_41_BIT, CHANNEL_IN_MONO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_41_BIT, CHANNEL_IN_STEREO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_48_BIT, CHANNEL_IN_MONO, PCM_16BIT));
            easyFormats.add(new EasyFormat(SAMPLE_RATE_48_BIT, CHANNEL_IN_STEREO, PCM_16BIT));
        }
        return easyFormats;
    }

    public void startPlay() {
        isPlaying = true;
        playFuture = playThreadPool.submit(playTask);
    }

    public void stopPlay() {
        isPlaying = false;
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
        if (playFuture != null) {
            if (!playFuture.isCancelled()) {
                playFuture.cancel(true);
            }
        }
//        //中断所有没有正在执行任务的线程
//        playThreadPool.shutdown();
    }

    /**
     * 开始录制
     */
    public void startRecord(int sampleRate, int channel, int encodingBit) {
        mSampleRate = sampleRate;
        mInChannel = channel == 1 ? CHANNEL_IN_MONO : CHANNEL_IN_STEREO;
        mOutChannel = channel == 1 ? CHANNEL_OUT_MONO : CHANNEL_OUT_STEREO;
        mEncodingBit = encodingBit;

        isRecording = true;
        FileUtils.setFile(null);
        //执行录制音频任务
        recordFuture = recordThreadPool.submit(recordTask);
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        isRecording = false;
        if (mAudioRecord != null) {
            if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                mAudioRecord.stop();
            }
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                mAudioRecord.release();
            }
            mAudioRecord = null;
        }
        if (recordFuture != null) {
            if (!recordFuture.isCancelled()) {
                recordFuture.cancel(true);
            }
        }
        //中断所有没有正在执行任务的线程
        //recordThreadPool.shutdown();
        //停止录制时返回文件地址
        if (mCallback != null) {
            mCallback.onRecordingEnd(FileUtils.getFile());
        }
    }

    private final Runnable playTask = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            // 获取播放最小缓冲区大小
            int bufferSize = AudioTrack.getMinBufferSize(mSampleRate, mOutChannel, mEncodingBit);
            playSize = bufferSize * 2;
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mOutChannel, mEncodingBit, bufferSize, AudioTrack.MODE_STREAM);

            File file = FileUtils.getFile();
            if (file == null) {
                if (mCallback != null) {
                    mCallback.onFailed("播放文件为空");
                    return;
                }
            }

            byte[] data = new byte[playSize];
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                int total = fileInputStream.available();
                int offset = 0;
                mAudioTrack.play();
                while (isPlaying) {
                    int len = fileInputStream.read(data, 0, playSize);
                    if (len <= 0) {
                        break;
                    }
                    mAudioTrack.write(data, 0, playSize);
                    offset += len;
                    if (offset >= total) {
                        break;
                    }
                }
                if (mCallback != null) {
                    mCallback.onPlayFinish();
                }
                stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /**
     * 创建录制音频任务
     */
    private final Runnable recordTask = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            // 获取录制最小缓冲区大小
            int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mInChannel, mEncodingBit);
            Log.e("==", " " + bufferSize + " " + mSampleRate + " " + mInChannel + " " + mEncodingBit);
            //实例化之前确保动态权限已申请，否者会崩溃
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, mInChannel, mEncodingBit, bufferSize);
            //开始录制
            mAudioRecord.startRecording();
            //分配缓存大小
            ByteBuffer dataBuffer = ByteBuffer.allocate(bufferSize);
            while (isRecording) {
                dataBuffer.clear();
                dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                if (readData(dataBuffer.array(), bufferSize)) {
                    //数据回调
                    if (mCallback != null) {
                        mCallback.onReadRecordData(dataBuffer.array());
                    }
                    //保存数据
                    FileUtils.saveData(dataBuffer.array(), bufferSize);
                }
            }
        }

        private boolean readData(byte[] data, int size) {
            int readSize = 0;
            while (isRecording) {
                int thisReadSize = mAudioRecord.read(data, readSize, size - readSize);
                if (thisReadSize > 0) {
                    readSize += thisReadSize;
                } else {
                    break;
                }
                if (readSize == size) {
                    return true;
                } else if (readSize > size) {
                    break;
                }
            }
            return false;
        }
    };
}
