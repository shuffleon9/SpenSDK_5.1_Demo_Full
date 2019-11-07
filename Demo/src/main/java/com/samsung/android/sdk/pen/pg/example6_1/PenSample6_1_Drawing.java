package com.samsung.android.sdk.pen.pg.example6_1;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.spensdk3.example.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class PenSample6_1_Drawing extends Activity implements DrawingSavingListener{
    public static String KEY_LOAD_FILE_PATH = "LoadPath";
    public static String KEY_IMAGE_DRAWING_PATH = "DrawingPath";
    public static String DOC_FILE_TYPE = ".spp";
    public static String PHOTO_FILE_TYPE = ".png";

    private final static String TAG = "PenSample6_1_Drawing";

    private Context mContext;

    private String mFilePath = null;
    private File mFileStorage = null;
    private DrawingFragment fr;

    LinearLayout mPaintingListLayout;
    TextView mTextStorage;

    private final int PERMISSION_REQUEST_STORAGE = 100;
    private static final int PICK_FROM_ALBUM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        mContext = this;

        setContentView(R.layout.activity_drawing);
        setWindowFlags();

        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/Drawing";
        mFileStorage = new File(mFilePath);
        if (!mFileStorage.exists()) {
            if (!mFileStorage.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mPaintingListLayout = (LinearLayout) findViewById(R.id.painting_list_layout);
        mTextStorage = (TextView) findViewById(R.id.textStorage);
        mTextStorage.setText(mFilePath);
    }

    public void onClickedButtonMenu(View view) {
        switch (view.getId()) {
            case R.id.btnNewDrawing:
                fr = new DrawingFragment();

                Bundle bundle = new Bundle();
                fr.setArguments(bundle);

                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.commit();

                break;

            case R.id.btnPhotoDrawing:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
                break;

        }
    }


    @Override
    public void onSaved() {
        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshList();
    }

    @Override
    public void onBackPressed() {
        if (fr != null && fr.isVisible()) {
            fr.close();
            refreshList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                Uri ImageUri = data.getData();
                fr = new DrawingFragment();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_IMAGE_DRAWING_PATH, getRealPathFromURI(ImageUri));
                fr.setArguments(bundle);

                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.commit();
            }
        }
    }

    // Util

    public void refreshList() {
        ArrayList<String> strItems = setFileList();
        mPaintingListLayout.removeAllViews();
        for (String str : strItems) {
            RelativeLayout layout = new RelativeLayout(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (50 * getResources().getDisplayMetrics().density));
            layout.setLayoutParams(params);
            TextView textView = new TextView(getApplicationContext());
            textView.setText(str);
            textView.setTextSize(22.0f);
            textView.setTextColor(Color.BLACK);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strFilePath = mFilePath + '/' + ((TextView)v).getText();
                    fr = new DrawingFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_LOAD_FILE_PATH, strFilePath);
                    fr.setArguments(bundle);

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_place, fr);
                    fragmentTransaction.commit();
                }
            });
            textView.setBackgroundResource(R.drawable.paintinglist_item_background);
            layout.addView(textView);
            mPaintingListLayout.addView(layout);
        }
    }

    private ArrayList<String> setFileList() {
        // Filter in spainting and png files.
        File[] fileList = mFileStorage.listFiles(new txtFileFilter());
        if (fileList == null) {
            Toast.makeText(mContext, "File does not exist.", Toast.LENGTH_SHORT).show();
            return null;
        }

        ArrayList<String> strFileList = new ArrayList<>();
        for (File file: fileList)
            strFileList.add(file.getName());

        return strFileList;
    }

    static class txtFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(DOC_FILE_TYPE));// || name.endsWith(PHOTO_FILE_TYPE));
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void setWindowFlags() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

        getWindow().setAttributes(lp);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Log.i(TAG, "CheckPermission : " + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showToastMsg("Read/Write external storage");
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        } else {
            Log.e(TAG, "permission deny");
        }
    }

    private void showToastMsg(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
