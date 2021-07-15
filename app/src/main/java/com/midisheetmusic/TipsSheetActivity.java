package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TipsSheetActivity extends AppCompatActivity{
    private Button btn_play_tip;
    private Button btn_add_tip;
    private Button btn_go_back;
    private String name = TipsSongActivity.song_name;
    private  TextView textView;
    private LinearLayout sheet_music_tip;
    private SheetMusic sheet;

    public void onCreate(Bundle state){
        super.onCreate(state);
        setContentView(R.layout.play_tips_sheet);

        btn_play_tip = findViewById(R.id.btn_play_tip);
        btn_add_tip = findViewById(R.id.btn_add_tip);
        textView =  findViewById(R.id.text_name);
        sheet_music_tip = findViewById(R.id.sheet_music_tip);

        textView.setText(name);

        sheet = new SheetMusic(this);
//        sheet_music_tip.addView(sheet);


        //返回鍵
        btn_go_back = findViewById(R.id.btn_go_back3);
        btn_go_back.setOnClickListener((back)->{
            onBackPressed();
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }
}

