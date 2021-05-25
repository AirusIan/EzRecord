package com.midisheetmusic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RecordActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("MidiSheetMusic: Welcome");
        // Load the list of songs
        setContentView(R.layout.record);
    }
}
