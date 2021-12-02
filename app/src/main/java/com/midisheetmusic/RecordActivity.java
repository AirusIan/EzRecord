package com.midisheetmusic;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

//import com.chaquo.python.PyObject;
//import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;
import com.midisheetmusic.ServerConnect.RequestHandle;
import com.midisheetmusic.AudioRecorder.AudioRecordFunc;
import com.midisheetmusic.AudioRecorder.FileUtils;

public class RecordActivity extends AppCompatActivity {

    /*
    Timer timer = null;
    TimerTask task = null;
    int second = 0;
    int minute = 0;
     */
    boolean status = false;//錄音狀態:false為關閉 true為開啟
    boolean isPlay = false;
//    TextView record_timer;
    Chronometer chronometer_timer;


    MediaRecorder recorder = null;

    private String fileName = null;
    ImageButton btn_play = null;
    ImageButton btn_record = null;
    ImageButton btn_finishRecording = null;
    ImageButton btn_reset = null;
    ImageButton btn_delete = null;
    ImageButton btn_music_note = null;

    long startRecorderTime = 0;
    long stopRecorderTime = 0;
    long pauseOffset = 0;
    AudioRecordFunc mAudioRecordFunc;

    ProgressDialog mProgressDialog = null;
    String midiFilePath = "";
    Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioRecordFunc = new AudioRecordFunc(this.getBaseContext());
        setTitle("MidiSheetMusic: Record");
        // Load the list of songs
        setContentView(R.layout.record);

        //檢查權限
        checkPermission();

//        initPython();

        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_record = (ImageButton) findViewById(R.id.btn_mic);
        btn_finishRecording = (ImageButton) findViewById(R.id.btn_check);
        btn_reset = (ImageButton) findViewById(R.id.btn_reset);
        btn_delete = (ImageButton) findViewById(R.id.btn_delete);
        btn_music_note = (ImageButton) findViewById(R.id.btn_music_note);
        chronometer_timer = (Chronometer) findViewById(R.id.timer);

        ImageButton[] FinishRecordUnits = {btn_delete,btn_music_note,btn_play};
        ImageButton[] RecordingUnits = {btn_reset,btn_finishRecording};

