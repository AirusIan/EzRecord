/*
 * Copyright (c) 2011-2013 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.midisheetmusic;

import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.*;


import androidx.appcompat.app.AppCompatActivity;
public class WelcomeActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("MidiSheetMusic: Welcome");
        // Load the list of songs
        setContentView(R.layout.welcome);

        ImageButton btn_to_choose_song = (ImageButton) findViewById(R.id.btn_folder);
        Button btn_to_new_song = (Button) findViewById(R.id.btn_start);

        btn_to_choose_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, ChooseSongActivity.class);
                startActivity(intent);
            }
        });
        btn_to_new_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                Intent intent = new Intent(Intent.ACTION_VIEW, file.getUri(), this, SheetMusicActivity.class);
//                如何建立空白檔案?
                intent.setClass(WelcomeActivity.this, ChooseSongActivity.class);
                startActivity(intent);
            }
        });
    }

}


