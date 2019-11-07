package com.samsung.android.sdk.pen.pg.example6_1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenInvalidPasswordException;
import com.samsung.android.sdk.pen.document.SpenPaintingDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenPaintingSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenPaintingViewInterface;
import com.samsung.android.sdk.pen.engine.SpenZoomListener;
import com.samsung.android.sdk.pen.settingui.SpenSettingBrushLayout;
import com.samsung.android.spr.drawable.Spr;
import com.samsung.spensdk3.example.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class DrawingFragment extends Fragment {
    private final static String TAG = "DrawingFragment";

    private Fragment mine;
    private Context mContext;

    private SpenPaintingSurfaceView mSpenView;
    private SpenPaintingDoc mSpenPaintingDoc;

    private DrawingSettingView mDrawingSettingView;
    private SpenSettingBrushLayout mBrushSetting;
    private SpenSettingPenInfo mCurrentPenInfo;

    private int mScreenWidth, mScreenHeight;

    private ImageView mEraserButton, mUndoButton, mRedoButton, mSpuitButton;
    private View mDrawingFragment;
    private RelativeLayout mCanvaslayout;
    private TextView mDoneButton;

    private String mFilePath;
    private String mOpenFilePath;
    private String mImagePath;
    private File mFileStorage;

    private DrawingSavingListener mDrawingSavingListener;
    private FrameLayout mFrameLayout;
    private RelativeLayout mToolbar;
    private FrameLayout mBrushParentView, mSettingContainer;

    private Configuration mConfiguration;

    private static float BRUSH_SETTING_RATIO_TAB = 0.87f;
    private static float BRUSH_SETTING_RATIO_PHONE = 0.758f;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mine = this;
        mFrameLayout = new FrameLayout(getActivity());

        setWindowFlags();

        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Rect outSize = new Rect();
        display.getRectSize(outSize);
        int screenWidth = outSize.width();
        int screenHeight = outSize.height();
        mScreenWidth = screenWidth;
        Log.d(TAG, "onCreateView mScreenWidth=" + mScreenWidth);
        Log.d(TAG, "onCreateView mScreenHeight=" + mScreenHeight);

        int menuHeight = (int) getDimensionPixelSize(R.dimen.toolbar_drawing_height);
        mScreenHeight = screenHeight - menuHeight;
        Log.d(TAG, "onCreateView menuHeight=" + menuHeight);
        int palleteLayoutHeight = screenHeight - mScreenHeight - menuHeight;
        Log.d(TAG, "onCreateView palleteLayoutHeight=" + palleteLayoutHeight);

        mDrawingFragment = inflater.inflate(R.layout.fragment_drawing1, container, false);
        mToolbar = (RelativeLayout) mDrawingFragment.findViewById(R.id.toolbar);
        mDoneButton = (TextView) mDrawingFragment.findViewById(R.id.drawingOkBtn);
        mDoneButton.setOnClickListener(mDoneBtnListener);

        initButtons();

        // Init SpenSDK
        Spen spen = new Spen();
        try {
            spen.initialize(mContext, 5);
        } catch (SsdkUnsupportedException e) {
            if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
                // Vendor is not SAMSUNG
            } else {
                // Device is not supported
            }
        }

        mSpenView = new SpenPaintingSurfaceView(mContext);
        mSpenView.setZoom(0, 0, getFitZoomRatio(isLandscapeDefault(mContext)));
        mSpenView.setToolTypeAction(SpenPaintingViewInterface.TOOL_FINGER, SpenPaintingViewInterface.ACTION_STROKE);
        mSpenView.setToolTypeAction(SpenPaintingViewInterface.TOOL_SPEN, SpenPaintingViewInterface.ACTION_STROKE);
        mSpenView.setColorPickerListener(mColorPickerListener);
        mSpenView.setZoomListener(mZoomListener);

        mCanvaslayout = (RelativeLayout) mDrawingFragment.findViewById(R.id.canvasLayout);

        return mFrameLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle extra = getArguments();
        mOpenFilePath = extra.getString(PenSample6_1_Drawing.KEY_LOAD_FILE_PATH);
        mImagePath = extra.getString(PenSample6_1_Drawing.KEY_IMAGE_DRAWING_PATH);
        // New painting doc

        mCanvaslayout.addView(mSpenView);

        if (mOpenFilePath != null) {
            // Open painting doc
            Log.d(TAG, "mOpenFilePath = " + mOpenFilePath);
            try {
                mSpenPaintingDoc = new SpenPaintingDoc(mContext, mOpenFilePath, null, mScreenWidth, SpenPaintingDoc.MODE_WRITABLE);
                mSpenView.setPaintingDoc(mSpenPaintingDoc, true);
            } catch (IOException e) {
                Toast.makeText(mContext, "Cannot open this file.", Toast.LENGTH_LONG).show();
                return;
            } catch (SpenUnsupportedTypeException e) {
                Toast.makeText(mContext, "This file is not supported.", Toast.LENGTH_LONG).show();
                return;
            } catch (SpenInvalidPasswordException e) {
                Toast.makeText(mContext, "This file is locked by a password.", Toast.LENGTH_LONG).show();
                return;
            } catch (SpenUnsupportedVersionException e) {
                Toast.makeText(mContext, "This file is the version that does not support.",
                        Toast.LENGTH_LONG).show();
                return;
            } catch (Exception e) {
                Toast.makeText(mContext, "Failed to load painting doc.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            try {
                mSpenPaintingDoc = new SpenPaintingDoc(mContext, mScreenWidth, mScreenHeight, null);
                mSpenView.setPaintingDoc(mSpenPaintingDoc, true);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (mImagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);
                if (bitmap != null) {
                    mSpenView.setLayerBackgroundBitmap(mSpenPaintingDoc.getCurrentLayerId(), bitmap, SpenPaintingDoc.BACKGROUND_IMAGE_MODE_FIT);
                    bitmap.recycle();
                }
            }
        }

        initSettingView();
        mDrawingSettingView.getBrushPenSettingLayout().setEraserListener(new EraserListener());
        mSpenPaintingDoc.setHistoryListener(mHistroyListener);
        mConfiguration = getActivity().getResources().getConfiguration();
        adjustBrushSettingLayout(mConfiguration);
    }

    private void initButtons() {
        Drawable d = Spr.getDrawable(getResources(), R.drawable.drawing_toolbar_ic_eraser, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            d.setTint(getResources().getColor(R.color.drawing_toolbar_icon_tint_color));
        }

        mEraserButton = (ImageView) mDrawingFragment.findViewById(R.id.drawingEraserBtn);
        mEraserButton.setImageDrawable(d);
        mEraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpenSettingPenInfo penInfo = mDrawingSettingView.getPenInfo();
                if (!penInfo.name.contains("Eraser")) {
                    mCurrentPenInfo.name = penInfo.name;
                    mCurrentPenInfo.size = penInfo.size;
                    mCurrentPenInfo.color = penInfo.color;
                    mCurrentPenInfo.isCurvable = penInfo.isCurvable;
                }
                mDrawingSettingView.getBrushPenSettingLayout().setEraserPen();
            }
        });

        d = Spr.getDrawable(getResources(), R.drawable.drawing_toolbar_icon_undo_mtrl, null);
        mUndoButton = (ImageView) mDrawingFragment.findViewById(R.id.drawingUndoBtn);
        mUndoButton.setImageDrawable(d);
        mUndoButton.setOnClickListener(mUndoBtnListener);
        mUndoButton.setOnLongClickListener(mUndoBtnLongListener);
        setButtonDimEnabled(mUndoButton, R.drawable.drawing_toolbar_icon_undo_mtrl, true);

        d = Spr.getDrawable(getResources(), R.drawable.drawing_toolbar_icon_redo_mtrl, null);
        mRedoButton = (ImageView) mDrawingFragment.findViewById(R.id.drawingRedoBtn);
        mRedoButton.setImageDrawable(d);
        mRedoButton.setOnClickListener(mRedoBtnListener);
        mRedoButton.setOnLongClickListener(mRedoBtnLongListener);
        setButtonDimEnabled(mRedoButton, R.drawable.drawing_toolbar_icon_redo_mtrl, true);

        mFrameLayout.addView(mDrawingFragment);
    }

    private void initSettingView() {
        HashMap<String, Integer> settingResourcesInt = new HashMap<String, Integer>();
        HashMap<String, String> fontNameMap = new HashMap<String, String>();
        mDrawingSettingView = new DrawingSettingView(getActivity().getApplicationContext(), settingResourcesInt, fontNameMap, "", mCanvaslayout);

        mBrushSetting = mDrawingSettingView.getBrushPenSettingLayout();
        mBrushParentView = ((FrameLayout) mDrawingFragment.findViewById(R.id.drawing_brush_setting));
        mBrushParentView.addView(mBrushSetting);

        mBrushParentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i != i4 || i1 != i5 || i2 != i6 || i3 != i7)
                    adjustBrushSettingLayout(mConfiguration);
            }
        });

        mSettingContainer = (FrameLayout) mDrawingFragment.findViewById(R.id.setting_container);

        mDrawingSettingView.setSpenView(mSpenView);

        SpenSettingPenInfo info = new SpenSettingPenInfo();
        info.name = "com.samsung.android.sdk.pen.pen.preload.OilBrush3";
        info.color = Color.RED;
        info.size = 20 * mScreenWidth / 360;
        mDrawingSettingView.setPenInfo(info);

        mCurrentPenInfo = new SpenSettingPenInfo();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged orientation : " + newConfig.orientation);
        super.onConfigurationChanged(newConfig);
        mConfiguration = newConfig;
