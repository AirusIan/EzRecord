package com.midisheetmusic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.zip.CRC32;
/* 目前問題 ：
 * 1. 沒有track2，可視為同一track來處理
 * 2. 複數音符的處理 -> 刪除、調整音高能只調單音?
 * 3. */
public class EditorActivity extends  MidiHandlingActivity {
    public static final String MidiTitleID = "MidiTitleID";
    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private Piano piano;         /* The piano at the top */
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* The layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private Uri uri;
    private String title;
    private long midiCRC;
    private int transpose = 0;

    //靈感、存檔功能
    //讀取midiFile的方式, 先抓Uri跟title, 透過FileUri解析byte[], 轉為midi檔案讀入.
    private Button btn_tips;
    private Button btn_save;
    private String str_saveName;
    private Button btn_addSheet;

    //音符相關Button
    private Button ic_rewind,ic_transpose,ic_delete,ic_done,ic_menu_save,ic_music_note,ic_add; //ic_rewind是backButton
    private Button eightnote,dotquarternote,downnote,halfnote,quarternote,quarterrest,sixteennote,upnote,wholenote;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate(Bundle state) {
        try {
            //獲取寫入權限
            requestPermission();

            super.onCreate(state);
            SheetType.sheet_type = SheetType.Sheet_type_list.Edit;
            setTitle("MidiSheetMusic: Welcome");
            // Load the list of songs
            setContentView(R.layout.editor_bar);
            hideSystemUI();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            uri = this.getIntent().getData();
            if (uri == null) {
                this.finish();
                return;
            }
            title = this.getIntent().getStringExtra(MidiTitleID);
            if (title == null) {
                title = uri.getLastPathSegment();
            }
            FileUri file = new FileUri(uri, title);
            this.setTitle("MidiSheetMusic: " + title);
            byte[] data;

            data = file.getData(this);
            midifile = new MidiFile(data, title);

            options = new MidiOptions(midifile);
            CRC32 crc = new CRC32();
            crc.update(data);
            midiCRC = crc.getValue();

            createViews();

            trackCheck();


            /** id宣告 */
            ic_rewind = findViewById( R.id.ic_rewind);
            ic_transpose = findViewById(R.id.ic_transpose);
            ic_delete = findViewById( R.id.ic_delete);
            ic_done = findViewById( R.id.ic_done);
            ic_menu_save = findViewById( R.id.ic_menu_save);
            ic_music_note = findViewById( R.id.ic_music_note);

            eightnote = findViewById( R.id.eightnote);
//            downnote = findViewById( R.id.downnote);
            halfnote = findViewById( R.id.halfnote);
            quarternote = findViewById( R.id.quarternote);
//            quarterrest = findViewById( R.id.quarterrest);
            sixteennote = findViewById( R.id.sixteennote);
//            upnote = findViewById( R.id.upnote);
            wholenote = findViewById( R.id.wholenote);

            btn_save = findViewById(R.id.ic_menu_save);
            ic_add = findViewById(R.id.ic_add);

            /**  Button功能 */
            tips();
            btn_save.setOnClickListener((save)->{
                save();
            });
            ic_add.setOnClickListener((add)->{
                addSheet();
            });
            wholenote.setOnClickListener((whole)->{
                Log.d("time_check", String.valueOf(midifile.getTime().DurationToTime(NoteDuration.Whole)));
                addNote(0, midifile.getTime().DurationToTime(NoteDuration.Whole));
            });
            halfnote.setOnClickListener((half)->{
                Log.d("time_check", String.valueOf(midifile.getTime().DurationToTime(NoteDuration.Half)));
                addNote(0, midifile.getTime().DurationToTime(NoteDuration.Half));
            });
            quarternote.setOnClickListener((quarter)->{
                Log.d("time_check", String.valueOf(midifile.getTime().DurationToTime(NoteDuration.Quarter)));
                addNote(0, midifile.getTime().DurationToTime(NoteDuration.Quarter));
            });
            eightnote.setOnClickListener((eight)->{
                Log.d("time_check", String.valueOf(midifile.getTime().DurationToTime(NoteDuration.Eighth)));
                addNote(0, midifile.getTime().DurationToTime(NoteDuration.Eighth));
            });
            sixteennote.setOnClickListener((sixteen)->{
                Log.d("time_check", String.valueOf(midifile.getTime().DurationToTime(NoteDuration.Sixteenth)));
                addNote(0, midifile.getTime().DurationToTime(NoteDuration.Sixteenth));
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void createViews() {
        layout = findViewById(R.id.editor_content);

        player = new MidiPlayer(this);
        player.setSheetUpdateRequestListener(() -> createSheetMusic(options));
        createSheetMusic(options);
    }
    private void
    createSheetMusic(MidiOptions options) {
        if (sheet != null) {
            layout.removeView(sheet);
        }
        sheet = new SheetMusic(this);
        sheet.init(midifile, options);
        sheet.setPlayer(player);
        layout.addView(sheet);//將樂譜加入畫面
        player.SetMidiFile(midifile, options, sheet);
        layout.requestLayout();
        sheet.draw();
    }
    private void hideSystemUI() {
        // Enables sticky immersive mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    public void onbackClick(View view) {
        super.onBackPressed();
    }


    /** 音高轉換 */
    public void onTransposeClick(View view){
        int notePulseTime = player.NotePulseTime();
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setCancelable(false);
        builder.setMessage("選擇要調整的音軌");

        builder.setNegativeButton("Track 1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createListDialog(0);
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Track 2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createListDialog(1);
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    /** 選取音高調整幅度 */
    private void createListDialog(int track){
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(EditorActivity.this);
        dialog_list.setItems(R.array.transpose_entries, new DialogInterface.OnClickListener(){
            @Override

            public void onClick(DialogInterface dialog, int value) {
                if(value <= 12){
                    transpose = 12 - value;
                }else if(value > 12){
                    transpose = (value - 12) * (-1);
                }
                if(player.prevPulseTime != -10) {
                    midifile.transposeNote(player.NotePulseTime(), track, transpose);
                }
                dialog.dismiss();
                createViews();
            }
        });
        dialog_list.show();
    }


    /** 音符刪除 */
    public void ondeleteClick(View view) {
        int NotePulseTime = player.NotePulseTime();
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setCancelable(false);
        builder.setMessage("選擇要刪除的音軌");
        //alterdialog最多可以放三個按鈕，且位置是固定的，分別是
        //setPositiveButton()右邊按鈕
        //setNegativeButton()中間按鈕
        //setNeutralButton()左邊按鈕
        builder.setNeutralButton("ALL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                midifile.DeleteNote(NotePulseTime,3);
                dialogInterface.dismiss();
                createViews();
            }
        });
        builder.setNegativeButton("Track 1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                midifile.DeleteNote(NotePulseTime,0);
                dialogInterface.dismiss();
                createViews();
            }
        });
        builder.setPositiveButton("Track 2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                midifile.DeleteNote(NotePulseTime,1);
                dialogInterface.dismiss();
                createViews();
            }
        });
        builder.create().show();
    }


