package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;

public class TipsSongActivity extends AppCompatActivity {

    private Button btn_go_back;
    private ListView list_songs;
    private ArrayAdapter adapter;
    private String str_lists[];
    private Bundle bag;
    private String path;    //uri路徑
    private String title;   //純檔名, e.g. Bach__Minuet_in_G_major
    private Uri uri;        //位置, e.g. file:///android_asset/Bach__Minuet_in_G_major.mid
    private FileUri fileUri;//檔案


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

        //list點擊功能
        list_songs.setOnItemClickListener((parent, view, position, id)->{

            //傳遞資料與頁面切換
            title = getName(position);//
            path = "file:///android_asset/"+this.getIntent().getStringExtra("status")+"/"+list_songs.getItemAtPosition(position);//
            uri = uri.parse(path);
            fileUri = new FileUri(uri, title);
            doOpenFile(fileUri);

            //確認訊息
            Toast.makeText(this,"點選第 "+(position +1) +" 個 \n內容："+ fileUri.toString(), Toast.LENGTH_SHORT).show();

        });


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
            str_lists = getAssets().list(this.getIntent().getStringExtra("status"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str_lists;
    }

    public String getName(int position){
        String str_name;
        str_name = (String)list_songs.getItemAtPosition(position);

        return str_name.substring(0, str_name.length()-4);
    }

    public void doOpenFile(FileUri file) {
        byte[] data = file.getData(this);
        if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
            ChooseSongActivity.showErrorDialog("Error: Unable to open song: " + file.toString(), this);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, file.getUri(), this, TipsSheetActivity.class);
        intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());


        startActivity(intent);
    }

}
