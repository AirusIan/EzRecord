package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TipsActivity extends AppCompatActivity {

    private Button btn_go_back;

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

        btn_go_back = findViewById(R.id.btn_go_back);
        btn_go_back.setOnClickListener((g)->{
                onBackPressed();
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }
}