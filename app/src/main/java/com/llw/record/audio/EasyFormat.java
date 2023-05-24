package com.llw.record.audio;

/**
 * 音频格式
 */
public class EasyFormat {

    /**
     * 采样率
     */
    private int sampleRate;
    /**
     * 通道
     */
    private int channel;
    /**
     * 编码格式
     */
    private int encoding;

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getEncoding() {
        return encoding;
    }

    public void setEncoding(int encoding) {
        this.encoding = encoding;
    }

    public EasyFormat(int sampleRate, int channel, int encoding) {
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.encoding = encoding;
    }
}
