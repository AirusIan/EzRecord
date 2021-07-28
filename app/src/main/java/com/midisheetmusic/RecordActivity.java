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
import android.media.AudioFormat;
import android.media.AudioRecord;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.midisheetmusic.AudioRecorder.AudioRecordFunc;
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

    private String fileName = null;
    ImageButton btn_record = null;

    long startRecorderTime = 0;
    long stopRecorderTime = 0;
    AudioRecordFunc mAudioRecordFunc = new AudioRecordFunc();

    String newWavFile = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MidiSheetMusic: Record");
        // Load the list of songs
        setContentView(R.layout.record);

        //檢查權限
        checkPermission();

        initPython();

        btn_record = (ImageButton) findViewById(R.id.btn_mic);
        chronometer_timer = (Chronometer) findViewById(R.id.timer);

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!status) {
                    doStart();
                    chronometer_timer.setBase(SystemClock.elapsedRealtime());
                    chronometer_timer.start();
                }
                else {
                    doStop();
                    chronometer_timer.stop();
                }
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
            startRecorderTime = System.currentTimeMillis();
            mAudioRecordFunc.startRecordAndFile();

        } catch (Exception e) {
            Toast.makeText(RecordActivity.this, "錄音失敗，請重試", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        //記錄開始錄音時間，用於統計時長，小於3秒中，錄音不傳送

        status = true;
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
            final int second = (int) (stopRecorderTime - startRecorderTime) / 1000;
//            //按住時間小於3秒鐘，算作錄取失敗，不進行傳送
//            if (second < 3) {
//                Toast.makeText(RecordActivity.this, "錄音失敗(未超過3秒)，請重試", Toast.LENGTH_SHORT).show();
//                status = false;
//                return true;
//            }
            fileName = mAudioRecordFunc.stopRecordAndFile();
            stopRecorderTime = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }

        status = false;
        Toast.makeText(RecordActivity.this, "錄音結束，轉檔開始", Toast.LENGTH_SHORT).show();

        String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String wavFilePath = fileBasePath+ "/recorderdemo/WavFile/" + fileName + ".wav";
        String midiFilePath = fileBasePath+ "/recorderdemo/MidiFile/" + fileName + ".mid";

        File file = new File(midiFilePath);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        callAudio2midi(wavFilePath, midiFilePath);

        Uri uri = Uri.parse("file://" + midiFilePath);
        FileUri fileuri = new FileUri(uri, uri.getLastPathSegment());

        doOpenFile(fileuri);

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


        startActivity(intent);
    }

    // 初始化python
    private void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    private void callAudio2midi(String file_in, String file_out){
        try {
            Toast.makeText(RecordActivity.this, "轉檔開始", Toast.LENGTH_SHORT).show();
            Python py = Python.getInstance();
            PyObject pyObject_result = py.getModule("audio2midi").callAttr("run", file_in, file_out);
            py.getBuiltins().get("help").call();
            int result = pyObject_result.toJava(Integer.class);
            if (result == 0) {
                Toast.makeText(RecordActivity.this, "轉檔成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RecordActivity.this, "轉檔失敗", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
