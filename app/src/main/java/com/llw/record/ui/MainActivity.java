package com.llw.record.ui;

import static com.llw.record.audio.Constant.CHANNEL_IN_MONO;
import static com.llw.record.audio.Constant.CHANNEL_IN_STEREO;
import static com.llw.record.audio.Constant.PCM_16BIT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.llw.record.R;
import com.llw.record.RecordApp;
import com.llw.record.audio.EasyFormat;
import com.llw.record.audio.RecordCallback;
import com.llw.record.audio.RecordCore;
import com.llw.record.basic.EasyActivity;
import com.llw.record.databinding.ActivityMainBinding;
import com.llw.record.databinding.DialogFileBinding;
import com.llw.record.databinding.DialogFormatBinding;
import com.llw.record.ui.adapter.DataAdapter;
import com.llw.record.ui.adapter.FileAdapter;
import com.llw.record.ui.adapter.FormatAdapter;
import com.llw.record.utils.FileUtils;
import com.llw.record.utils.HexUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends EasyActivity<ActivityMainBinding> implements View.OnClickListener, RecordCallback, AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //请求权限
    private ActivityResultLauncher<String[]> requestPermissions;

    private RecordCore mCore;

    private Integer[] sampleRateArray = {8000, 16000, 24000, 44100, 48000};

    private Integer[] channelArray = {1, 2};

    private String[] encodingArray = {"PCM_16BIT"};

    private List<String> mList = new ArrayList<>();
    private DataAdapter mAdapter;

    private final List<File> fileList = new ArrayList<>();

    //采样率
    private int mSampleRate = sampleRateArray[0];
    //通道
    private int mChannel = 1;
    //编码
    private int mEncodingBit = PCM_16BIT;

    @Override
    protected void onRegister() {
        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Log.d(TAG, "权限返回");
        });
    }

    @Override
    protected void onCreate() {
        mCore = RecordApp.getRecordCore();
        //读取数据回调监听
        mCore.setCallback(this);
        //初始化视图
        initView();
    }

    private void initView() {
        setSpinner(binding.spSampleRate, new ArrayAdapter<>(this, R.layout.item_select, sampleRateArray), "请选择采样率");
        setSpinner(binding.spChannel, new ArrayAdapter<>(this, R.layout.item_select, channelArray), "请选择通道");
        setSpinner(binding.spEncoding, new ArrayAdapter<>(this, R.layout.item_select, encodingArray), "请选择编码");
//        initSpinner();
        binding.tvFileInfo.setOnClickListener(this);
        binding.btnRecord.setOnClickListener(this);
        binding.btnPlay.setOnClickListener(this);
        //数据列表初始化
        mAdapter = new DataAdapter(mList);
        mAdapter.setOnItemClickListener((view, position) -> showMsg(mList.get(position)));
        ((SimpleItemAnimator) binding.rvData.getItemAnimator()).setSupportsChangeAnimations(false);
        binding.rvData.setLayoutManager(new LinearLayoutManager(this));
        binding.rvData.setAdapter(mAdapter);
        //刷新
        refreshFile();
    }

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        ArrayAdapter<Integer> sampleRateAdapter = new ArrayAdapter<>(this, R.layout.item_select, sampleRateArray);
        //设置数组适配器的布局样式
        sampleRateAdapter.setDropDownViewResource(R.layout.item_dropdown);
        //设置下拉框的标题，不设置就没有难看的标题了
        binding.spSampleRate.setPrompt("请选择采样率");
        //设置下拉框的数组适配器
        binding.spSampleRate.setAdapter(sampleRateAdapter);
        //设置下拉框默认的显示第一项
        binding.spSampleRate.setSelection(0);
        binding.spSampleRate.setOnItemSelectedListener(this);

        ArrayAdapter<Integer> channelAdapter = new ArrayAdapter<>(this, R.layout.item_select, channelArray);
        sampleRateAdapter.setDropDownViewResource(R.layout.item_dropdown);
        binding.spChannel.setPrompt("请选择通道");
        binding.spChannel.setAdapter(channelAdapter);
        binding.spChannel.setSelection(0);
        binding.spChannel.setOnItemSelectedListener(this);

        ArrayAdapter<String> encodingAdapter = new ArrayAdapter<>(this, R.layout.item_select, encodingArray);
        sampleRateAdapter.setDropDownViewResource(R.layout.item_dropdown);
        binding.spEncoding.setPrompt("请选择编码");
        binding.spEncoding.setAdapter(encodingAdapter);
        binding.spEncoding.setSelection(0);
        binding.spEncoding.setOnItemSelectedListener(this);
    }

    private void setSpinner(Spinner spinner, ArrayAdapter adapter, String title) {
        //设置数组适配器的布局样式
        adapter.setDropDownViewResource(R.layout.item_dropdown);
        //设置下拉框的标题，不设置就没有难看的标题了
        spinner.setPrompt(title);
        //设置下拉框的数组适配器
        spinner.setAdapter(adapter);
        //设置下拉框默认的显示第一项
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);
    }

    private void refreshFile() {
        //获取本地的录音文件
        List<File> localFiles = FileUtils.getLocalFiles();
        if (localFiles == null) return;
        if (localFiles.size() <= 0) return;
        //添加所有文件
        fileList.clear();
        fileList.addAll(localFiles);
        //设置第一个
        setFile(fileList.get(0));
    }


    /**
     * 设置录音文件
     */
    private void setFile(File file) {
        binding.tvFileInfo.setText(String.format(Locale.getDefault(), "路径：%s\n大小：%d Bytes", file.getPath(), file.length()));
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.tv_format:
//                showFormatDialog();
//                break;
            case R.id.tv_file_info:
                refreshFile();
                showFileDialog();
                break;
            case R.id.btn_record:
                //检查权限
                if (!checkPermission()) {
                    return;
                }
                //播放时不能录音
                if (mCore.isPlaying()) {
                    showMsg("播放时不能录音");
                    return;
                }

                if (mCore.isRecording()) stopRecord();
                else startRecord();
                break;
            case R.id.btn_play:
                if (!checkPermission()) {
                    return;
                }
                //录音时不能播放
                if (mCore.isRecording()) {
                    showMsg("录音时不能播放");
                    return;
                }
                if (mCore.isPlaying()) stopPlay();
                else startPlay();
                break;
        }
    }

    private boolean checkPermission() {
        List<String> permissions = new ArrayList<>();
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            //创建文件夹
            FileUtils.createFolder();
        }
        if (permissions.size() == 0) {
            return true;
        } else {
            String[] permissionArray = permissions.toArray(new String[permissions.size()]);
            requestPermissions.launch(permissionArray);
            return false;
        }
    }

    /**
     * 显示文件弹窗
     */
    private void showFileDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogFileBinding fileBinding = DialogFileBinding.inflate(LayoutInflater.from(this));
        fileBinding.toolbar.setNavigationOnClickListener(v -> dialog.dismiss());
        FileAdapter fileAdapter = new FileAdapter(fileList);
        fileAdapter.setOnItemClickListener((view, position) -> {
            setFile(fileList.get(position));
            dialog.dismiss();
        });
        fileAdapter.setOnItemChildClickListener((view, position) -> {
            File file = fileList.get(position);
            if (file.isFile()) {
                if (file.delete()) {
                    fileList.remove(position);
                    fileAdapter.notifyItemRemoved(position);
                    showMsg("删除成功");
                } else {
                    showMsg("删除失败");
                }
            }
        });
        fileBinding.rvFile.setLayoutManager(new LinearLayoutManager(this));
        fileBinding.rvFile.setAdapter(fileAdapter);
        dialog.setContentView(fileBinding.getRoot());
        dialog.show();
    }

    private void startPlay() {
        if (!mCore.isPlaying()) {
            binding.btnPlay.setText("Stop Play");
            binding.btnPlay.setBackgroundColor(getColor(R.color.purple_200));
            showMsg("开始播放");
            //开始播放
            mCore.startPlay();
        }
    }

    private void stopPlay() {
        if (mCore.isPlaying()) {
            binding.btnPlay.setText("Play");
            binding.btnPlay.setBackgroundColor(getColor(R.color.purple_500));
            showMsg("播放已停止");
            //停止播放
            mCore.stopPlay();
        }
    }

    private void startRecord() {
        if (!mCore.isRecording()) {
            binding.btnRecord.setText("Stop Record");
            binding.btnRecord.setBackgroundColor(getColor(R.color.purple_200));
            showMsg("开始录制");
            //开始录制
            mCore.startRecord(mSampleRate, mChannel, mEncodingBit);
        }
    }

    private void stopRecord() {
        if (mCore.isRecording()) {
            binding.btnRecord.setText("Record");
            binding.btnRecord.setBackgroundColor(getColor(R.color.purple_500));
            showMsg("录制已停止");
            //停止录制
            mCore.stopRecord();
        }
    }

    @Override
    public void onReadRecordData(byte[] data) {
        final String dataHex = HexUtils.bytesToHexStringFormat(data);
        Log.d(TAG, "RecordData: " + dataHex);
        runOnUiThread(() -> {
            mList.add("Size: " + data.length + " " + dataHex);
            int position = mList.size() - 1;
            mAdapter.notifyItemChanged(position);
            binding.rvData.smoothScrollToPosition(position);
        });
    }

    @Override
    public void onRecordingEnd(File file) {
        runOnUiThread(() -> {
            if (file != null) {
                mList.add(file.getPath() + "\n " + file.length() + " Bytes");
                int position = mList.size() - 1;
                mAdapter.notifyItemChanged(position);
                binding.rvData.smoothScrollToPosition(position);
                //刷新文件
                refreshFile();
            }
        });
    }

    @Override
    public void onPlayFinish() {
        runOnUiThread(() -> {
            showMsg("播放完成");
            binding.btnPlay.setText("Play");
            binding.btnPlay.setBackgroundColor(getColor(R.color.purple_500));
        });
    }

    @Override
    public void onFailed(String failed) {
        Log.d(TAG, "onFailed: " + failed);
        showMsg(failed);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (view.getId()) {
            case R.id.sp_sample_rate:
                mSampleRate = sampleRateArray[position];
                break;
            case R.id.sp_channel:
                mChannel = channelArray[position];
                break;
            case R.id.sp_encoding:
                mEncodingBit = PCM_16BIT;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}