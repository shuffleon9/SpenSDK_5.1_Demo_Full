package com.samsung.android.sdk.pen.pg.example7_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.composer.SpenActionListener;
import com.samsung.android.sdk.composer.SpenComposerView;
import com.samsung.android.sdk.composer.SpenContextMenuListener;
import com.samsung.android.sdk.composer.SpenCursorChangedListener;
import com.samsung.android.sdk.composer.SpenItemClickListener;
import com.samsung.android.sdk.composer.SpenScrollListener;
import com.samsung.android.sdk.composer.SpenSoftInputListener;
import com.samsung.android.sdk.composer.document.SpenContentBase;
import com.samsung.android.sdk.composer.document.SpenContentHandWriting;
import com.samsung.android.sdk.composer.document.SpenContentImage;
import com.samsung.android.sdk.composer.document.SpenContentText;
import com.samsung.android.sdk.composer.document.SpenContentVoice;
import com.samsung.android.sdk.composer.document.SpenContentWeb;
import com.samsung.android.sdk.composer.document.SpenSDoc;
import com.samsung.android.sdk.composer.document.exception.SpenExceedImageLimitException;
import com.samsung.android.sdk.composer.document.exception.SpenExceedTextLimitException;
import com.samsung.android.sdk.composer.document.exception.SpenSDocInvalidPasswordException;
import com.samsung.android.sdk.composer.document.exception.SpenSDocUnsupportedFileException;
import com.samsung.android.sdk.composer.document.exception.SpenSDocUnsupportedVersionException;
import com.samsung.android.sdk.composer.document.textspan.SpenTextSpan;
import com.samsung.android.sdk.composer.document.util.SpenSDocUtil;
import com.samsung.android.sdk.composer.voice.VoiceManager;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.pg.utils.ComposerContainer;
import com.samsung.android.sdk.pen.pg.utils.SoftInput;
import com.samsung.android.sdk.pen.pg.utils.web.WebManager;
import com.samsung.android.sdk.pen.pg.utils.web.util.Util;
import com.samsung.android.sdk.pen.settingui.SpenColorPickerPopup;
import com.samsung.spensdk3.example.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static com.samsung.android.sdk.composer.document.SpenContentBase.TYPE_HANDWRITING;

/**
 * Created by thuong.lt on 2017-08-10.
 */
public class ComposerSample7_1_Composer extends Activity {
    private static final String TAG = "ComposerSample7_1";
    public static final int SDK_VERSION = Build.VERSION.SDK_INT;
    public static final int REQUEST_CODE_PERMISSION = 1001;

    public static final String ARG_SDOC_PATH = "doc_path";

    private SpenComposerView mSpenComposerView;
    private ComposerContainer mComposerContainer;

    private SpenSDoc mSDoc;
    private WebManager mWebManager;
    private SpenSDocUtil mSDocUtil;
    private View mRichTextMenu;

    // temporary path
    private String mImageCache;

    private SoftInput mSoftInput;

    private Context mContext;