//        onRotateCanvas(newConfig);
//        float targetZoom = getTargetZoom();
//        float currentZoom = getZoomRatio();
//
//        if (targetZoom != 0.0f && currentZoom != targetZoom) {
//            mSpenView.setZoom(0, 0, targetZoom);
//        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        // Set the save directory for the file.
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/Drawing";
        mFileStorage = new File(mFilePath);
        if (!mFileStorage.exists()) {
            if (!mFileStorage.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Activity activity = getActivity();
        try {
            mDrawingSavingListener = (DrawingSavingListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + ". Must implement DrawingSavingListener interface");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSpenView != null) {
            mSpenView.close();
            mSpenView = null;
        }
        if (mSpenPaintingDoc != null) {
            try {
                mSpenPaintingDoc.discard();
                mSpenPaintingDoc = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setWindowFlags() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

        getActivity().getWindow().setAttributes(lp);
    }

    private float getDimensionPixelSize(int resID) {
        Resources resources = mContext.getResources();
        return resources.getDimensionPixelSize(resID);
    }

    private void setButtonDimEnabled(final ImageView v, int resId, boolean enable) {
        final Drawable d = Spr.getDrawable(getResources(), resId, null);
        if (v == null || d == null) {
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (enable) {
                d.setTint(getResources().getColor(R.color.drawing_color_gray_start));
            } else {
                d.setTint(getResources().getColor(R.color.drawing_toolbar_icon_tint_color));
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setImageDrawable(d);
            }
        });
    }

    private final View.OnClickListener mUndoBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mSpenView.closeControl();
            if (mSpenPaintingDoc.isUndoable()) {
                SpenPaintingDoc.HistoryUpdateInfo[] userData = mSpenPaintingDoc.undo();
                mSpenView.updateUndo(userData);
            }
            mSpenView.update();
        }
    };
    private final View.OnClickListener mRedoBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mSpenView.closeControl();
            if (mSpenPaintingDoc.isRedoable()) {
                SpenPaintingDoc.HistoryUpdateInfo[] userData = mSpenPaintingDoc.redo();
                mSpenView.updateRedo(userData);
            }
            mSpenView.update();
        }
    };

    private class EraserListener implements SpenSettingBrushLayout.EventListener {
        @Override
        public void onClearAll() {
            if (mSpenPaintingDoc == null) {
                return;
            }

            mSpenPaintingDoc.removeAllObject();
            mSpenView.update();
            mSpenView.setToolTypeAction(SpenPaintingViewInterface.TOOL_FINGER, SpenPaintingViewInterface.ACTION_STROKE);
            mSpenView.setToolTypeAction(SpenPaintingViewInterface.TOOL_SPEN, SpenPaintingViewInterface.ACTION_STROKE);

            if (mCurrentPenInfo != null) {
                mDrawingSettingView.setPenInfo(mCurrentPenInfo);
            }
        }
    }

    private void saveToPNG(String filepath, Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(mContext, "Capture failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            Log.d(TAG, "SaveToPNG - path: " + filepath);
            FileOutputStream outFs = new FileOutputStream(new File(filepath), false);
            outFs.write(byteArray);
            outFs.close();

        } catch (IOException e) {
            File tmpFile = new File(filepath);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            File tmpFile = new File(filepath);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        bitmap.recycle();
        mDrawingSavingListener.onSaved();
    }

    public void close() {
        if (mSpenPaintingDoc != null && mSpenPaintingDoc.isChanged() == true) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
            dlg.setTitle(mContext.getResources().getString(R.string.app_name))
                    .setMessage("Do you want to exit after save?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            savePainting();
                            dialog.dismiss();
                        }
                    }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.remove(mine);
                    fragmentTransaction.commit();
                    dialog.dismiss();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.remove(mine);
            fragmentTransaction.commit();
        }
    }

    private boolean savePainting() {
        // Prompt Save File dialog to get the file name and get its save format option (note file or image).
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.save_painting_dialog, (ViewGroup) getActivity().findViewById(R.id.layout_root));

        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        builderSave.setTitle("Enter file name");
        builderSave.setView(layout);

        final EditText inputPath = (EditText) layout.findViewById(R.id.input_path);
        inputPath.setText("Paint-" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date()));

        builderSave.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final RadioGroup selectFileExt = (RadioGroup) layout.findViewById(R.id.radioGroup);
                        Bitmap bitmap = null;
                        String fileName = inputPath.getText().toString();
                        String saveFilePath =  mFilePath + '/' + fileName;

                        if (!fileName.equals("")) {
                            if (mSpenPaintingDoc != null) {
                                if (mSpenPaintingDoc.isChanged()) {
                                    int checkRadioButtonId = selectFileExt.getCheckedRadioButtonId();
                                    if (checkRadioButtonId == R.id.radioNote) {
                                        saveFilePath += PenSample6_1_Drawing.DOC_FILE_TYPE;
                                        try {
                                            bitmap = mSpenView.capturePage(1.0f, SpenPaintingSurfaceView.CAPTURE_FOREGROUND_ALL);
                                            mSpenPaintingDoc.setForegroundImage(bitmap);
                                            mSpenPaintingDoc.save(saveFilePath);
                                            bitmap.recycle();
                                            mDrawingSavingListener.onSaved();
                                            Toast.makeText(mContext, "Success to save to .spp file, path : "+saveFilePath, Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "Fail to save to .spp file", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else if (checkRadioButtonId == R.id.radioImage) {
                                        saveFilePath += PenSample6_1_Drawing.PHOTO_FILE_TYPE;
                                        bitmap = mSpenView.capturePage(1.0f, SpenPaintingSurfaceView.CAPTURE_FOREGROUND_ALL);
                                        saveToPNG(saveFilePath, bitmap);
                                        Toast.makeText(mContext, "Success to save to .png file, path : "+saveFilePath, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.d(TAG, "PaintingDoc is not changed");
                                }
                            }
                        } else {
                            Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                        }

                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.remove(mine);
                        fragmentTransaction.commit();
                    }
                }

        );
        builderSave.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );

        if (mSpenPaintingDoc != null) {
            if (mSpenPaintingDoc.isChanged()) {
                AlertDialog dlgSave = builderSave.create();
                dlgSave.show();
            } else {
                Log.d(TAG, "PaintingDoc is not changed");

                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.hide(mine);
                fragmentTransaction.commit();
            }
        }
        return true;
    }


    private final View.OnClickListener mDoneBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            savePainting();
        }
    };


    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mBrushSetting != null) {
                SpenSettingPenInfo penInfo = mBrushSetting.getInfo();
                penInfo.color = color;
                mBrushSetting.setInfo(penInfo);
            }
        }
    };

    private final SpenZoomListener mZoomListener = new SpenZoomListener() {
        @Override
        public void onZoom(float panX, float panY, float ratio) {
            Log.d(TAG, "onZoom :" + ratio);
        }
    };

    private final View.OnLongClickListener mUndoBtnLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mSpenPaintingDoc != null) {
                mSpenView.closeControl();
                mSpenView.commitStroke();
                if (mSpenPaintingDoc.isUndoable()) {
                    SpenPaintingDoc.HistoryUpdateInfo[] userData = mSpenPaintingDoc.undoAll();
                    mSpenView.updateUndo(userData);
                }
                mUndoButton.setEnabled(mSpenPaintingDoc.isUndoable());
                mRedoButton.setEnabled(mSpenPaintingDoc.isRedoable());
            }
            return false;
        }
    };
    private final View.OnLongClickListener mRedoBtnLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mSpenPaintingDoc != null) {
                mSpenView.closeControl();
                mSpenView.commitStroke();
                if (mSpenPaintingDoc.isRedoable()) {
                    SpenPaintingDoc.HistoryUpdateInfo[] userData = mSpenPaintingDoc.redoAll();
                    mSpenView.updateRedo(userData);
                }
                mUndoButton.setEnabled(mSpenPaintingDoc.isUndoable());
                mRedoButton.setEnabled(mSpenPaintingDoc.isRedoable());
            }
            return false;
        }
    };

    private SpenPaintingDoc.HistoryListener mHistroyListener = new SpenPaintingDoc.HistoryListener() {
        @Override
        public void onCommit(SpenPaintingDoc spenPaintingDoc) {

        }

        @Override
        public void onUndoable(SpenPaintingDoc spenPaintingDoc, boolean undoable) {
            setButtonDimEnabled(mUndoButton, R.drawable.drawing_toolbar_icon_undo_mtrl, !undoable);
            mUndoButton.setEnabled(undoable);

        }

        @Override
        public void onRedoable(SpenPaintingDoc spenPaintingDoc, boolean redoable) {
            setButtonDimEnabled(mRedoButton, R.drawable.drawing_toolbar_icon_redo_mtrl, !redoable);
            mRedoButton.setEnabled(redoable);
        }
    };


    // Zoom setting
    private float mTargetZoom;
    private DisplayMetrics mOutMetrics;

    private static Boolean sIsLandscapeDefault = null;
    private final static Object sLockLandscapeDefault = new Object();

    public static final float MIN_ZOOM_RATION_PORTRAIT = 0.8f;
    public static final float DEFAULT_ZOOM_RATION_PORTRAIT = 1.0f;

    public float getZoomRatio() {
        return mSpenView.getZoomRatio();
    }

    public float getTargetZoom() {
        return mTargetZoom;
    }

    public void setTargetZoom(float ratio) {
        mTargetZoom = ratio;
    }

    public int getCurrentPageWidth() {
        return (null != mSpenPaintingDoc) ? mSpenPaintingDoc.getWidth() : 0;
    }

    public int getCurrentPageHeight() {
        return (null != mSpenPaintingDoc) ? mSpenPaintingDoc.getHeight() : 0;
    }

    private void getRatioForNoteFitToScreen() {
        setTargetZoom(getFitZoomRatio(false));
    }

    public float getFitZoomRatio(boolean isTablet) {
        if (mOutMetrics == null) {
            mOutMetrics = new DisplayMetrics();
        }

        float ratio;
        float pageWidth = (float) getCurrentPageWidth();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mOutMetrics);

        if (isTablet) {
            boolean isLandscape = isLandscapeTemplateCurrentNote();
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isLandscape) {
                    ratio = mOutMetrics.heightPixels / pageWidth;
                } else {
                    ratio = mOutMetrics.widthPixels / pageWidth;
                }
            } else {
                if (isLandscape) {

                    ratio = mOutMetrics.widthPixels / pageWidth;
                } else {
                    ratio = mOutMetrics.heightPixels / pageWidth;
                }
            }
        } else {
            ratio = mOutMetrics.widthPixels / pageWidth;
        }

        return ratio;
    }

    public final boolean isLandscapeTemplateCurrentNote() {
        return mSpenPaintingDoc != null && mSpenPaintingDoc.getOrientation() == SpenPaintingDoc.ORIENTATION_LANDSCAPE;
    }

    public static boolean isLandscapeDefault(Context context) {
        if (sIsLandscapeDefault == null) {
            synchronized (sLockLandscapeDefault) {
                if (sIsLandscapeDefault == null) {
                    initLandscapeDefault(context);
                }
            }
        }
        return sIsLandscapeDefault;
    }

    private static void initLandscapeDefault(Context context) {
        if (context == null) {
            return;
        }
        Display display = ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                sIsLandscapeDefault = size.x > size.y ? true : false;
                break;

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                sIsLandscapeDefault = size.x > size.y ? false : true;
                break;
        }
    }

    void onRotateCanvas(Configuration newConfig) {
        if (isLandscapeDefault(mContext)) {
            if (isLandscapeTemplateCurrentNote()) {
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (mSpenView.getZoomRatio() < MIN_ZOOM_RATION_PORTRAIT) {
                        setTargetZoom(MIN_ZOOM_RATION_PORTRAIT);
                    } else {
                        setTargetZoom(mSpenView.getZoomRatio());
                    }
                }
            } else {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mSpenView.getZoomRatio() < MIN_ZOOM_RATION_PORTRAIT) {
                        setTargetZoom(MIN_ZOOM_RATION_PORTRAIT);
                    } else {
                        setTargetZoom(mSpenView.getZoomRatio());
                    }
                }
            }
        } else {
            if (!isLandscapeTemplateCurrentNote()) {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    float FitRatio = (float) getCurrentPageHeight() / (float) getCurrentPageWidth();
                    //if (Math.abs(getZoomRatio() - FitRatio) < .0000001) {
                    if (Math.abs(mSpenView.getZoomRatio() - FitRatio) < .15) {
                        getRatioForNoteFitToScreen();
                    } else if (getZoomRatio() < MIN_ZOOM_RATION_PORTRAIT) {
                        setTargetZoom(MIN_ZOOM_RATION_PORTRAIT);
                    } else {
                        setTargetZoom(getZoomRatio());
                    }
                } else {
                    if (Math.abs(getZoomRatio() - DEFAULT_ZOOM_RATION_PORTRAIT) < .0000001) {
                        getRatioForNoteFitToScreen();
                    } else {
                        setTargetZoom(getZoomRatio());
                    }
                }
            } else {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (Math.abs(getZoomRatio() - DEFAULT_ZOOM_RATION_PORTRAIT) < .0000001) {
                        getRatioForNoteFitToScreen();
                    } else {
                        setTargetZoom(getZoomRatio());
                    }
                } else {
                    if (getZoomRatio() <= MIN_ZOOM_RATION_PORTRAIT) {
                        setTargetZoom(DEFAULT_ZOOM_RATION_PORTRAIT);
                    } else {
                        setTargetZoom(getZoomRatio());
                    }
                }
            }
        }
    }

    public void adjustBrushSettingLayout(Configuration configuration) {
        ViewGroup.LayoutParams params = mBrushSetting.getLayoutParams();
        RelativeLayout.LayoutParams toolbarParams = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams) mSettingContainer.getLayoutParams();
        Rect rect = getRect();
        int width = rect.width() < rect.height() ? rect.width() : rect.height();
        mBrushSetting.measure(0, 0);
        float scale = 1f, toolbarScale = 1f;
        if (isTablet()) {
            scale = ((float) width * BRUSH_SETTING_RATIO_TAB) / (float) mBrushSetting.getMeasuredWidth();
            toolbarScale = 1 - BRUSH_SETTING_RATIO_TAB;
        } else {
            scale = ((float) width * BRUSH_SETTING_RATIO_PHONE) / (float) mBrushSetting.getMeasuredWidth();
            toolbarScale = 1 - BRUSH_SETTING_RATIO_PHONE;
        }

        mBrushSetting.setPivotX(0);
        mBrushSetting.setPivotY(0);
        mBrushSetting.setScaleX(scale);
        mBrushSetting.setScaleY(scale);

        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mBrushSetting.setRotation(0);
            mBrushSetting.setY(0);
            mBrushSetting.setPenDegree(0);
            mBrushSetting.setSelectorDegree(0);

            toolbarParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            toolbarParams.width = (int) (width * toolbarScale);
            toolbarParams.height = (int) (mBrushSetting.getMeasuredHeight() * scale);
            toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            containerParams.leftMargin = 0;
            containerParams.topMargin = toolbarParams.height;

        } else {
            mBrushSetting.setRotation(-90);
            int calculatedY = rect.height();
            mBrushSetting.setY(calculatedY);
            mBrushSetting.setPenDegree(180);
            mBrushSetting.setSelectorDegree(90);

            toolbarParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            toolbarParams.width = (int) (mBrushSetting.getMeasuredHeight() * scale);
            toolbarParams.height = (int) (width * toolbarScale);
            toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);


            containerParams.leftMargin = toolbarParams.width;
            containerParams.topMargin = 0;
        }
        params.width = mBrushSetting.getMeasuredWidth();
        params.height = mBrushSetting.getMeasuredHeight();
        mBrushSetting.setLayoutParams(params);
        mToolbar.setLayoutParams(toolbarParams);
        mSettingContainer.setLayoutParams(containerParams);
    }

    private Rect getRect() {
        Rect rect = new Rect(0, 0, mBrushParentView.getWidth(), mBrushParentView.getHeight());
        return rect;
    }

    private boolean isTablet() {
        DisplayMetrics dmGlobal = mContext.getApplicationContext().getResources().getDisplayMetrics();
        float width = dmGlobal.widthPixels < dmGlobal.heightPixels ? dmGlobal.widthPixels : dmGlobal.heightPixels;
        if (width / dmGlobal.density >= 600f)
            return true;
        return false;
    }
}
