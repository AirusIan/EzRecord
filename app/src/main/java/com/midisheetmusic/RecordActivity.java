package com.midisheetmusic;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

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
    }
}
