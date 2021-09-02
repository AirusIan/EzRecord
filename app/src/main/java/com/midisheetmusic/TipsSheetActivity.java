package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.midisheetmusic.sheets.ClefSymbol;

import java.util.zip.CRC32;

import static com.midisheetmusic.SheetType.Sheet_type_list.Tip;

public class TipsSheetActivity extends MidiHandlingActivity{
    private Button btn_go_back;
    private TextView textView;

    public static Uri addUri;
    public static String addTitle;

/*  1-1. 相同音檔 測試有沒有辦法直接讀取音軌的全部聲音然後加入至樂譜後面
    1-2. 相同音檔做檔案合併測試
    補：這裡需要抓取原音軌最後endTime
    2. 嘗試儲存成新的檔案
    3. 讀取不同檔案的音軌並加入
    4. 開始實作加入樂譜, 如何在不同activity更改其他activity的內容
    補：要怎麼抓取檔案? Tips的音檔inflact在不同xml, 且套用了MidiPlayer。
*/

    //樂譜
    public static final String MidiTitleID = "MidiTitleID";
    private MidiPlayer player;   /* The play */
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* The layout for sheet*/
    private LinearLayout layout_button; /* The layout for button */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;        /* CRC of the midi bytes */
    private Uri uri;
    private String title;
    private byte[] data;

    public void onCreate(Bundle state){
        SheetType.sheet_type = Tip;
        super.onCreate(state);
        setTitle("MidiSheetMusic: PlayTips");
        setContentView(R.layout.play_tips_sheet);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);

        //設定title
        textView =  findViewById(R.id.text_name);
        textView.setText(this.getIntent().getStringExtra(MidiTitleID));


        //樂譜顯示
        uri = this.getIntent().getData();
        Log.d("uri_check", uri.getPath());
        if (uri == null) {
            this.finish();
            return;
        }
        title = this.getIntent().getStringExtra(MidiTitleID);
        if (title == null) {
            title = uri.getLastPathSegment();
        }
        addUri = uri;
        addTitle = title;
        FileUri file = new FileUri(uri, title);
        this.setTitle("MidiSheetMusic: " + title);

        try {
            data = file.getData(this);
            midifile = new MidiFile(data, title);
            Log.d("midi_check",midifile.toString());
        }
        catch (MidiFileException e) {
            this.finish();
            return;
        }

        // Initialize the settings (MidiOptions).
        // If previous settings have been saved, use those
        options = new MidiOptions(midifile);
        CRC32 crc = new CRC32();
        crc.update(data);
        midiCRC = crc.getValue();
        SharedPreferences settings = getPreferences(0);
        String json = settings.getString("" + midiCRC, null);
        MidiOptions savedOptions = MidiOptions.fromJson(json);
        if (savedOptions != null) {
            options.merge(savedOptions);
        }

        //返回鍵
        btn_go_back = findViewById(R.id.btn_go_back3);
        btn_go_back.setOnClickListener((back)->{
            onBackPressed();
        });


        createViews();
    }
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        SheetType.sheet_type = SheetType.Sheet_type_list.Edit;
        super.onBackPressed();
    }

    public void createViews() {
        layout = findViewById(R.id.sheet_music_tip);
        layout_button = findViewById(R.id.layout_button);
        player = new MidiPlayer(this);
        layout_button.addView(player);
        layout.requestLayout();
        layout_button.requestLayout();
        player.setSheetUpdateRequestListener(() -> createSheetMusic(options));
        createSheetMusic(options);

    }

    private void createSheetMusic(MidiOptions options) {
        if (sheet != null) {
            layout.removeView(sheet);
        }

        sheet = new SheetMusic(this);
        sheet.init(midifile, options);
        sheet.setPlayer(player);
        layout.addView(sheet);

        player.SetMidiFile(midifile, options, sheet);
//        player.updateToolbarButtons(); //考慮是否加入
        layout.requestLayout();
        layout_button.requestLayout();
        sheet.draw();
    }

    @Override
    void OnMidiDeviceStatus(boolean connected) {

    }

    @Override
    void OnMidiNote(int note, boolean pressed) {

    }

    @Override
    protected void onStop() {
        SheetType.sheet_type = SheetType.Sheet_type_list.Edit;
        super.onStop();
    }
}

