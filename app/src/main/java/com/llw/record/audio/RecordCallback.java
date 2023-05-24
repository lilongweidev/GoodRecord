package com.llw.record.audio;

import java.io.File;

public interface RecordCallback {

    void onReadRecordData(byte[] data);

    void onRecordingEnd(File file);

    void onPlayFinish();

    void onFailed(String failed);
}