    public void noteClick(View view) {
        ic_music_note.setVisibility(View.GONE);
        ic_menu_save.setVisibility(View.GONE);
        ic_add.setVisibility(View.GONE);
        ic_transpose.setVisibility(View.GONE);
        ic_done.setVisibility(View.VISIBLE);
        ic_rewind.setVisibility(View.GONE);
        btn_tips.setVisibility(View.GONE);
        ic_delete.setVisibility(View.VISIBLE);

//    downnote.setVisibility(View.VISIBLE);
//    upnote.setVisibility(View.VISIBLE);
//    quarterrest.setVisibility(View.VISIBLE);
        sixteennote.setVisibility(View.VISIBLE);
        eightnote.setVisibility(View.VISIBLE);
        quarternote.setVisibility(View.VISIBLE);
        halfnote.setVisibility(View.VISIBLE);
        wholenote.setVisibility(View.VISIBLE);
    }


    public void doneClick(View view) {
        ic_music_note.setVisibility(View.VISIBLE);
        ic_menu_save.setVisibility(View.VISIBLE);
        ic_done.setVisibility(View.GONE);
        ic_rewind.setVisibility(View.VISIBLE);
        btn_tips.setVisibility(View.VISIBLE);
        ic_add.setVisibility(View.VISIBLE);
        ic_transpose.setVisibility(View.VISIBLE);
        ic_delete.setVisibility(View.GONE);

        eightnote.setVisibility(View.GONE);
//        dotquarternote.setVisibility(View.GONE);
//        downnote.setVisibility(View.GONE);
        halfnote.setVisibility(View.GONE);
        quarternote.setVisibility(View.GONE);
//        quarterrest.setVisibility(View.GONE);
        sixteennote.setVisibility(View.GONE);
//        upnote.setVisibility(View.GONE);
        wholenote.setVisibility(View.GONE);
    }


    /** 前往tips頁面 */
    private void tips() {
        btn_tips = findViewById(R.id.btn_go_tips);
        btn_tips.setOnClickListener((tips)->{
            Intent intent = new Intent();
            intent.setClass(this, TipsActivity.class);
            startActivity(intent);
        });
    }


