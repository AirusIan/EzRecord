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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.*;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class WelcomeActivity extends AppCompatActivity {
    
    private Handler handler;
    private NbButton btn_record;
    private ConstraintLayout clayout;
    private Animator animator;


    @Override
    public void onCreate(Bundle state) {
        try {
            super.onCreate(state);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setTitle("MidiSheetMusic: Welcome");
            // Load the list of songs
            setContentView(R.layout.welcome);

            btn_record = findViewById(R.id.welcome_record_btn);
            clayout = findViewById(R.id.welcome_layout);
            handler = new Handler();

            clayout.getBackground().setAlpha(0);

            btn_record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Start test");
                    btn_record.startAnim();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gotoNew();
                        }
                    }, 2000);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


        ImageButton btn_to_choose_song = findViewById(R.id.btn_start);

        btn_to_choose_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, ChooseSongActivity.class);
                startActivity(intent);
            }
        });
    }

    private void gotoNew() {
        btn_record.gotoNew();

        final Intent intent=new Intent(this,RecordActivity.class);

        int xc=(btn_record.getLeft()+btn_record.getRight())/2;
        int yc=(btn_record.getTop()+btn_record.getBottom())/2;
        animator = ViewAnimationUtils.createCircularReveal(clayout,xc,yc,0,1111);
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    }
                }, 200);
            }
        });
        animator.start();
        clayout.getBackground().setAlpha(255);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(animator.isRunning()){
//            animator.cancel();
//        }
        clayout.getBackground().setAlpha(0);
        btn_record.regainBackground();
        btn_record.setText("Record Now");

    }

    @SuppressLint("ResourceAsColor")
    private void changeLayoutBackground(){
        clayout.setBackgroundColor(R.color.md_blue_400);
    }

    private void recoverLayoutBackground(){

    }

}