    private SpenColorPickerPopup mColorPickerPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Spen spen = new Spen();
        try {
            spen.initialize(this);
        } catch (SsdkUnsupportedException e) {
            if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
                // Vendor is not SAMSUNG
            } else {
                // Device is not supported
            }
        }

        setContentView(R.layout.activity_composer_writing);

        mContext = this;
        mRichTextMenu =  findViewById(R.id.bottom_layout);

        mSDocUtil = new SpenSDocUtil();
        mSoftInput = new SoftInput(mContext);
        mSoftInput.setListener(new SoftInput.Listener() {
            @Override
            public void onSoftInputStateChanged(int state) {
                Log.d(TAG, "onSoftInputStateChanged state = " + state);

                if (mSpenComposerView == null) {
                    return;
                }
            }
        });

        mSpenComposerView = (SpenComposerView) findViewById(R.id.composer_view);
        mSpenComposerView.setSpenOnlyMode(spen.isFeatureEnabled(Spen.DEVICE_PEN));
        mSpenComposerView.setClickable(true);
        mSpenComposerView.setSettingContainer((ViewGroup) findViewById(R.id.composer_setting_container));
        mSpenComposerView.setActionListner(new SpenActionListener() {
            @Override
            public void zoomIn() {
                super.zoomIn();
            }
        });

        mSpenComposerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SpenSDoc.CursorInfo cursorInfo = mSDoc.getSelectedRegionBegin();
                if (cursorInfo.index >= 0) {
                    onSelectionTextChanged();
                }
                return false;
            }
        });

        checkPermission();
        setImageCache();
        setSettingView();
        setButtonListener();
        setRichTextListener();
        setClickListener();
        setSoftInputListener();

        String loadPath = getIntent().getStringExtra(ComposerSample7_1_Composer.ARG_SDOC_PATH);
        loadSDoc(loadPath);

        mWebManager = new WebManager();
        mWebManager.setContext(getApplicationContext());

        final Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleText(intent);
            }
        }

        setScrollListener();

        VoiceManager.addStateListener(voiceCallback);

        setCursor();

    }

    VoiceManager.OnStateChanged voiceCallback = new VoiceManager.OnStateChanged() {
        @Override
        public void Play_onError(SpenContentVoice contentVoice, int errorCode) {

        }

        @Override
        public void Play_onPrepared(SpenContentVoice contentVoice, int duration) {

        }

        @Override
        public void Play_onStarted(SpenContentVoice contentVoice) {

        }

        @Override
        public void Play_onPaused(SpenContentVoice contentVoice) {

        }

        @Override
        public void Play_onResumed(SpenContentVoice contentVoice) {

        }

        @Override
        public void Play_onStopped(SpenContentVoice contentVoice) {

        }

        @Override
        public void Play_onSeekComplete(SpenContentVoice contentVoice, int position) {

        }

        @Override
        public void Play_onComplete(SpenContentVoice contentVoice) {

        }

        @Override
        public void Record_onStarted(SpenContentVoice contentVoice, String cachePath) {
            Log.d(TAG, "Record_onStarted in APP " + cachePath);
        }

        @Override
        public void Record_onStopped(SpenContentVoice contentVoice) {

        }

        @Override
        public void Record_onCancelled(SpenContentVoice contentVoice) {

        }

        @Override
        public void Record_onPaused(SpenContentVoice contentVoice) {

        }

        @Override
        public void Record_onResumed(SpenContentVoice contentVoice) {

        }

        @Override
        public void Record_onError(SpenContentVoice contentVoice, int errorCode) {

        }

        @Override
        public void Record_onUpdateTime(SpenContentVoice contentVoice, int timeInSecond) {

        }

        @Override
        public void OnInfo(SpenContentVoice contentVoice, int what) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpenComposerView != null) {
            mSpenComposerView.clearFocus();
            mSpenComposerView.requestReadyForSave(true);
            mSpenComposerView.close();
            mSpenComposerView = null;
        }
        try {
            if (mSDoc != null) {
                mSDoc.close();
                mSDoc = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mSDocUtil != null) {
            mSDocUtil.close();
        }

        if (mSoftInput != null) {
            mSoftInput.close();
        }

        VoiceManager.stopPlaying();
        VoiceManager.stopRecording();
        VoiceManager.removeStateListener(voiceCallback);
    }

    private void setSettingView() {
        mComposerContainer = (ComposerContainer) findViewById(R.id.canvasLayout);
        mComposerContainer.setListener(new ComposerContainer.SizeChangeListener() {
            @Override
            public void onSizeChangeListener(int w, int h, int oldw, int oldh) {
                Log.d(TAG, "onSizeChangeListener");

                if (mSoftInput != null) {
                    mSoftInput.onSizeChanged(h > oldh ? 1 : -1);
                }
            }
        });
    }

    private void setButtonListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpenContentBase content = null;
                switch (v.getId()) {
                    case R.id.btn_01: // writing
                        content = new SpenContentHandWriting();
                        break;

                    case R.id.btn_03: // voice
                        content = new SpenContentVoice();
                        content.setText("Name 001");
                        break;

                    case R.id.btn_04: // image
                        final SpenContentImage contentImage = new SpenContentImage();
                        //content.setThumbnailPath(mImageCache);//("/mnt/sdcard/DCIM/Screenshots/Screenshot_20160921-135422.png");
                        contentImage.setTaskStyle(SpenContentBase.TASK_NONE);
                        contentImage.setState(SpenContentBase.STATE_INIT);

                        content = contentImage;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "SpenContentImage thread run : " + mImageCache);
                                if (mImageCache == null) {
                                    setImageCache();
                                }
                                contentImage.setThumbnailPath(mImageCache);
                                contentImage.setState(SpenContentBase.STATE_DONE);
                            }
                        }, 500);
                        break;

                    case R.id.btn_05: // web
                        SpenContentWeb contentWeb = new SpenContentWeb();
                        contentWeb.setUri("http://naver.me/5ExakKi0");//("www.naver.com");
                        contentWeb.setState(SpenContentBase.STATE_INIT);
                        Log.d(TAG, "contentWeb :" + contentWeb);

                        mWebManager.createWebData(contentWeb);
                        content = contentWeb;
                        break;

                    case R.id.btn_06:
                        loadDocument();
                        break;

                    case R.id.btn_08:
                        saveNoteFile(false);
                        break;

                    default:
                        break;
                }

                if (content != null) {
                    mSDocUtil.setDocument(mSDoc);
                    ArrayList<SpenContentBase> list = new ArrayList<>();
                    list.add(content);
                    mSDocUtil.insertContents(list);

                    if (content.getType() == SpenContentBase.TYPE_HANDWRITING) {
                        mSpenComposerView.setFocus(content);
                    }
                    //mSpenComposerView.setSoftInputVisible(false);
                    mSpenComposerView.setMode(SpenComposerView.MODE_EDIT);
                }
            }
        };

        int[] ids = new int[]{
                R.id.btn_01,
                R.id.btn_03,
                R.id.btn_04,
                R.id.btn_05,
                R.id.btn_06,
                R.id.btn_08,
        };
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(listener);
            }
        }
    }

    public String saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public static final Pattern WEB_URL = Pattern.compile(
            "(((http|https|Http|Https):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)"
                    + "(?:" + Patterns.DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + Patterns.GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~\\$"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)(\\/)?"); // and finally, a word boundary or end of

    private String getSubject(Intent intent) {
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (TextUtils.isEmpty(subject)) {
            subject = "";
        }
        return subject;
    }

    private String getText(Intent intent) {
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(text)) {
            text = "";
        }

        String UNICODE_OBJ = "\ufffc";
        text = text.replace(UNICODE_OBJ, "");
        return text;
    }

    private static class SubText {

        static final int TYPE_NONE = 0;
        static final int TYPE_TEXT = 1;
        static final int TYPE_URL = 2;

        int type = TYPE_NONE;
        String data = "";

        SubText(int _type, String _data) {
            type = _type;
            data = _data;
        }
    }

    private void handleText(Intent intent) {
        String subject = getSubject(intent);
        String sharedText = getText(intent);

        Matcher matcher = WEB_URL.matcher(sharedText);

        ArrayList<SubText> subList = new ArrayList<>();
        int prevEnd = 0;
        while (matcher.find()) {
            if (prevEnd < matcher.start()) {
                String text = sharedText.substring(prevEnd, matcher.start());
                subList.add(new SubText(SubText.TYPE_TEXT, text));
            }
            subList.add(new SubText(SubText.TYPE_URL, sharedText.substring(matcher.start(), matcher.end())));
            prevEnd = matcher.end();
        }

        if (prevEnd < sharedText.length()) {
            String text = sharedText.substring(prevEnd, sharedText.length());
            subList.add(new SubText(SubText.TYPE_TEXT, text));
        }

        for (SubText sub : subList) {
            if (sub.data == null) {
                continue;
            }

            switch (sub.type) {
                case SubText.TYPE_TEXT:
                    String text = sub.data.trim();
                    if (TextUtils.isEmpty(text)) {
                        continue;
                    }
//                    pList.add(new Paragraph.Builder().setParagraphType(Paragraph.ParagraphType.Text)
//                            .setRichContent(text).create());
                    break;
                case SubText.TYPE_URL:
                    String url = sub.data;
                    if (TextUtils.isEmpty(url)) {
                        continue;
                    }

                    SpenContentWeb content = new SpenContentWeb();
                    content.setUri(url);
                    content.setState(SpenContentBase.STATE_INIT);
                    mWebManager.createWebData(content);

                    if (content != null) {
                        try {
                            mSDoc.appendContent(content);
                        } catch (SpenExceedImageLimitException e) {
                            e.printStackTrace();
                        } catch (SpenExceedTextLimitException e) {
                            e.printStackTrace();
                        }
                    }

                default:
                    break;
            }
        }
    }

    public static final String SNOTE_PATH = "/SnoteData/";

    class txtFileFilter implements FilenameFilter {
        String[] extensions;

        public txtFileFilter(String... e) {
            extensions = e;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String ext : extensions) {
                if (name.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    }

    void loadDocument() {
        final File txtFileFilesSdoc = new File(getFilesDir().getAbsolutePath() + File.separator);
        final File txtFileFilesSpd = new File(Environment.getExternalStorageDirectory() + File.separator + SNOTE_PATH);

        try {
            File[] listFileSdoc = txtFileFilesSdoc.listFiles(new txtFileFilter("sdoc"));
            File[] listFileSpd = txtFileFilesSpd.listFiles(new txtFileFilter("spd"));

            if (listFileSdoc == null) {
                listFileSdoc = new File[0];
            }
            if (listFileSpd == null) {
                listFileSpd = new File[0];
            }

            if (listFileSdoc.length > 0 || listFileSpd.length > 0) {
                int i = 0;
                final String[] mStrItems = new String[listFileSdoc.length + listFileSpd.length];
                for (File file : listFileSpd) {
                    mStrItems[i++] = file.getName();
                }
                for (File file : listFileSdoc) {
                    mStrItems[i++] = file.getName();
                }

                new AlertDialog.Builder(this).setTitle("Select note :")
                        .setItems(mStrItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (mStrItems[which].endsWith("spd")) {
                                        String path = txtFileFilesSpd.getAbsolutePath();
                                        path += File.separator + mStrItems[which];

                                        SpenContentHandWriting writing = new SpenContentHandWriting(SpenContentHandWriting.CANVAS_TYPE_FINITE);
                                        writing.attachFile(path);
                                        mSDoc.appendContent(writing);
                                    } else if (mStrItems[which].endsWith("sdoc")) {
                                        String path = txtFileFilesSdoc.getAbsolutePath();
                                        path += File.separator + mStrItems[which];

                                        createSDoc(getApplicationContext(), path, null, null);

                                        mSpenComposerView.setDocument(mSDoc);
                                        mSDocUtil.setDocument(mSDoc);
                                        mSpenComposerView.setMode(SpenComposerView.MODE_VIEW);
                                        updateLastModifiedTime();
                                    }
                                    showToastMsg("Success to load file!");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            } else {
                showToastMsg("nothing File !!!");
            }
        } catch (Exception e) {
            showToastMsg("nothing Folder !!!");
        }
    }

    private Toast mToastMessage;

    private void showToastMsg(String msg) {
        if (mToastMessage == null) {
            mToastMessage = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            mToastMessage.setText(msg);
            mToastMessage.setDuration(Toast.LENGTH_SHORT);
        }
        mToastMessage.setGravity(Gravity.CENTER, 0, 0);
        mToastMessage.show();
    }

    private void onSelectionTextChanged() {
        SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
        SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();

        boolean isBold = true, isItalic = true, isUnderline = true, isStyle = true, isForeGroundColor = true, isChecked = false;
        int taskType = SpenContentBase.TASK_NONE;

        if (begin.index != end.index || begin.pos != end.pos) {
            if (begin.index > end.index) {
                SpenSDoc.CursorInfo cursorInfo = begin;
                begin = end;
                end = cursorInfo;
            } else if (begin.index == end.index && begin.pos > end.pos) {
                int pos = begin.pos;
                begin.pos = end.pos;
                end.pos = pos;
            }

            Log.d(TAG, "check span index[" + begin.index + " ~ " + end.index + "]");
            for (int idx = begin.index; idx <= end.index; idx++) {
                SpenContentBase content = mSDoc.getContent(idx);
                // check status Task style
                if (idx == begin.index) {
                    taskType = content.getTaskStyle();
                } else {
                    if (taskType != content.getTaskStyle()) {
                        isStyle = false;
                    }
                }
                setBulletButtons(SpenContentBase.TASK_BULLET, 0);
                setBulletButtons(SpenContentBase.TASK_TODO, 0);
                setBulletButtons(SpenContentBase.TASK_NUMBER, 0);
                setBulletButtons(taskType, isStyle ? 1 : 0);

                if (content != null && content.getText() != null) {
                    int s = 0, e = content.getText().length();
                    if (idx == begin.index) {
                        s = begin.pos;
                    }
                    if (idx == end.index) {
                        e = end.pos;
                    }

                    isBold &= isAllStyle(content, s, e, SpenTextSpan.TYPE_BOLD);
                    isItalic &= isAllStyle(content, s, e, SpenTextSpan.TYPE_ITALIC);
                    isUnderline &= isAllStyle(content, s, e, SpenTextSpan.TYPE_UNDERLINE);
                    isForeGroundColor &= isAllStyle(content, s, e, SpenTextSpan.TYPE_FOREGROUND_COLOR);
                    isChecked = true;
                }
            }

            if (!isChecked) {
                isBold = false;
                isItalic = false;
                isUnderline = false;
                isForeGroundColor = false;
            }

            setSpanButtons(SpenTextSpan.TYPE_BOLD, isBold ? 1 : 0);
            setSpanButtons(SpenTextSpan.TYPE_ITALIC, isItalic ? 1 : 0);
            setSpanButtons(SpenTextSpan.TYPE_UNDERLINE, isUnderline ? 1 : 0);
            if (isForeGroundColor) {
                setSpanButtons(SpenTextSpan.TYPE_FOREGROUND_COLOR, getCurrentColor());
            } else {
                setSpanButtons(SpenTextSpan.TYPE_FOREGROUND_COLOR, Color.BLACK);
            }

        } else {
            final SpenSDoc.CursorInfo cursorInfo = mSDoc.getCursorPosition();
            if (cursorInfo.index < 0 || cursorInfo.index >= mSDoc.getContentCount()) {
                return;
            }
            final SpenContentBase contentBase = mSDoc.getContent(cursorInfo.index);
            if (contentBase != null) {

                setBulletButtons(SpenContentBase.TASK_BULLET, 0);
                setBulletButtons(SpenContentBase.TASK_TODO, 0);
                setBulletButtons(SpenContentBase.TASK_NUMBER, 0);
                setBulletButtons(contentBase.getTaskStyle(), 1);

                ArrayList<SpenTextSpan> arrSpans = contentBase.findSpan(cursorInfo.pos, cursorInfo.pos);
                setSpanButtons(SpenTextSpan.TYPE_BOLD, 0);
                setSpanButtons(SpenTextSpan.TYPE_ITALIC, 0);
                setSpanButtons(SpenTextSpan.TYPE_UNDERLINE, 0);
                setSpanButtons(SpenTextSpan.TYPE_FOREGROUND_COLOR, Color.BLACK);
                if (arrSpans != null) {
                    for (SpenTextSpan span : arrSpans) {
                        if (span.getType() == SpenTextSpan.TYPE_FOREGROUND_COLOR) {
                            setSpanButtons(SpenTextSpan.TYPE_FOREGROUND_COLOR, span.getForegroundColor());
                        } else {
                            setSpanButtons(span.getType(), span.isPropertyEnabled() ? 1 : 0);
                        }
                    }
                }
            }
        }
    }

    private void setStyle(int style) {
        mSDocUtil.setTaskStyle(style);

        SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();
        if (end.index >= 0 && end.index < mSDoc.getContentCount() && end.pos >= 0) {
            mSDoc.setCursorPosition(end);
        }
        onSelectionTextChanged();
    }

    private void setSpan(int span, int value) {
        SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
        SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();

        if (begin.index != end.index || begin.pos != end.pos) {
            if (begin.index > end.index) {
                SpenSDoc.CursorInfo cursorInfo = begin;
                begin = end;
                end = cursorInfo;
            } else if (begin.index == end.index && begin.pos > end.pos) {
                int pos = begin.pos;
                begin.pos = end.pos;
                end.pos = pos;
            }

            switch (span) {
                case SpenTextSpan.TYPE_BOLD:
                    mSDocUtil.setBold(value != 0, begin.index, begin.pos, end.index, end.pos);
                    setSpanButtons(span, value);
                    break;

                case SpenTextSpan.TYPE_ITALIC:
                    mSDocUtil.setItalic(value != 0, begin.index, begin.pos, end.index, end.pos);
                    setSpanButtons(span, value);
                    break;

                case SpenTextSpan.TYPE_UNDERLINE:
                    mSDocUtil.setUnderline(value != 0, begin.index, begin.pos, end.index, end.pos);
                    setSpanButtons(span, value);
                    break;

                case SpenTextSpan.TYPE_FOREGROUND_COLOR:
                    mSDocUtil.setTextColor(value, begin.index, begin.pos, end.index, end.pos);
                    setSpanButtons(span, value);
                    break;
            }
        } else {
            final SpenSDoc.CursorInfo cursorInfo = mSDoc.getCursorPosition();
            if (cursorInfo.index < 0 || cursorInfo.index >= mSDoc.getContentCount()) {
                return;
            }

            SpenTextSpan textSpan = null;
            final SpenContentBase contentBase = mSDoc.getContent(cursorInfo.index);
            if (contentBase != null) {
                textSpan = new SpenTextSpan(span, cursorInfo.pos, cursorInfo.pos,
                        cursorInfo.pos != 0 ? SpenTextSpan.SPAN_EXCLUSIVE_INCLUSIVE : SpenTextSpan.SPAN_INCLUSIVE_INCLUSIVE);
                if (span == SpenTextSpan.TYPE_FOREGROUND_COLOR) {
                    textSpan.setForegroundColor(value);
                } else if (value == 0) {
                    textSpan.setProrpertyEnabled(false);
                }
                contentBase.appendSpan(textSpan);
            }

            if (textSpan == null) {
                return;
            }

            setSpanButtons(span, value);
        }
    }

    private void setSpanButtons(int spanType, int value) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.rich_text_menu);
        ImageButton button = null;
        switch (spanType) {
            case SpenTextSpan.TYPE_BOLD:
                button = (ImageButton) ll.findViewById(R.id.rich_bold);
                break;

            case SpenTextSpan.TYPE_ITALIC:
                button = (ImageButton) ll.findViewById(R.id.rich_italic);
                break;

            case SpenTextSpan.TYPE_UNDERLINE:
                button = (ImageButton) ll.findViewById(R.id.rich_underline);
                break;

            case SpenTextSpan.TYPE_FOREGROUND_COLOR:
                ll.findViewById(R.id.richtext_textColor).setBackgroundColor(value);
                break;
        }

        if (button != null) {
            if (value != 0) {
                button.setBackgroundColor(Color.LTGRAY);
                button.setSelected(true);
            } else {
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setSelected(false);
            }
        }
    }

    private void setBulletButtons(int bulletType, int value) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.rich_text_menu);
        ImageButton button = null;
        switch (bulletType) {
            case SpenContentBase.TASK_TODO:
                button = (ImageButton) ll.findViewById(R.id.rich_todo);
                break;

            case SpenContentBase.TASK_DONE:
                button = (ImageButton) ll.findViewById(R.id.rich_todo);
                break;

            case SpenContentBase.TASK_NUMBER:
                button = (ImageButton) ll.findViewById(R.id.rich_number);
                break;

            case SpenContentBase.TASK_BULLET:
                button = (ImageButton) ll.findViewById(R.id.rich_bullet);
                break;

        }

        if (button != null) {
            if (value != 0) {
                button.setBackgroundColor(Color.LTGRAY);
                button.setSelected(true);
            } else {
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setSelected(false);
            }
        }
    }

    private void setRichTextListener() {
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.rich_text_menu);
                switch (v.getId()) {
                    case R.id.rich_todo:
                        setStyle(ll.findViewById(R.id.rich_todo).isSelected() ? SpenContentBase.TASK_NONE : SpenContentBase.TASK_TODO);
                        break;
                    case R.id.rich_number:
                        setStyle(ll.findViewById(R.id.rich_number).isSelected() ? SpenContentBase.TASK_NONE : SpenContentBase.TASK_NUMBER);
                        break;
                    case R.id.rich_bullet:
                        setStyle(ll.findViewById(R.id.rich_bullet).isSelected() ? SpenContentBase.TASK_NONE : SpenContentBase.TASK_BULLET);
                        break;
                    case R.id.rich_bold:
                        setSpan(SpenTextSpan.TYPE_BOLD, ll.findViewById(R.id.rich_bold).isSelected() ? 0 : 1);
                        break;
                    case R.id.rich_italic:
                        setSpan(SpenTextSpan.TYPE_ITALIC, ll.findViewById(R.id.rich_italic).isSelected() ? 0 : 1);
                        break;
                    case R.id.rich_underline:
                        setSpan(SpenTextSpan.TYPE_UNDERLINE, ll.findViewById(R.id.rich_underline).isSelected() ? 0 : 1);
                        break;
                    case R.id.rich_fontcolor:
                        showColorPickerPopup();
                        break;
                }
            }
        };
        int[] ids = new int[]{
                R.id.rich_todo,
                R.id.rich_number,
                R.id.rich_bullet,
                R.id.rich_bold,
                R.id.rich_italic,
                R.id.rich_underline,
                R.id.rich_fontcolor,
        };
        for (int id : ids) {
            View view = findViewById(id);
            view.setOnClickListener(listener);
        }
    }

    private void showColorPickerPopup() {
        if ((mColorPickerPopup != null && mColorPickerPopup.isShowing())) {
            return;
        }
        int currentColor = getCurrentColor();

        mColorPickerPopup = new SpenColorPickerPopup(mContext, currentColor);
        mColorPickerPopup.setColorPickerSpuitEnable(false);
        mColorPickerPopup.setColorPickerChangeListener(new SpenColorPickerPopup.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int color) {
                Log.d(TAG, "SpenColorPickerPopup onColorChanged " + color);
                setColor(color);
            }
        });
        mColorPickerPopup.show(mComposerContainer);
    }
    private int getCurrentColor() {
        SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
        SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();

        boolean isForeGroundColor = true;
        int currentForeGroundColor = Color.BLACK;

        if (begin.index != end.index || begin.pos != end.pos) {
            if (begin.index > end.index) {
                SpenSDoc.CursorInfo cursorInfo = begin;
                begin = end;
                end = cursorInfo;
            } else if (begin.index == end.index && begin.pos > end.pos) {
                int pos = begin.pos;
                begin.pos = end.pos;
                end.pos = pos;
            }

            for (int idx = begin.index; idx <= end.index; idx++) {
                SpenContentBase content = mSDoc.getContent(idx);
                if (content != null && content.getType() == SpenContentBase.TYPE_TEXT && content.getText() != null) {

                    int s = 0, e = content.getText().length();
                    if (idx == begin.index) {
                        s = begin.pos;
                    }
                    if (idx == end.index) {
                        e = end.pos;
                    }

                    if (idx == begin.index) {
                        ArrayList<SpenTextSpan> arrSpans = ((SpenContentText) content).findSpan(s, s);
                        if (arrSpans != null) {
                            for (SpenTextSpan span : arrSpans) {
                                if (span.getType() == SpenTextSpan.TYPE_FOREGROUND_COLOR) {
                                    currentForeGroundColor = span.getForegroundColor();
                                }
                            }
                        }
                    }

                    SpenContentText text = (SpenContentText) content;
                    isForeGroundColor &= isAllStyle(text, s, e, SpenTextSpan.TYPE_FOREGROUND_COLOR);
                }
            }

            if (!isForeGroundColor) {
                currentForeGroundColor = Color.BLACK;
            }

        } else {
            final SpenSDoc.CursorInfo cursorInfo = mSDoc.getCursorPosition();
            if (cursorInfo.index < 0 || cursorInfo.index >= mSDoc.getContentCount()) {
                return currentForeGroundColor;
            }

            final SpenContentBase contentBase = mSDoc.getContent(cursorInfo.index);
            if (contentBase != null) {
                if (contentBase instanceof SpenContentText) {
                    ArrayList<SpenTextSpan> arrSpans = ((SpenContentText) contentBase).findSpan(cursorInfo.pos, cursorInfo.pos);
                    if (arrSpans != null) {
                        for (SpenTextSpan span : arrSpans) {
                            if (span.getType() == SpenTextSpan.TYPE_FOREGROUND_COLOR) {
                                currentForeGroundColor = span.getForegroundColor();
                            }
                        }
                    }
                }
            }
        }

        return currentForeGroundColor;
    }
    private void setColor(int color) {
        SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
        SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();
        if (begin.index != end.index || begin.pos != end.pos) {
            //swap
            if (begin.index > end.index) {
                SpenSDoc.CursorInfo tmp = begin;
                begin = end;
                end = tmp;
            } else if (begin.index == end.index & begin.pos > end.pos) {
                int tmp = begin.pos;
                begin.pos = end.pos;
                end.pos = tmp;
            }
            Log.d(TAG, "setColor index[" + begin.index + " ~ " + end.index + "] color = " + color);
            Log.d(TAG, "setColor pos[" + begin.pos + " ~ " + end.pos + "] color = " + color);

            mSDocUtil.setTextColor(color, begin.index, begin.pos, end.index, end.pos);

        } else {
            SpenSDoc.CursorInfo info = mSDoc.getCursorPosition();
            if (info.index >= 0 && info.index < mSDoc.getContentCount()) {

                SpenContentBase content = mSDoc.getContent(info.index);
                if (content != null && content.getType() == SpenContentBase.TYPE_TEXT) {
                    int s = info.pos, e = info.pos;
                    Log.d(TAG, "setColor 2 index[" + info.index + " ~ " + info.index + "] span = " + color);
                    mSDocUtil.setTextColor(color, info.index, info.pos, info.index, info.pos);
                }
            }
        }

        setSpanButtons(SpenTextSpan.TYPE_FOREGROUND_COLOR,color);
    }

    private void setSoftInputListener() {
        mSpenComposerView.setSoftInputListener(new SpenSoftInputListener() {
            @Override
            public void onShowInput(boolean visible) {
                if (visible) {
                    mSoftInput.show((Activity) mContext);
                } else {
                    mSoftInput.hide((Activity) mContext);
                }
            }
        });
    }

    private void registContentListenner() {
        if (mSDoc != null) {
            mSDoc.registContentListener(this.hashCode(), new SpenSDoc.ContentEventListener() {
                @Override
                public void onContentAdded(SpenSDoc sdoc, ArrayList<SpenContentBase> contentList, ArrayList<Integer> indexList) {
                }

                @Override
                public void onContentRemoved(SpenSDoc sdoc, ArrayList<SpenContentBase> contentList, ArrayList<Integer> indexList) {
                }

                @Override
                public void onContentChanged(SpenSDoc sdoc, ArrayList<SpenContentBase> contentList, ArrayList<Integer> indexList, ArrayList<Integer> changedTypeList, ArrayList<Integer> historyPosList) {
                    onSelectionTextChanged();
                }

                @Override
                public void onThumbnailAddable(SpenSDoc sdoc, boolean addable) {
                }
            });
        }
    }

    private void setClickListener() {
        mSpenComposerView.setItemClickListener(new SpenItemClickListener() {
            @Override
            public void onClicked(SpenContentBase content) {
                if (content != null && content.getType() == SpenContentBase.TYPE_WEB) {
                    String url = ((SpenContentWeb) content).getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "ACTION_ID_CLICK_WEB_CARD", e);
                    }
                }
                mSpenComposerView.setFocus(content);
                if (content != null && content.getType() == TYPE_HANDWRITING) {
                    //mSpenComposerView.setSoftInputVisible(false);
                }
                if(mRichTextMenu != null && mRichTextMenu.getVisibility() == View.GONE){
                    mRichTextMenu.setVisibility(View.VISIBLE);
                }
                Toast.makeText(getApplicationContext(), "onItemClicked : holder", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCategoryClicked() {
                Toast.makeText(getApplicationContext(), "onItemClicked : category", Toast.LENGTH_SHORT).show();
                mSpenComposerView.clearFocus();
            }

            @Override
            public void onTitleClicked(SpenContentText content) {
                Toast.makeText(getApplicationContext(), "onItemClicked : title", Toast.LENGTH_SHORT).show();
                if(mRichTextMenu != null && mRichTextMenu.getVisibility() == View.VISIBLE){
                    mRichTextMenu.setVisibility(View.GONE);
                }
                mSpenComposerView.setFocus(content);
            }

            @Override
            public void onEmptyAreaClicked() {
                Toast.makeText(getApplicationContext(), "onItemClicked : EmptyArea", Toast.LENGTH_SHORT).show();
                if(mRichTextMenu != null && mRichTextMenu.getVisibility() == View.GONE){
                    mRichTextMenu.setVisibility(View.VISIBLE);
                }
                super.onEmptyAreaClicked();
            }

            @Override
            public void onHyperTextClicked(String hypertext, int hypertextType, int datetimeType) {

                final String STANDARD_DATE_TIME_1_SCHEME = "standard_date_time_1://";
                long time = 0;
                Boolean isAllDay = false;

                String url = new String();

                switch (hypertextType) {
                    case HYPERTEXT_TYPE_NONE:
                        break;
                    case HYPERTEXT_TYPE_URL:
                        url = makeUrl(hypertext, new String[]{"http://", "https://", "rtsp://"}, null, null);
                        break;
                    case HYPERTEXT_TYPE_EMAIL:
                        url = makeUrl(hypertext, new String[]{"mailto:"}, null, null);
                        break;
                    case HYPERTEXT_TYPE_PHONE:
                        url = makeUrl(hypertext, new String[]{"tel:"}, null, null);
                        break;
                    case HYPERTEXT_TYPE_ADDRESS: {
                        // ex) 1150 South Beverly Drive, CA 90035
                        String encodedAddress = null;
                        try {
                            encodedAddress = URLEncoder.encode(hypertext, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                        }

                        url = "geo:0,0?q=" + encodedAddress;
                    }
                    break;
                    case HYPERTEXT_TYPE_DATETIME: {
                        if (datetimeType == SpenTextSpan.DATETIME_TYPE_STANDARD_DATE) {
                            isAllDay = true;
                            url = hypertext + " 00:00"; // "yyyy/MM/dd HH:mm"
                            time = toDate(url).getTime();
                        }
                    }
                    break;
                }

                if (url.length() > 0) {
                    Log.d(TAG, "hypertext :" + url);
                    if (hypertextType == HYPERTEXT_TYPE_DATETIME) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_EDIT);
                        intent.setType("vnd.android.cursor.item/event");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(EXTRA_EVENT_BEGIN_TIME, time);
                        intent.putExtra(EXTRA_EVENT_ALL_DAY, isAllDay);

                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                        }
                    } else if (url.startsWith("http://") || url.startsWith("https://") ||
                            url.startsWith("rtsp://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                        }
                    }
                }
            }
        });

        mSpenComposerView.setCursorChangedListener(new SpenCursorChangedListener() {
            @Override
            public void onChanged() {
                SpenSDoc.CursorInfo info = mSDoc.getCursorPosition();
                SpenContentBase content = null;
                if (info.index == SpenComposerView.TITLE_HOLDER_INDEX) {
                    content = mSDoc.getTitle();

                    if (mRichTextMenu != null && mRichTextMenu.getVisibility() == View.VISIBLE) {
                        mRichTextMenu.setVisibility(View.GONE);
                    }
                } else if (info.index >= SpenComposerView.FIRST_HOLDER_INDEX) {
                    content = mSDoc.getContent(info.index);

                    if (mRichTextMenu != null && mRichTextMenu.getVisibility() == View.GONE) {
                        mRichTextMenu.setVisibility(View.VISIBLE);
                    }

                }
                if (content == null) {
                    return;
                }

                if (content.getType() == SpenContentBase.TYPE_TEXT) {
                    SpenContentText text = (SpenContentText) content;
                    ArrayList<SpenTextSpan> spans = text.getSpan();
                    if (spans != null) {
                        for (SpenTextSpan span : spans) {
                            Log.d(TAG, "onCursorChange : " + span.getType());
                        }
                    }
                }

                mSpenComposerView.setMode(SpenComposerView.MODE_EDIT);
                onSelectionTextChanged();
            }
        });

        mSpenComposerView.setContextMenuListener(new SpenContextMenuListener() {
            public static final int CONTEXT_MENU_ID_CUT = 1;
            public static final int CONTEXT_MENU_ID_COPY = 2;
            public static final int CONTEXT_MENU_ID_PASTE = 3;
            public static final int CONTEXT_MENU_ID_RESIZE_IMAGE = 4;
            public static final int CONTEXT_MENU_ID_SELECT_ALL = 5;

            @Override
            public boolean onActionItemClicked(Object mode, MenuItem item) {
                if (mSDoc == null || mSDoc.isClosed()) {
                    Log.d(TAG, "SpenContextMenuListener$onActionItemClicked, SDoc is null or closed");
                    return false;
                }

                Log.d(TAG, "SpenContextMenuListener$onActionItemClicked");
                executeMenuItem(item);
                if (item.getItemId() == CONTEXT_MENU_ID_SELECT_ALL) {
                    mSpenComposerView.startActionMode();
                } else {
                    mSpenComposerView.stopActionMode();
                }
                onSelectionTextChanged();
                return super.onActionItemClicked(mode, item);
            }

            @Override
            public boolean onCreateActionMode(Object mode, Menu menu) {
                if (mSDoc == null || mSDoc.isClosed()) {
                    Log.d(TAG, "SpenContextMenuListener$onCreateActionMode, SDoc is null or closed");
                    return false;
                }

                Log.d(TAG, "SpenContextMenuListener$onCreateActionMode");
                createContextMenu(menu);

                return super.onCreateActionMode(mode, menu);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu) {
                super.onCreateContextMenu(menu);

                if (mSDoc == null || mSDoc.isClosed()) {
                    Log.d(TAG, "SpenContextMenuListener$onCreateContextMenu, SDoc is null or closed");
                    return;
                }

                Log.d(TAG, "SpenContextMenuListener$onCreateContextMenu");
                createContextMenu(menu);
            }

            private void createContextMenu(Menu menu) {
                final boolean editable = mSpenComposerView.getMode() == SpenComposerView.MODE_EDIT;
                final SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
                final SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();

                Log.d(TAG, "SpenContextMenuListener$createContextMenu : " + begin.index + " - " + end.index);

                menu.add(Menu.NONE, CONTEXT_MENU_ID_CUT, CONTEXT_MENU_ID_CUT, "CUT")
                        .setEnabled(editable);
                menu.add(Menu.NONE, CONTEXT_MENU_ID_COPY, CONTEXT_MENU_ID_COPY, "COPY")
                        .setEnabled(mSDoc.isSelected());
                menu.add(Menu.NONE, CONTEXT_MENU_ID_PASTE, CONTEXT_MENU_ID_PASTE, "PASTE")
                        .setEnabled(editable);
                menu.add(Menu.NONE, CONTEXT_MENU_ID_SELECT_ALL, CONTEXT_MENU_ID_SELECT_ALL, "SELECT ALL")
                        .setEnabled(true);

                if (begin.index >= 0 && begin.index == end.index) {
                    switch (mSDoc.getContent(begin.index).getType()) {
                        case SpenContentBase.TYPE_HANDWRITING:
                            break;
                        case SpenContentBase.TYPE_DRAWING:
                            menu.add(Menu.NONE, CONTEXT_MENU_ID_RESIZE_IMAGE, CONTEXT_MENU_ID_RESIZE_IMAGE, "RESIZE")
                                    .setEnabled(editable);
                            break;
                        case SpenContentBase.TYPE_IMAGE:
                            menu.add(Menu.NONE, CONTEXT_MENU_ID_RESIZE_IMAGE, CONTEXT_MENU_ID_RESIZE_IMAGE, "RESIZE")
                                    .setEnabled(editable);
                            break;
                        case SpenContentBase.TYPE_VOICE:
                            break;
                    }
                }
            }

            private boolean executeMenuItem(MenuItem item) {
                final SpenSDoc.CursorInfo begin = mSDoc.getSelectedRegionBegin();
                final SpenSDoc.CursorInfo end = mSDoc.getSelectedRegionEnd();

                Log.d(TAG, "SpenContextMenuListener$executeMenuItem : " + item.getItemId() + " / " + begin.index + " - " + end.index);

                switch (item.getItemId()) {
                    case CONTEXT_MENU_ID_CUT:
                        //TODO
                        break;
                    case CONTEXT_MENU_ID_COPY:
                        //TODO
                        break;
                    case CONTEXT_MENU_ID_PASTE:
                        //TODO
                        break;
                    case CONTEXT_MENU_ID_RESIZE_IMAGE: {
                        if (!mSDoc.isSelected()) {
                            return true;
                        }
                        final SpenContentBase contentImage = mSDoc.getContent(begin.index);
                        if (contentImage != null
                                && (contentImage.getType() == SpenContentBase.TYPE_IMAGE || contentImage.getType() == SpenContentBase.TYPE_DRAWING)) {
                            mSpenComposerView.setResizeHandleVisible(true);
                            mSDoc.setCursorPosition(begin.pos > 0 ? begin : end);
                            mSoftInput.hide((Activity) mContext);
                        }
                    }
                    break;
                    case CONTEXT_MENU_ID_SELECT_ALL:
                        selectAll();
                        break;
                }

                return true;
            }

            private void selectAll() {
                final SpenSDoc.CursorInfo begin = new SpenSDoc.CursorInfo();
                final SpenSDoc.CursorInfo end = new SpenSDoc.CursorInfo();

                boolean isCursorInTitleHolder = (mSDoc.getCursorPosition().index == -1 || (mSDoc.getSelectedRegionBegin().index == -1 && mSDoc.getSelectedRegionEnd().index == -1));
                if (isCursorInTitleHolder) {
                    begin.index = SpenComposerView.TITLE_HOLDER_INDEX;
                    begin.pos = 0;

                    end.index = SpenComposerView.TITLE_HOLDER_INDEX;
                    if (mSDoc.getTitle().getText() == null) {
                        return;
                    }
                    end.pos = mSDoc.getTitle().getText().length();
                } else {
                    begin.index = 0;
                    begin.pos = 0;

                    end.index = mSDoc.getContentCount() - 1;
                    if (mSDoc.getContent(end.index) == null) {
                        return;
                    }
                    end.pos = mSDoc.getContent(end.index).getLength();
                }

                mSDoc.selectRegion(begin, end);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "finish, finishAndRemoveTask");
            if (Util.isLockTaskMode(this)) {
                stopLockTask();
            }
            finishAndRemoveTask();
        } else {
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }
    }

    public boolean createSDoc(Context context, String fileFullPath, String confirmResult, String password) {
        try {
            if (mSDoc != null) {
                mSDoc.unregistContentListener(this.hashCode());
            }
            mSDoc = new SpenSDoc(context, fileFullPath, confirmResult, password);
            registContentListenner();

        } catch (IOException e) {
            e.printStackTrace();
            return true;
        } catch (SpenSDocUnsupportedFileException e) {
            e.printStackTrace();
            return false;
        } catch (SpenSDocInvalidPasswordException e) {
            e.printStackTrace();
            return false;
        } catch (SpenSDocUnsupportedVersionException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void loadSDoc(String loadPath) {
        if (TextUtils.isEmpty(loadPath)) {
            String sdocPath = getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".sdoc";

            if(!createSDoc(getApplicationContext(),sdocPath,null,null)){
                return;
            }
        } else {
            if(!createSDoc(getApplicationContext(),loadPath,null,null)){
                return;
            }
        }
        mSDocUtil.setDocument(mSDoc);

        SpenContentText title = mSDoc.getTitle();
        if (title.getText() == null || title.getText().length() <= 0) {
            String notitle = "Title";
            title.setHintText(notitle);
            title.setHintTextColor(0xFF252525);
            title.setHintTextSize(19.f);
            title.setHintTextEnabled(true);
        }
        if (mSDoc.getContentCount() == 0) {
            SpenContentText content = new SpenContentText();
            content.setText("");
            String hintText = "Enter Text";
            content.setHintText(hintText);
            content.setHintTextColor(0xffb7b7b7);
            content.setHintTextSize(17.f);
            content.setHintTextEnabled(true);
            try {
                mSDoc.appendContent(content);
            } catch (SpenExceedImageLimitException e) {
                e.printStackTrace();
            } catch (SpenExceedTextLimitException e) {
                e.printStackTrace();
            }
            mSpenComposerView.setContentHintText(hintText);
        }

        if (TextUtils.isEmpty(loadPath)) {
            mSpenComposerView.setMode(SpenComposerView.MODE_EDIT);
        } else {
            mSpenComposerView.setMode(SpenComposerView.MODE_VIEW);
        }
        mSpenComposerView.setCategoryName("Uncategorised");
        updateLastModifiedTime();
        mSpenComposerView.setDocument(mSDoc);
    }

    private void updateLastModifiedTime() {
        String modifiedTime;
        long timeMs = mSDoc.getModifiedTime();
        if (DateUtils.isToday(timeMs)) {
            modifiedTime = android.text.format.DateFormat.getTimeFormat(getApplicationContext()).format(timeMs);
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
                modifiedTime = "\u202A" + modifiedTime + "\u202C";
            }
        } else {
            String pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMd");
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
            modifiedTime = dateFormat.format(timeMs);
        }
        mSpenComposerView.setLastModifiedTime(getResources().getString(R.string.composer_lastmodified, modifiedTime));
    }

    void save() {
        mSpenComposerView.requestReadyForSave(true);

        if (mWebManager.isRunning()) {
            mWebManager.stop();
        }

        String savePath = getIntent().getStringExtra(ComposerSample7_1_Composer.ARG_SDOC_PATH);
        if (TextUtils.isEmpty(savePath)) {
            savePath = getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".sdoc";
        }
        try {
            mSDoc.save(savePath, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }

    private boolean saveNoteFile(final boolean isClose) {
        // Prompt Save File dialog to get the file name
        // and get its save format option (note file or image).
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.save_file_sdoc_dialog, (ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        builderSave.setTitle("Enter file name");
        builderSave.setView(layout);

        final EditText inputPath = (EditText) layout.findViewById(R.id.input_path);
        inputPath.setText("Note");

        builderSave.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = inputPath.getText().toString();
                if (!fileName.equals("")) {
                    mSpenComposerView.requestReadyForSave(true);
                    updateLastModifiedTime();

                    if (mWebManager.isRunning()) {
                        mWebManager.stop();
                    }

                    String savePath = getIntent().getStringExtra(ComposerSample7_1_Composer.ARG_SDOC_PATH);
                    if (TextUtils.isEmpty(savePath)) {
                        savePath = getFilesDir().getAbsolutePath() + File.separator + fileName + ".sdoc";
                    }
                    try {
                        mSDoc.save(savePath, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isClose) {
                        finish();
                    }
                } else {
                    Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                }

            }
        });
        builderSave.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isClose) {
                    finish();
                }
            }
        });

        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();

        return true;
    }

    private void setCursor() {
        SpenSDoc.CursorInfo info = new SpenSDoc.CursorInfo();
        info.index = 0;
        info.pos = 0;
        mSDoc.setCursorPosition(info);
    }

    private int scrollY = 0;

    private void setScrollListener() {
        mSpenComposerView.setScrollListener(new SpenScrollListener() {
            @Override
            public boolean onScrollChanged(int y) {
                Log.d(TAG, "setScrollListener y:" + y);
                scrollY = y;
                return false;
            }
        });
    }

    private boolean isAllStyle(SpenContentBase content, int begin, int end, int spanType) {

        ArrayList<SpenTextSpan> list = content.findSpan(begin, end);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                SpenTextSpan span = list.get(i);
                if (span.getType() == spanType) {
                    if (begin >= span.getStart() && end <= span.getEnd()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void search() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input to search text");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(input, 0);
            }
        }, 200);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSpenComposerView.setHighlightText(input.getText().toString());
                mSpenComposerView.clearFocus();
                mSpenComposerView.requestReadyForSave(true);
                updateLastModifiedTime();
                mSpenComposerView.setMode(SpenComposerView.MODE_VIEW);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private static final String makeUrl(String url, String[] prefixes,
                                        Matcher m, Linkify.TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(m, url);
        }

        boolean hasPrefix = false;

        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0,
                    prefixes[i].length())) {
                hasPrefix = true;
                if (!url.regionMatches(false, 0, prefixes[i], 0,
                        prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }

                break;
            }
        }

        if (!hasPrefix) {
            url = prefixes[0] + url;
        }

        return url;
    }

    public Date toDate(String dateStr) throws IllegalArgumentException {

        final int DATA_ELEMENT_COUNT = 5;
        boolean isPM = false;
        Date defalutDate = new Date();

        if (dateStr == null) {
            return defalutDate;
        }

        dateStr = dateStr.toLowerCase(Locale.US);
        if (dateStr.contains("pm")) {
            isPM = true;
            dateStr = dateStr.replace("pm", "");
        } else if (dateStr.contains("am")) {
            dateStr = dateStr.replace("am", "");
        }

        dateStr = dateStr.trim();

        // D1/D2/D3 D4:D5
        dateStr = dateStr.replace('/', ' ');
        dateStr = dateStr.replace('-', ' ');
        dateStr = dateStr.replace('.', ' ');
        if (!dateStr.contains(":")) {
            dateStr = dateStr + ":00";
        } else if (dateStr.endsWith(":")) {
            dateStr = dateStr + "00";
        }
        dateStr = dateStr.replace(':', ' ');
        dateStr = dateStr.replace(',', ' ');
        dateStr = dateStr.trim().replaceAll("\\s+", " ");

        String[] dateStrArr = dateStr.split(" ");
        String[] newDateStrArr = new String[DATA_ELEMENT_COUNT];

        System.arraycopy(dateStrArr, 0, newDateStrArr, 0, dateStrArr.length);

        if (dateStrArr[0].length() < 4) {
            newDateStrArr[0] = dateStrArr[2]; // YYYY
            if (Integer.parseInt(dateStrArr[1]) > 12) {
                newDateStrArr[1] = dateStrArr[0];
                newDateStrArr[2] = dateStrArr[1];
            } else if (Integer.parseInt(dateStrArr[0]) > 12) {
                newDateStrArr[1] = dateStrArr[1];
                newDateStrArr[2] = dateStrArr[0];
            } else {
                // default is MM/DD/YYYY
                newDateStrArr[1] = dateStrArr[0];
                newDateStrArr[2] = dateStrArr[1];

                String curDateFormat = Settings.System.getString(mContext.getContentResolver(),
                        Settings.System.DATE_FORMAT);
                if ((curDateFormat != null)
                        && curDateFormat.equals("dd-MM-yyyy")) { // DD/MM/YYYY
                    newDateStrArr[1] = dateStrArr[1];
                    newDateStrArr[2] = dateStrArr[0];
                }
            }
        }

        for (int i = 1; i < newDateStrArr.length; i++) {
            if (newDateStrArr[i].length() == 1) {
                newDateStrArr[i] = "0" + newDateStrArr[i];
            }
        }

        if (isPM) {
            int hour = Integer.parseInt(newDateStrArr[3]) + 12;
            newDateStrArr[3] = "" + hour;
        }

        dateStr = newDateStrArr[0] + "/" + newDateStrArr[1] + "/" + newDateStrArr[2] + " "
                + newDateStrArr[3] + ":" + newDateStrArr[4];

        dateStr = dateStr.trim();
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try {
            date = dateFormat.parse(dateStr);
            if ("Asia/Shanghai".equals(Time.getCurrentTimezone())) {
                return date;
            }
        } catch (ParseException e) {
            date = defalutDate;
        }

        return date;
    }

    public void setImageCache() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.splashscreen_scale_200);
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "drawable");
        boolean doSave = true;
        if (!dir.exists()) {
            doSave = dir.mkdirs();
        }
        if (doSave) {
            mImageCache = saveBitmapToFile(dir, "theNameYouWant.jpg", bm, Bitmap.CompressFormat.JPEG, 80);
        } else {
            Log.e(TAG, "Couldn't create target directory.");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermission() {
        if (SDK_VERSION < 23) {
            return false;
        }
        List<String> permissionList = new ArrayList<String>(Arrays.asList(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO));
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.CAMERA)) {
            permissionList.remove(Manifest.permission.CAMERA);
        }
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
            permissionList.remove(Manifest.permission.RECORD_AUDIO);
        }
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionList.size() > 0) {
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults != null) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "permission: " + permissions[i] + " is denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