    /** 儲存功能 延續createFile
     * 只是這裡負責畫面顯示，輸入檔名的部分*/
    private void save() {
        if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            View v = getLayoutInflater().inflate(R.layout.save_midi_dialog, null);
            v.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            EditText editText = v.findViewById(R.id.editor_EditText);
            new AlertDialog.Builder(this)
                    .setTitle("儲存檔案")
                    .setMessage("如果檔名相同會覆蓋，請注意")
                    .setView(v)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            str_saveName = String.valueOf(editText.getText());
                            if (str_saveName.equals("")) {
                                Toast.makeText(EditorActivity.this, "檔名不得為空，儲存失敗", Toast.LENGTH_SHORT).show();
                            } else {
                                createFile();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setCancelable(false)
                    .show();

        } else if (isExternalStorageWritable()) {
            Toast.makeText(this, "Storage failed", Toast.LENGTH_SHORT).show();
        } else if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Permission failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "exceptionally failed", Toast.LENGTH_SHORT).show();
        }
    }


    /** 權限確認 */
    private boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.i("State", "Yes, it is writable!");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }


    /** 儲存、建立新檔 */
    private void createFile() {
        str_saveName = str_saveName + ".mid";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String midiFilePath = filePath + "/midiSaveFile/" + str_saveName;

        File saved_file = new File(midiFilePath);
        if (!saved_file.getParentFile().exists()) {
            saved_file.getParentFile().mkdirs();
        }
        try {
            if (!saved_file.exists()) {
                saved_file.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(saved_file);
            midifile.Save_function(output,player.options);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /** 加入樂譜 */
    private void addSheet(){
        Uri addUri = TipsSheetActivity.addUri;
        String addTitle = TipsSheetActivity.addTitle;
        if (addUri != null && !addTitle.isEmpty()) {
            Log.d("", "Uri:" + addUri.getPath() + "/n  Title:" + addTitle);
            FileUri addFile = new FileUri(addUri, addTitle);
            byte[] addData = addFile.getData(this);
            MidiFile addMidiFile = new MidiFile(addData, addTitle);
            midifile.AddSheet(midifile, addMidiFile.getTracks());
            createViews();
        }else{
            Toast.makeText(this, "尚未設定", Toast.LENGTH_SHORT).show();
        }
    }

    /** 新增音符 */
//    要對休止符以及音符點擊時做出差異
//    track有時單有時雙, HOW?
//    需判斷前一個音符的endTime (startTime + Duration)
//    音符合併的處理? 目前可以 4分->兩個8分, 那兩個8分可以改變成其他的嗎?
//    16分/120 - 8分/240 - 4分/480 - 2分/960 - 全音符/1920  有部分歌曲音符不規律 暫不考慮 ex: 384
    // 傳入參數 陣列位置 持續時間
    private void addNote(int trackNum, int duration){
        int notePulseTime = player.notePulseTime;
        int position = 0;
        boolean noteExist = false;
        MidiNote midiNote = null;


         /*
        判斷點選的是休止符還是音符
        exist = true(音符)/false(休止符)
        midiNote 用來記錄音符, 如果為休止符則為休止符後面的音符
        position 音符位於陣列的位置(音符位置、休止符後一個音符位置)
         */
        for (MidiNote note : midifile.getTracks().get(trackNum).getNotes()){
            if (note.getStartTime() - notePulseTime == 0){
                midiNote = note;
                noteExist = true;
                break;
            }else if (note.getStartTime() - notePulseTime > 0){
                midiNote = note;
                break;
            }
            position++;
        }

//      休止符位置加入音符
        if (noteExist && midiNote.getDuration() > duration){
            midifile.getTracks().get(trackNum).getNotes().get(position).setDuration(duration);
            Log.d("note_duration", String.valueOf(midiNote.getDuration()));
        }else{
            if (midiNote.getStartTime() - notePulseTime >= duration) {
                Log.d("Time", "NoteTime："+ midiNote.getStartTime() +"  pulseTime："+notePulseTime);
                MidiNote addNote = new MidiNote(notePulseTime, 0, 72, duration);
                midifile.getTracks().get(0).AddAtNote(position, addNote);
            }
        }
//        trackCheck();
        createViews();
    }

    //列印Track提供檢視 -> 單track - False / 雙track - True
    private boolean trackCheck(){
        if (midifile.getTracks().size() == 2){
            Log.d("track_check", "第一條Track" + midifile.getTracks().get(0).toString());
            Log.d("track_check", "第二條Track" + midifile.getTracks().get(1).toString());
            return true;
        }else{
            Log.d("track_check", "Only One Track/n");
            Log.d("track_check", "第一條Track" + midifile.getTracks().get(0).toString());
            return false;
        }
    }


    @Override
    void OnMidiDeviceStatus(boolean connected) {


    }

    @Override
    void OnMidiNote(int note, boolean pressed) {

    }

    @Override
    protected void onStop() {
        SheetType.sheet_type = SheetType.Sheet_type_list.Normal;
        super.onStop();
    }
}

