package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TipsActivity extends AppCompatActivity {

    private Button btn_go_back;
    private Button btn_happy;
    private Button btn_inspiring;
    private Button btn_depressing;
    private Button btn_relaxing;
    private Button btn_solemn;
    private Button btn_romantic;
    public String status;

    @Override
    public void onCreate(Bundle state) {
        try {
            super.onCreate(state);
            setTitle("MidiSheetMusic: Welcome");
            // Load the list of songs
            setContentView(R.layout.tips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //BUTTON連結
        btn_happy = findViewById(R.id.btn_happy);
        btn_inspiring = findViewById(R.id.btn_inspiring);
        btn_depressing = findViewById(R.id.btn_depressing);
        btn_relaxing = findViewById(R.id.btn_relaxing);
        btn_solemn = findViewById(R.id.btn_solemn);
        btn_romantic = findViewById(R.id.btn_romantic);

        //BUTTON功能
        btn_happy.setOnClickListener((next)->{
            status = "Happy";
            loadSongs();
        });
        btn_inspiring.setOnClickListener((next)->{
            status = "Inspiring";
            loadSongs();
        });
        btn_depressing.setOnClickListener((next)->{
            status = "Depressing";
            loadSongs();
        });
        btn_relaxing.setOnClickListener((next)->{
            status = "Relaxing";
            loadSongs();
        });
        btn_solemn.setOnClickListener((next)->{
            status = "Solemn";
            loadSongs();
        });
        btn_romantic.setOnClickListener((next)->{
            status = "Romantic";
            loadSongs();
        });

        //返回鍵
        btn_go_back = findViewById(R.id.btn_go_back);
        btn_go_back.setOnClickListener((back)->{
            onBackPressed();
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    public void loadSongs(){
        Intent intent = new Intent();
        intent.setClass(this, TipsSongActivity.class);
        intent.putExtra("status",status);
        startActivity(intent);
    }
}