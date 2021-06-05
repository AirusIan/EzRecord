package com.midisheetmusic;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MidiSheetMusic: Record");
        // Load the list of songs
        setContentView(R.layout.record);

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

        fileName = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/myrecording.3gp";

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setOutputFile(fileName);
                try {
                    recorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recorder.start();
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void start(View view){
        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        btn_record.setEnabled(false);
//        btn_stop.setEnabled(true);

    }

//    public void stop(View view){
//        recorder.stop();
//        recorder.release();
//        recorder  = null;
//        btn_stop.setEnabled(false);
//        Toast.makeText(getApplicationContext(), "Audio recorded successfully",
//                Toast.LENGTH_LONG).show();
//    }
}
