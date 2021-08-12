package com.midisheetmusic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

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
    private Button btn_tips; /*新增 連結tips by昭穎*/
    private Button ic_rewind,ic_delete,ic_done,ic_menu_save,ic_music_note; //ic_rewind是backButton
    private Button eightnote,dotquarternote,downnote,halfnote,quarternote,quarterrest,sixteennote,upnote,wholenote;

    public void onCreate(Bundle state) {
        try {
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


            createViews();


            //***********
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        tips(); //go to tips

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
        player.updateToolbarButtons();
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

    //新增功能，前往tips頁面
    private void tips() {
        btn_tips = findViewById(R.id.btn_go_tips);
        btn_tips.setOnClickListener((tips)->{
            Intent intent = new Intent();
            intent.setClass(this, TipsActivity.class);
            startActivity(intent);
        });
    }
    public void onbackClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, SheetMusicActivity.class);
        intent.putExtra(SheetMusicActivity.MidiTitleID, title);
        intent.setClass(this, SheetMusicActivity.class);
        startActivity(intent);
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