        for(ImageButton b:FinishRecordUnits){
            b.setClickable(false);
            b.setVisibility(View.INVISIBLE);
        }
        for(ImageButton btn:RecordingUnits){
            btn.setClickable(false);
            btn.setVisibility(View.INVISIBLE);
        }

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlay = !isPlay;
                if(isPlay) {
                    doPlay();
                }else{
                    doFinishPlaying();
                }
            }
        });

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = !status;
                btn_record.setSelected(!btn_record.isSelected());
                if (btn_record.isSelected()) {
                    for(ImageButton b:FinishRecordUnits){
                        b.setClickable(false);
                        b.setVisibility(View.INVISIBLE);
                    }
                    for(ImageButton btn:RecordingUnits){
                        btn.setClickable(false);
                        btn.setVisibility(View.INVISIBLE);
                    }
                    doStart();
                    chronometer_timer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer_timer.start();
                } else {
                    for(ImageButton btn:RecordingUnits){
                        btn.setClickable(true);
                        btn.setVisibility(View.VISIBLE);
                    }
                    doPause();
                    chronometer_timer.stop();
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer_timer.getBase();
                }
            }
        });

        btn_finishRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ImageButton b:FinishRecordUnits){
                    b.setClickable(true);
                    b.setVisibility(View.VISIBLE);
                }
                for(ImageButton btn:RecordingUnits){
                    btn.setClickable(false);
                    btn.setVisibility(View.INVISIBLE);
                }
                btn_record.setClickable(false);
                btn_finishRecording.setClickable(false);
                btn_finishRecording.setVisibility(View.INVISIBLE);
                btn_record.setSelected(false);
                doStop();
                chronometer_timer.stop();
                pauseOffset = SystemClock.elapsedRealtime() - chronometer_timer.getBase();
            }
        });


        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_record.setSelected(false);
                doReset();
                chronometer_timer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                for(ImageButton b:FinishRecordUnits){
                    b.setClickable(false);
                    b.setVisibility(View.INVISIBLE);
                }
                for(ImageButton btn:RecordingUnits){
                    btn.setClickable(false);
                    btn.setVisibility(View.INVISIBLE);
                }
                btn_record.setVisibility(View.VISIBLE);
                btn_record.setClickable(true);
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDelete();
                chronometer_timer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                for(ImageButton b:FinishRecordUnits){
                    b.setClickable(false);
                    b.setVisibility(View.INVISIBLE);
                }
                for(ImageButton btn:RecordingUnits){
                    btn.setClickable(false);
                    btn.setVisibility(View.INVISIBLE);
                }
                btn_record.setVisibility(View.VISIBLE);
                btn_record.setClickable(true);
            }
        });

        btn_music_note.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createloadDialog();
            String fileBasePath = getBaseContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            String wavFilePath = fileBasePath + "/Ezrecord/WavFile/" + fileName + ".wav";
            midiFilePath = fileBasePath + "/Ezrecord/MidFile/" + fileName + ".mid";
            File file = new File(wavFilePath);
            RequestHandle requestHandle = new RequestHandle();
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 1:
                            File file = new File(midiFilePath);
                            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                            Uri uri = Uri.parse("file://" + midiFilePath);
                            FileUri fileuri = new FileUri(uri, uri.getLastPathSegment());
                            doOpenFile(fileuri);
                            mProgressDialog.cancel();
                            break;
                        case 2:
                            mProgressDialog.cancel();
                            ChooseSongActivity.showErrorDialog("請檢查您的麥克風是否有開啟", RecordActivity.this);
                            this.removeCallbacksAndMessages(null);
                            break;
                        case 3:
                            mProgressDialog.cancel();
                            ChooseSongActivity.showErrorDialog("網路傳輸出現問題，請再重錄一次", RecordActivity.this);
                            this.removeCallbacksAndMessages(null);
                            break;
                        case 4:
                            mProgressDialog.cancel();
                            ChooseSongActivity.showErrorDialog("檔案寫入出現問題", RecordActivity.this);
                            this.removeCallbacksAndMessages(null);
                            break;
                        default:
                            break;
                    }
                }
            };
            requestHandle.run_wav2midi(file,midiFilePath,handler);
        }});
    }


    private boolean doPlay(){
        try {
            mAudioRecordFunc.play(FileUtils.getWavFileAbsolutePath(fileName,getBaseContext()));
        }catch (Exception e){
            Toast.makeText(RecordActivity.this, "播放失敗", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean doFinishPlaying(){
        try {
            mAudioRecordFunc.releaseAudioTrack();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        Toast.makeText(RecordActivity.this, "播放結束", Toast.LENGTH_SHORT).show();
        return true;
    }
    /**
     * 啟動錄音
     *
     * @return
     */
    private boolean doStart() {

        try {
            startRecorderTime = System.currentTimeMillis();
            mAudioRecordFunc.startRecordAndFile();

        } catch (Exception e) {
            Toast.makeText(RecordActivity.this, "錄音失敗，請重試", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        //記錄開始錄音時間，用於統計時長，小於3秒中，錄音不傳送

//        status = true;
        Toast.makeText(RecordActivity.this, "錄音開始，請開始唱歌", Toast.LENGTH_SHORT).show();

        return true;
    }

    /**
     * 關閉錄音
     *
     * @return
     */
    private boolean doPause() {
        try {
            final int second = (int) (stopRecorderTime - startRecorderTime) / 1000;
//            //按住時間小於3秒鐘，算作錄取失敗，不進行傳送
//            if (second < 3) {
//                Toast.makeText(RecordActivity.this, "錄音失敗(未超過3秒)，請重試", Toast.LENGTH_SHORT).show();
//                status = false;
//                return true;
//            }
            mAudioRecordFunc.pauseRecord();
            stopRecorderTime = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        status = false;
        Toast.makeText(RecordActivity.this, "錄音暫停 ", Toast.LENGTH_SHORT).show();

        return true;
    }

    private boolean doStop() {
        try {
            mAudioRecordFunc.stopRecord();
            fileName = mAudioRecordFunc.getFileName();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        status = false;
        Toast.makeText(RecordActivity.this, "錄音結束", Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean doReset() {
        try {
            mAudioRecordFunc.resetRecord();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        status = false;
        Toast.makeText(RecordActivity.this, "捨棄錄音檔", Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean doDelete(){
        try {
            mAudioRecordFunc.deleteRecord();
        } catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(RecordActivity.this, "刪除錄音檔", Toast.LENGTH_SHORT).show();

        return true;
    }

    public void doOpenFile(FileUri file) {
        byte[] data = file.getData(this);
        if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
            ChooseSongActivity.showErrorDialog("Error: Unable to open song: " + file.toString(), this);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, file.getUri(), this, SheetMusicActivity.class);
        intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());
        handler.removeCallbacksAndMessages(null);
        startActivity(intent);
    }


    /**
     * 权限申请
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == 200) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            checkPermission();
        }
    }

    private void createloadDialog(){
        mProgressDialog = new ProgressDialog(RecordActivity.this);
        // 设置进度条的形式为圆形转动的进度条
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置是否可以通过点击Back键取消
        mProgressDialog.setCancelable(false);
        // 设置在点击Dialog外是否取消Dialog进度条
        mProgressDialog.setCanceledOnTouchOutside(false);
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle("轉檔進度");
        mProgressDialog.setMessage("轉檔中，請燒等...");
        mProgressDialog.show();
    }

}
