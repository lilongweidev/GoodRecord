package com.llw.record.audio;

import android.media.AudioFormat;

public class Constant {

    /**
     * 采样率 - 8k
     */
    public static final int SAMPLE_RATE_8_BIT = 8000;
    /**
     * 采样率 - 16k
     */
    public static final int SAMPLE_RATE_16_BIT = 16000;
    /**
     * 采样率 - 24k
     */
    public static final int SAMPLE_RATE_24_BIT = 24000;
    /**
     * 采样率 - 41k
     */
    public static final int SAMPLE_RATE_41_BIT = 44100;
    /**
     * 采样率 - 48k
     */
    public static final int SAMPLE_RATE_48_BIT = 48000;

    /**
     * 通道 - 输入单声道
     */
    public static final int CHANNEL_IN_MONO = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 通道 - 输入立体声（双声道）
     */
    public static final int CHANNEL_IN_STEREO = AudioFormat.CHANNEL_IN_STEREO;

    /**
     * 通道 - 输入单声道
     */
    public static final int CHANNEL_OUT_MONO = AudioFormat.CHANNEL_OUT_MONO;
    /**
     * 通道 - 输入立体声（双声道）
     */
    public static final int CHANNEL_OUT_STEREO = AudioFormat.CHANNEL_OUT_STEREO;

    /**
     * 编码格式 - pcm 8bit
     */
    public static final int PCM_8BIT = AudioFormat.ENCODING_PCM_8BIT;
    /**
     * 编码格式 - pcm 16bit
     */
    public static final int PCM_16BIT = AudioFormat.ENCODING_PCM_16BIT;
}
