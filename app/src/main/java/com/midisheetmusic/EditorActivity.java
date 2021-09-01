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
            btn_addSheet = findViewById(R.id.ic_delete);
            btn_addSheet.setOnClickListener((add)->{
                addSheet();
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
        String str = "content";
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

            FileUri fileUri = new FileUri(uri, title);
            FileOutputStream output = new FileOutputStream(saved_file);
            byte[] bytes = fileUri.getData(this);
            MidiFile midiFile = new MidiFile(fileUri.getData(this), fileUri.toString());

            //合併區域
            Uri addUri = uri.parse("file:///android_asset/Bach__Minuet_in_G_major.mid");
            String addTitle = "Bach__Minuet_in_G_major";
            FileUri addFile = new FileUri(addUri, addTitle);
            byte[] addData = addFile.getData(this);

//            byte[] combined_array = new byte[bytes.length + addData.length];

//            System.arraycopy(bytes, 0, combined_array, 0, bytes.length);
//            System.arraycopy(addData, 0, combined_array, bytes.length, addData.length);
//            java.lang.ArrayIndexOutOfBoundsException: src.length=2889 srcPos=22 dst.length=2985 dstPos=0 length=2889
//            java.lang.ArrayIndexOutOfBoundsException: src.length=2889 srcPos=22 dst.length=8452 dstPos=5563 length=2889

            output.write(bytes);
            output.close();

//            reviseFile(midiFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //暫留, 可能之後會用到 (未來存檔功能修正時)
//    private void reviseFile(String midiFilePath){
//        Uri re_uri = Uri.parse(midiFilePath);
//        FileUri fileUri = new FileUri(re_uri, re_uri.getLastPathSegment());
//        MidiFile re_midifile = new MidiFile(fileUri.getData(this), re_uri.getLastPathSegment());
//        try {
//            FileOutputStream output = openFileOutput(title, Context.MODE_PRIVATE);
//            re_midifile.Write(output, options);
//            output.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    /** 寫到懷疑人生卻還是沒有結果 */
    /** 音軌合併 */
    //    將Array2合併至Array1
//    public byte[] trackAdd(byte[] array1, byte[] array2, int i) {
//
//        int len_head = 14; //檔案開頭長度
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        baos.write(array1, 0, array1.length);
//        baos.write(array2, len_head, i-len_head);
//
//        byte[] complete_track = baos.toByteArray();
//
//        return complete_track;
//
//    }
//    /** 預測音軌長度 */
//    //    以byte[4]的形式呈現
//    public byte[] trackLength(int length) {
//        ByteArrayOutputStream trackLengthBaos = new ByteArrayOutputStream();
//        System.out.println(String.format("%08X", length));
//        for(int i = 0; i < 8; i += 2) {
//             String trimString = String.format("%08X", length).substring(i, i+2).trim();
//            try {
//                trackLengthBaos.write(hexStringToByteArray(trimString));
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            System.out.println(trimString);
//        }
//        return trackLengthBaos.toByteArray();
//    }
//    //   每個byte[]含有兩個字，符合midi格式 ex: 00 00 05 1D
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
//    public static byte[] convert(String digits) {
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        for (int i = 0; i < digits.length(); i += 2) {
//            char c1 = digits.charAt(i);
//            if ((i + 1) >= digits.length())
//                throw new IllegalArgumentException("hexUtil.odd");
//            char c2 = digits.charAt(i + 1);
//            byte b = 0;
//            if ((c1 >= '0') && (c1 <= '9'))
//                b += ((c1 - '0') * 16);
//            else if ((c1 >= 'a') && (c1 <= 'f'))
//                b += ((c1 - 'a' + 10) * 16);
//            else if ((c1 >= 'A') && (c1 <= 'F'))
//                b += ((c1 - 'A' + 10) * 16);
//            else
//                throw new IllegalArgumentException("hexUtil.bad");
//            if ((c2 >= '0') && (c2 <= '9'))
//                b += (c2 - '0');
//            else if ((c2 >= 'a') && (c2 <= 'f'))
//                b += (c2 - 'a' + 10);
//            else if ((c2 >= 'A') && (c2 <= 'F'))
//                b += (c2 - 'A' + 10);
//            else
//                throw new IllegalArgumentException("hexUtil.bad");
//            baos.write(b);
//        }
//        return (baos.toByteArray());
//
//    }

    /** 加入樂譜 */
    private void addSheet(){
//        可用功能Clone、CombineToTwoTracks
        Uri addUri = uri.parse("file:///android_asset/Bach__Minuet_in_G_major.mid");
        String addTitle = "Bach__Minuet_in_G_major";
        FileUri addFile = new FileUri(addUri, addTitle);
        byte[] addData = addFile.getData(this);;
        MidiFile addMidiFile = new MidiFile(addData, addTitle);
//        ArrayList<MidiTrack> testTrack = midifile.AddSheet(midifile.getTracks(), addMidiFile.getTracks());
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