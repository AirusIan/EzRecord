package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TipsSongActivity extends AppCompatActivity {


    private Button btn_go_back;
    private ListView list_songs;
    private String status_check = TipsActivity.status;
    private ArrayAdapter adapter;

    /*讀檔*/
//    private ArrayList<FileUri> songlist;
//    private File rootdir;
//    File[] files = {};

    /*分類*/
    private String str_lists[];

    public void onCreate(Bundle state) {
        try {
            super.onCreate(state);
            setContentView(R.layout.tips_song);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //顯示歌曲清單
        list_songs = findViewById(R.id.list_songs);
        adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, getFiles());
        list_songs.setAdapter(adapter);

        //返回鍵
        btn_go_back = findViewById(R.id.btn_go_back2);
        btn_go_back.setOnClickListener((back)->{
            onBackPressed();
        });
    }


    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    public String [] getFiles(){
        try {
            str_lists = getAssets().list(status_check);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str_lists;
    }

//    public void getFiles(File[] files) {
//        for (File file : files){
//            if (file.getName().endsWith(".mid") || file.getName().endsWith(".MID") ||
//                    file.getName().endsWith(".midi")) {
//                Uri uri = Uri.parse("file://" + file.getAbsolutePath());
//                String displayName = uri.getLastPathSegment();
//                FileUri song = new FileUri(uri, displayName);
//                songlist.add(song);
//            }
//        }
//    }
}
