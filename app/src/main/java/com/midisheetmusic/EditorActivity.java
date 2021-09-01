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
import java.util.zip.CRC32;

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

    //靈感、存檔功能
    //讀取midiFile的方式, 先抓Uri跟title, 透過FileUri解析byte[], 轉為midi檔案讀入.
    private Button btn_tips;
    private Button btn_save;
    private String str_saveName;
    private Button btn_addSheet;

    //音符相關Button
    private Button ic_rewind,ic_delete,ic_done,ic_menu_save,ic_music_note; //ic_rewind是backButton
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
            //測試用
            String value = new String(data, "UTF-8");
            Log.d("ByteData", Base64.getEncoder().encodeToString(data));
            Log.d("ByteData", value);
            

            midifile = new MidiFile(data, title);

            options = new MidiOptions(midifile);
            CRC32 crc = new CRC32();
            crc.update(data);
            midiCRC = crc.getValue();

            createViews();

            //***********
            // id宣告
            ic_rewind = findViewById( R.id.ic_rewind);
            ic_delete = findViewById( R.id.ic_delete);
            ic_done = findViewById( R.id.ic_done);
            ic_menu_save = findViewById( R.id.ic_menu_save);
            ic_music_note = findViewById( R.id.ic_music_note);

            eightnote = findViewById( R.id.eightnote);
            dotquarternote = findViewById( R.id.dotquarternote);
            downnote = findViewById( R.id.downnote);
            halfnote = findViewById( R.id.halfnote);
            quarternote = findViewById( R.id.quarternote);
            quarterrest = findViewById( R.id.quarterrest);
            sixteennote = findViewById( R.id.sixteennote);
            upnote = findViewById( R.id.upnote);
            wholenote = findViewById( R.id.wholenote);

            tips();
            btn_save = findViewById(R.id.ic_menu_save);
            btn_save.setOnClickListener((save)->{
                save();
            });
//            btn_addSheet = findViewById(R.id.ic_delete);
//            btn_addSheet.setOnClickListener((add)->{
//                addSheet();
//            });
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
    public void ondeleteClick(View view) {
        int NotePulseTime = player.NotePulseTime();
        System.out.println(NotePulseTime);
        midifile.DeleteNote(NotePulseTime);
        createViews();
    }
    public void noteClick(View view) {
    ic_music_note.setVisibility(View.GONE);
    ic_menu_save.setVisibility(View.GONE);
    ic_done.setVisibility(View.VISIBLE);
    ic_rewind.setVisibility(View.GONE);
    btn_tips.setVisibility(View.GONE);

    eightnote.setVisibility(View.VISIBLE);
    dotquarternote.setVisibility(View.VISIBLE);
    downnote.setVisibility(View.VISIBLE);
    halfnote.setVisibility(View.VISIBLE);
    quarternote.setVisibility(View.VISIBLE);
    quarterrest.setVisibility(View.VISIBLE);
    sixteennote.setVisibility(View.VISIBLE);
    upnote.setVisibility(View.VISIBLE);
    wholenote.setVisibility(View.VISIBLE);
    }
    public void doneClick(View view) {
        ic_music_note.setVisibility(View.VISIBLE);
        ic_menu_save.setVisibility(View.VISIBLE);
        ic_done.setVisibility(View.GONE);
        ic_rewind.setVisibility(View.VISIBLE);
        btn_tips.setVisibility(View.VISIBLE);

        eightnote.setVisibility(View.GONE);
        dotquarternote.setVisibility(View.GONE);
        downnote.setVisibility(View.GONE);
        halfnote.setVisibility(View.GONE);
        quarternote.setVisibility(View.GONE);
        quarterrest.setVisibility(View.GONE);
        sixteennote.setVisibility(View.GONE);
        upnote.setVisibility(View.GONE);
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
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
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
//            addSheet();

            midifile.Write(output, null);
            output.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /** 加入樂譜 */
    private void addSheet(){
        Uri addUri = uri.parse("file:///android_asset/Bach__Minuet_in_G_major.mid");
        String addTitle = "Bach__Minuet_in_G_major";
        FileUri addFile = new FileUri(addUri, addTitle);
        byte[] addData = addFile.getData(this);;
        MidiFile addMidiFile = new MidiFile(addData, addTitle);
        midifile.AddSheet(midifile, addMidiFile.getTracks());

        createViews();
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