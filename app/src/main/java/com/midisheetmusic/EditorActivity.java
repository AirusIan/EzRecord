package com.midisheetmusic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.midisheetmusic.R;

public class EditorActivity extends AppCompatActivity {
    public void onCreate(Bundle state) {
        try {
            super.onCreate(state);
            setTitle("MidiSheetMusic: Welcome");
            // Load the list of songs
            setContentView(R.layout.editor_bar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}