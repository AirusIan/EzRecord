package com.midisheetmusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    /*
    Timer timer = null;
    TimerTask task = null;
    int second = 0;
    int minute = 0;
     */
    boolean status = false;//錄音狀態:false為關閉 true為開啟
//    TextView record_timer;
    Chronometer chronometer_timer;


    MediaRecorder recorder = null;

    private static String fileName = null;
    ImageButton btn_record = null;

    long startRecorderTime = 0;
    long stopRecorderTime = 0;

    boolean record_on = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MidiSheetMusic: Record");
        // Load the list of songs
        setContentView(R.layout.record);

        //檢查權限
        checkPermission();

        ImageButton btn_mic = (ImageButton) findViewById(R.id.btn_mic);
        chronometer_timer = (Chronometer) findViewById(R.id.timer);
//        record_timer = (TextView) findViewById(R.id.record_timer);

        btn_mic.setOnClickListener((press)->{
            if (status == false){
                chronometer_timer.setBase(SystemClock.elapsedRealtime());
                chronometer_timer.start();
                status = true;
            }else{
                chronometer_timer.stop();
                status = false;
            }

        });
        //舊寫法
        /*
        btn_mic.setOnClickListener((start)->{
            if (status == false) {
                timer = new Timer(); //時間函式初始化
                task = new TimerTask(){
                    public void run(){
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                if(second < 60) {
                                    second++; //增加秒數
                                    record_timer.setText(minute+":"+second);
                                }else{
                                    minute++;
                                    second = 0;
                                }
                            }

                        });
                    }
                };
                timer.schedule(task, 1000, 1000);
                status = true;
            }
            else{
                timer.cancel();
                timer = null;
                task.cancel();
                task = null;
                status = false;
                second = 0;
                minute = 0;
                record_timer.setText(minute+":"+second);
            }
        });
  */

        btn_record = (ImageButton) findViewById(R.id.btn_mic);


        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!record_on)
                    doStart();
                else
                    doStop();
            }
        });

    }


    /**
     * 啟動錄音
     *
     * @return
     */
    private boolean doStart() {

        try {
            //建立MediaRecorder
            recorder = new MediaRecorder();
            //建立錄音檔案
            File mRecorderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/recorderdemo/" + System.currentTimeMillis() + ".mp3");
            if (!mRecorderFile.getParentFile().exists()) mRecorderFile.getParentFile().mkdirs();
            mRecorderFile.createNewFile();


            //配置MediaRecorder

            //從麥克風採集
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            //儲存檔案為MP4格式
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            //所有android系統都支援的適中取樣的頻率
            recorder.setAudioSamplingRate(44100);

            //通用的AAC編碼格式
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            //設定音質頻率
            recorder.setAudioEncodingBitRate(96000);

            //設定檔案錄音的位置
            recorder.setOutputFile(mRecorderFile.getAbsolutePath());


            //開始錄音
            recorder.prepare();
            recorder.start();
            startRecorderTime = System.currentTimeMillis();

        } catch (Exception e) {
            Toast.makeText(RecordActivity.this, "錄音失敗，請重試", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        //記錄開始錄音時間，用於統計時長，小於3秒中，錄音不傳送

        record_on = true;
        Toast.makeText(RecordActivity.this, "錄音開始，請開始唱歌", Toast.LENGTH_SHORT).show();

        return true;
    }

    /**
     * 關閉錄音
     *
     * @return
     */
    private boolean doStop() {
        try {
            recorder.stop();
            stopRecorderTime = System.currentTimeMillis();
            final int second = (int) (stopRecorderTime - startRecorderTime) / 1000;
            //按住時間小於3秒鐘，算作錄取失敗，不進行傳送
            if (second < 3) return false;

        } catch (Exception e) {
            e.printStackTrace();
        }

        record_on = false;
        Toast.makeText(RecordActivity.this, "錄音結束，轉檔開始", Toast.LENGTH_SHORT).show();

        return true;
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

}
