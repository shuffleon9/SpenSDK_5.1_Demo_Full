package com.samsung.android.sdk.pen.pg.utils.web.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Parcel;
import android.provider.MediaStore;
import android.text.GetChars;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by EUNJI on 2016-09-29.
 */
public class Util {
    public static final int KEYBOARD_BT = 0x02;
    public static final int KEYBOARD_USB = 0x04;

    // Got from SDL android/frameworks/base/core/java/android/provider/Settings.java
    public static final String SETTINGS_SYSTEM_ULTRA_POWERSAVING_MODE = "ultra_powersaving_mode";

    private static final String TAG = "Util";

    private static final long MINIMUM_AVAILABLE_MEMORY_SIZE_FOR_NEW_MEMO = 1024 * 1024 * 100; //100MB

    private static final long FORCE_TO_SHOW_INPUT_RETRY_INTERVAL = 100;

    private static final float LIMIT_BATTERY_LEVEL_FOR_SYNC = 15;

//    private static SimpleDateFormat FOLDER_DATAFORMAT = new SimpleDateFormat("_yyMMdd_HHmmss_SSS", Locale.ENGLISH);
    private static SimpleDateFormat FILE_DATAFORMAT = new SimpleDateFormat("_yyMMdd_HHmmss_SSS.", Locale.ENGLISH);

//    private static Toast mToast;

//    private static int mForceToShowInputTryCount = 0;

//    public static final String ON_DEVICE_HELP_PACKAGE_HLEPHUB = "com.samsung.helphub";
//    public static final String ON_DEVICE_HELP_PACKAGE_HLEPHUB_HELP = "com.samsung.helphub.HELP";
//    public static final String ON_DEVICE_HELP_HLEPHUB_SECTION = "helphub:section";
//    public static final String ON_DEVICE_HELP_NOTES = "notes";


//    public static int getRandomNumberInRange(int min, int max) {
//        if (min >= max) {
//            throw new IllegalArgumentException("max must be greater than min");
//        }
//
//        Random r = new Random();
//        return r.nextInt((max - min) + 1) + min;
//    }
//
//    public static String getVersionInfo(Context context) {
//        String version;
//
//        PackageInfo packageInfo;
//        try {
//            packageInfo = context.getApplicationContext()
//                    .getPackageManager()
//                    .getPackageInfo(
//                            context.getApplicationContext().getPackageName(),
//                            0
//                    );
//            version = packageInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            version = "Unknown";
//        }
//        return version;
//    }
//
//    public static boolean isPackageInstalled(Context context, String packageName) {
//        PackageManager packageManager = context.getApplicationContext().getPackageManager();
//        try {
//            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
//            return true;
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//    }
//
//    public static int getVersionCode(Context context, String packageName) {
//        try {
//            PackageInfo packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
//            return packageInfo.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            return -1;
//        }
//    }
//
//    public static void hideSoftInput(Activity activity, View v) {
//        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (v != null)
//            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//    }
//
//    public static void forceHideSoftInput(Context context) {
//        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) {
//            try {
//                Method forceHideSoftInput = inputMethodManager.getClass().getMethod("forceHideSoftInput");
//                forceHideSoftInput.invoke(inputMethodManager);
//            } catch (Exception e) {
//                com.samsung.android.app.notes.drawingobject.util.Log.d(TAG, "Exception: " + e.getMessage());
//            }
//        }
//    }

    private static Class<?> InputMethodManagerClass = null;
    private static Method minimizeSoftInput = null;

//    public static boolean minimizeSoftInput(Activity activity, View V, boolean forceHide) {
//        boolean result = true;
//        try {
//            Method getSystemServiceMethod = Context.class.getMethod("getSystemService", String.class);
//            Object imm = getSystemServiceMethod.invoke(activity, Context.INPUT_METHOD_SERVICE);
//
//            if (InputMethodManagerClass == null) {
//                InputMethodManagerClass = Class.forName("android.view.inputmethod.InputMethodManager");
//            }
//            if (minimizeSoftInput == null) {
//                minimizeSoftInput = InputMethodManagerClass.getMethod("minimizeSoftInput", IBinder.class, int.class);
//            }
//            if (V != null)
//                minimizeSoftInput.invoke(imm, V.getWindowToken(), 22);
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (forceHide) {
//                hideSoftInput(activity, V);
//            }
//            result = false;
//        } finally {
//            return result;
//        }
//    }
//
//    public static void hideMobileSoftInput(Context context, View v) {
//
//        InputMethodManager imm = (InputMethodManager)
//                context.getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        if (imm != null) {
//            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//        }
//    }
//
//    public static void hideMobileSoftInput(Activity activity, View v) {
//        try {
//            Method getSystemServiceMethod = Context.class.getMethod("getSystemService", String.class);
//            Object imm = getSystemServiceMethod.invoke(activity, Context.INPUT_METHOD_SERVICE);
//            Class<?> inputMethodManager = Class.forName("android.view.inputmethod.InputMethodManager");
//            Method hideSoftInputFromWindow = inputMethodManager.getMethod("hideSoftInputFromWindow", IBinder.class, int.class);
//            hideSoftInputFromWindow.invoke(imm, v.getWindowToken(), 0);
//
//            Method focusOut = inputMethodManager.getMethod("focusOut", View.class);
//            focusOut.invoke(imm, v);
//        } catch (Exception e) {
//            Log.d(TAG, "" + e.getMessage());
//        }
//    }
//
//    public static void showInput(Context context, View v) {
//        Log.d(TAG, "showInput");
//        if (context == null) {
//            return;
//        }
//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(v, 0);
//    }
//
//    public static void forceToShowInput(final Activity activity, final View v, final long timeout) {
//        Log.d(TAG, "forceToShowInput");
//        if (activity == null || v == null) {
//            return;
//        }
//        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (!imm.showSoftInput(v, 0)) {
//            mForceToShowInputTryCount = 1;
//            v.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d(TAG, "TEST forceToShowInput Run count : " + mForceToShowInputTryCount);
//                    if (!imm.showSoftInput(v, 0)) {
//
//                        if (mForceToShowInputTryCount * FORCE_TO_SHOW_INPUT_RETRY_INTERVAL < timeout) {
//
//                            mForceToShowInputTryCount++;
//                            v.postDelayed(this, FORCE_TO_SHOW_INPUT_RETRY_INTERVAL);
//                        }
//                    }
//                }
//            }, FORCE_TO_SHOW_INPUT_RETRY_INTERVAL);
//        }
//    }
//
//    public static boolean isKeyboardConnected(Context context) {
//        if(isBtKeyboardConnected(context) || isUsbKeyboardConnected(context)) {
//            return true;
//        }
//        return false;
//    }

//    public static boolean isBtKeyboardConnected(Context context) {
//        if (isAccessoryKeyboardState((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)) == KEYBOARD_BT) {
//            return true;
//        }
//        return false;
//    }
//
//    public static boolean isUsbKeyboardConnected(Context context) {
//        if (isAccessoryKeyboardState((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)) == KEYBOARD_USB) {
//            return true;
//        }
//        return false;
//    }

//    public static int isAccessoryKeyboardState(InputMethodManager imm) {
//        int result = 0;
//        Method isAccessoryKeyboardState = null;
//        try {
//            isAccessoryKeyboardState = imm.getClass().getMethod("isAccessoryKeyboardState");
//            if (isAccessoryKeyboardState != null) {
//                result = (Integer) isAccessoryKeyboardState.invoke(imm);
//            }
//        } catch (NoSuchMethodException e) {
//            Log.e(TAG, "Fail to invoke isAccessoryKeyboardState");
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "Fail to invoke isAccessoryKeyboardState");
//        } catch (IllegalAccessException e) {
//            Log.e(TAG, "Fail to invoke isAccessoryKeyboardState");
//        } catch (InvocationTargetException e) {
//            Log.e(TAG, "Fail to invoke isAccessoryKeyboardState");
//        }
//        Log.d(TAG, "isAccessoryKeyboardState " + result);
//        return result;
//    }
//
//    public static void saveBitmapToFileCache(Bitmap bitmap, String path) {
//        if (bitmap == null || bitmap.isRecycled()) {
//            return;
//        }
//
//        File fileCacheItem = new File(path);
//        OutputStream out = null;
//
//        try {
//            if (fileCacheItem.createNewFile()) {
//                out = new FileOutputStream(fileCacheItem);
//            }
//            if (out != null) {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
//            }
//        } catch (Throwable e) {
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//            }
//        }
//        bitmap.recycle();
//    }

    public static void saveBitmapToFileCache(Bitmap bitmap, String path, Bitmap.CompressFormat format, int quality) {
        saveBitmapToFileCache(bitmap, path, format, quality, true);
    }

    public static void saveBitmapToFileCache(Bitmap bitmap, String path, Bitmap.CompressFormat format, int quality, boolean recycle) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        File fileCacheItem = new File(path);
        OutputStream out = null;

        try {
            //Log.d(TAG, "saveBitmapToFileCache, start path: " + path + ", format: " + format + ", quality: " + quality);
            if (fileCacheItem.createNewFile()) {
                out = new FileOutputStream(fileCacheItem);
            }
            if (out != null) {
                Bitmap bg = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bg);
                canvas.drawColor(0xfffafafa);//MemoApplication.getAppContext().getResources().getColor(R.color.composer_main_background));
                canvas.drawBitmap(bitmap, 0, 0, null);
                if (recycle) {
                    // bitmap.recycle();
                }
                bg.compress(format, quality, out);
                bg.recycle();
                //Log.d(TAG, "saveBitmapToFileCache, done path: " + path);
            }
        } catch (Throwable e) {
            Log.e(TAG, "saveBitmapToFileCache", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
    }

//    public static void copyFile(String src, String dst) throws IOException {
//
//        File dstName = new File(dst);
//        //Log.d(TAG, "copyFile, src: " + src + ", dstName: " + dstName.toString());
//        try {
//            FileInputStream inStream = new FileInputStream(src);
//            FileOutputStream outStream = new FileOutputStream(dstName);
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = inStream.read(buf)) > 0) {
//                outStream.write(buf, 0, len);
//            }
//            inStream.close();
//            outStream.close();
//        } catch (Exception e) {
//            Log.e(TAG, "copyFile", e);
//        }
//    }

//    public static String copyFileIfBindFile(String path, String copyPath) {
//
//        if (!TextUtils.isEmpty(path)) {
//            File target = new File(path);
//            if (target.exists()) {
//                String name = target.getName();
//                if (!TextUtils.isEmpty(name) && name.contains("@")) {
//                    try {
//                        Util.copyFile(path, copyPath);
//                        if (new File(copyPath).exists()) {
//                            path = copyPath;
//                        }
//                    } catch (IOException e) {
//                        Log.e(TAG, "copyFileIfBindFile, exception on copy file.", e);
//                    }
//                }
//            }
//        }
//        return path;
//    }
//
//    public static void saveResourceFile(Resources resource, int resourceId, String outFile) {
//        try {
//            FileOutputStream outStream = new FileOutputStream(outFile);
//            InputStream inStream = resource.openRawResource(resourceId);
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = inStream.read(buffer)) != -1) {
//                outStream.write(buffer, 0, len);
//            }
//            inStream.close();
//            outStream.close();
//        } catch (Exception e) {
//            Log.d(TAG, "" + e.getMessage());
//        }
//    }
//
//    public static String getFormattedDate(long time) {
//        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMdd");
//        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
//        return dateFormat.format(time);
//    }
//
//    public static String getSdocInternalCachePathByTime(SDoc sdoc, String prefix, String extension) {
//        return sdoc.getRepositoryPath() + getFileNameByTime(prefix, extension);
//    }
//
    public static String getImagePathByTimeInAppCache(Context context, String prefix, String extension) {
        return context.getCacheDir() + "/" + getFileNameByTime(prefix, extension);
    }

//    public static String getImagePathByTimeInAppCache(Context context, String folder, String prefix, String extension) {
//        return context.getCacheDir() + "/" + folder + "/" + getFileNameByTime(prefix, extension);
//    }

    public static String getFileNameByTime(String prefix, String extension) {
        return prefix + FILE_DATAFORMAT.format(new Date()) + extension;
    }

//    public static String createFolderPathByTime(Context context, String prefix) {
//        return context.getCacheDir() + "/" + prefix + FOLDER_DATAFORMAT.format(new Date());
//    }
//
//    public static String createImagePathByTime(String folderPath, String extension) {
//        return folderPath + "/" + getFileNameByTime("", extension);
//    }
//
//    public static void removeUnderDir(File dir) {
//        //Log.d(TAG, "removeUnderDir, path: " + dir.getAbsolutePath());
//        if (dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                new File(dir, children[i]).delete();
//            }
//        }
//    }

    public static Bitmap downloadImage(String url) {
        Log.d(TAG, "downloadImage, url: " + url);
        Bitmap bmp = null;
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp != null) {
                Log.d(TAG, "downloadImage, done");
                return bmp;
            }
        } catch (Exception e) {
            Log.e(TAG, "downloadImage, ", e);
        }
        return bmp;
    }

//    public static int getThemeAttributeDimensionSize(Context context, int attr) {
//        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
//        final int size = a.getDimensionPixelSize(0, 0);
//        a.recycle();
//        return size;
//    }
//
//    public static float getPixelFromDip(Context context, float dp) {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
//                .getDisplayMetrics());
//    }
//
//    public static String concat(String folderPath, String fileName) {
//        String path = folderPath;
//        if (path.endsWith("/"))
//            path += fileName;
//        else
//            path += "/" + fileName;
//        return path;
//    }

//    public static long getAvailableInternalMemorySize() {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSizeLong();
//        long availableBlocks = stat.getAvailableBlocksLong();
//        return availableBlocks * blockSize;
//    }

//    public static boolean isAvailableMemoryForNewMemo() {
//        return (getAvailableInternalMemorySize() > MINIMUM_AVAILABLE_MEMORY_SIZE_FOR_NEW_MEMO);
//    }


//    public static void notEnoughStorageDialog(Context context) {
//        final AlertDialogBuilderForAppCompat addDialog = new AlertDialogBuilderForAppCompat(context);
//        addDialog.setTitle(context.getResources().getString(com.samsung.android.app.notes.R.string.not_enough_storage))
//                .setMessage(context.getResources().getString(com.samsung.android.app.notes.R.string.voice_not_enough_memory_delete_some_items))
//                .setCancelable(true)
//                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                }).create();
//        addDialog.show();
//    }

    public static class Image {

//        public static BitmapDrawable resizeDrawable(BitmapDrawable source, int maxResolution) {
//            if (source == null) {
//                return source;
//            }
//            Bitmap b = source.getBitmap();
//            Bitmap resizedBitmap = resizeBitmapImage(b, maxResolution);
//            if (b == resizedBitmap) {
//                return source;
//            }
//            source = new BitmapDrawable(MemoApplication.getAppContext().getResources(), resizedBitmap);
//            return source;
//        }

        public static Bitmap resizeBitmapImage(Bitmap source, float maxWidth) {
            int maxHeight = Util.getMaxTextureSize();
            Log.d(TAG, "resizeBitmapImage, maxWidth: " + maxWidth + ", maxHeight: " + maxHeight);

            int width = source.getWidth();
            int height = source.getHeight();
            int newWidth = width;
            int newHeight = height;

            if (maxWidth < width) {
                float rate = maxWidth / (float) width;
                newHeight = (int) (height * rate);
                newWidth = (int) (width * rate);
                Log.d(TAG, "resizeBitmapImage, width newWidth: " + newWidth + ", newHeight: " + newHeight);
            }

            if (maxHeight < newHeight) {
                float rate = maxHeight / (float) height;
                newWidth = (int) (newWidth * rate);
                newHeight = (int) (newHeight * rate);
                Log.d(TAG, "resizeBitmapImage, height newWidth: " + newWidth + ", newHeight: " + newHeight);
            }

            if ((width == newWidth) && (height == newHeight)) {
                return source;
            }

            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        }

//        public static Bitmap resizeBitmapImage(Bitmap source, float maxWidth, int degrees) {
//            int maxHeight = Util.getMaxTextureSize();
//            Log.d(TAG, "resizeBitmapImage, maxWidth: " + maxWidth + ", maxHeight: " + maxHeight);
//
//            int width = 0;
//            int height = 0;
//            int newWidth = 0;
//            int newHeight = 0;
//
//            width = source.getWidth();
//            height = source.getHeight();
//
//            if (degrees != 0) {
//                width = source.getHeight();
//                height = source.getWidth();
//            }
//
//            newWidth = width;
//            newHeight = height;
//
//
//            if (maxWidth < width) {
//                float rate = maxWidth / (float) width;
//                newHeight = (int) (height * rate);
//                newWidth = (int) (width * rate);
//                Log.d(TAG, "resizeBitmapImage, width newWidth: " + newWidth + ", newHeight: " + newHeight);
//            }
//
//            if (maxHeight < newHeight) {
//                float rate = maxHeight / (float) height;
//                newWidth = (int) (newWidth * rate);
//                newHeight = (int) (newHeight * rate);
//                Log.d(TAG, "resizeBitmapImage, height newWidth: " + newWidth + ", newHeight: " + newHeight);
//            }
//
//            if ((width == newWidth) && (height == newHeight)) {
//                return source;
//            }
//
//            if (degrees != 0) {
//                return Bitmap.createScaledBitmap(source, newHeight, newWidth, true);
//            } else {
//                return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
//            }
//
//        }
//
//        public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
//
//            Matrix matrix = new Matrix();
//            if (degrees != 0) {
//                matrix.preRotate(degrees);
//            } else {
//                return bitmap;
//            }
//
//            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//            if (bmRotated != bitmap && !bitmap.isRecycled()) {
//                bitmap.recycle();
//            }
//            return bmRotated;
//        }
//
//        public static int getImageRotation(Context context, Uri imageUri) {
//            try {
//                ExifInterface exif = new ExifInterface(imageUri.getPath());
//                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//
//                if (rotation == ExifInterface.ORIENTATION_UNDEFINED) {
//                    return getRotationFromMediaStore(context, imageUri);
//                } else {
//                    return exifToDegrees(rotation);
//                }
//            } catch (IOException e) {
//                return 0;
//            }
//        }

        public static int getRotationFromMediaStore(Context context, Uri imageUri) {
            try {
                String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
                Cursor cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
                if (cursor == null) {
                    Log.d(TAG, "getRotationFromMediaStore, cursor is null");
                    return 0;
                }

                cursor.moveToFirst();
                int orientationColumnIndex = cursor.getColumnIndex(columns[1]);
                int ret = cursor.getInt(orientationColumnIndex);
                Log.d(TAG, "getRotationFromMediaStore, ret: " + ret);
                cursor.close();
                return ret;
            } catch (Exception e) {
                Log.e(TAG, "getRotationFromMediaStore, e: " + e.getMessage());
            }
            return 0;
        }

        private static int exifToDegrees(int exifOrientation) {
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
            return 0;
        }

//        public static int calculateInSampleSize(
//                BitmapFactory.Options options, int reqWidth, int reqHeight) {
//            // Raw height and width of image
//            final int height = options.outHeight;
//            final int width = options.outWidth;
//            Log.d(TAG, "calculateInSampleSize, reqWidth: " + reqWidth + ", reqHeight: " + reqHeight);
//            Log.d(TAG, "calculateInSampleSize, width: " + width + ", height: " + height);
//            int inSampleSize = 1;
//
//            if (height > reqHeight || width > reqWidth) {
//
//                final int halfHeight = height / 2;
//                final int halfWidth = width / 2;
//
//                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//                // height and width larger than the requested height and width.
//                while ((halfHeight / inSampleSize) > reqHeight
//                        && (halfWidth / inSampleSize) > reqWidth) {
//                    inSampleSize *= 2;
//                }
//
//                int prevHeight = height / Math.max(1, inSampleSize / 2);
//                int prevWidth = width / Math.max(1, inSampleSize / 2);
//                if (inSampleSize > 1 &&
//                        prevHeight < Util.getMaxTextureSize() &&
//                        prevWidth < Util.getMaxTextureSize()) {
//                    inSampleSize /= 2;
//                }
//            }
//            Log.d(TAG, "calculateInSampleSize, inSampleSize: " + inSampleSize);
//            return inSampleSize;
//        }

//        //@Nullable
//        public static Bitmap getBitmapFromUri(Context context, Uri uri) throws Exception {
//
//            Bitmap bitmap = null;
//            if (uri == null) {
//                Log.d(TAG, "getBitmapFromUri, uri is null.");
//                return null;
//            }
//
//            // Log.d(TAG, "getBitmapFromUri, uri: " + uri);
//            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                InputStream is = context.getContentResolver().openInputStream(uri);
//                if (is != null) {
//                    BitmapFactory.decodeStream(is, null, options);
//                    is.close();
//                } else {
//                    Log.d(TAG, "getBitmapFromUri, get bounds, can not open input stream.");
//                    return null;
//                }
//                Log.d(TAG, "getBitmapFromUri options, width: " + options.outWidth + ", height: " + options.outHeight);
//                int width = (options.outWidth != 0) ? options.outWidth : 1;
//                float imageRatio = options.outHeight / (float) width;
//                Rect screen = ScreenDimension.getScreenDimension(context);
//                int reqWidth = screen.width();
//                options.inSampleSize = calculateInSampleSize(options, reqWidth, (int) (reqWidth * imageRatio));
//                options.inJustDecodeBounds = false;
//                is = context.getContentResolver().openInputStream(uri);
//                if (is != null) {
//                    bitmap = BitmapFactory.decodeStream(is, null, options);
//                    is.close();
//                } else {
//                    Log.d(TAG, "getBitmapFromUri, get bitmap, can not open input stream.");
//                    return null;
//                }
//            } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                String path = uri.getPath();
//                // Log.d(TAG, "getBitmapFromUri, path: " + path);
//                File file = new File(path);
//                boolean exists = file.exists();
//                Log.d(TAG, "getBitmapFromUri, uri get path is exists: " + exists);
//                if (!exists) {
//                    path = uri.toString();
//                    path = path.replace("file://", "");
//                    Log.d(TAG, "getBitmapFromUri, retry with undecoded uri");
//                    file = new File(path);
//                    exists = file.exists();
//                    Log.d(TAG, "getBitmapFromUri, uri get string is exists: " + exists);
//                    if (!exists) {
//                        return null;
//                    }
//                }
//                final BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(path, options);
//                Log.d(TAG, "getBitmapFromUri options, width: " + options.outWidth + ", height: " + options.outHeight);
//                int width = (options.outWidth != 0) ? options.outWidth : 1;
//                float imageRatio = options.outHeight / (float) width;
//                Rect screen = ScreenDimension.getScreenDimension(context);
//                int reqWidth = screen.width();
//                options.inSampleSize = calculateInSampleSize(options, reqWidth, (int) (reqWidth * imageRatio));
//                options.inJustDecodeBounds = false;
//                bitmap = BitmapFactory.decodeFile(path, options);
//            } else if (uri.getScheme().startsWith("http")) {
//                bitmap = getBitmapFromHttpUri(uri.toString(), 5000);
//            } else if (uri.toString().startsWith("data:image")) {
//                String uriString = uri.toString();
//                String dataBytes = uriString.substring(uriString.indexOf(",") + 1);
//                byte[] decode = Base64.decode(dataBytes, Base64.DEFAULT);
//                bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
//            } else {
//                throw new RuntimeException("no handle to get bitmap scheme : " + uri.getScheme());
//            }
//            if (bitmap == null) {
//                Log.d(TAG, "getBitmapFromUri, bitmap is null");
//            }
//            return bitmap;
//        }

//        public static boolean isValidUri(Context context, Uri uri) {
//
//            //Log.d(TAG, "isValidUri, uri: " + uri);
//            boolean ret = false;
//            try {
//                if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//                    InputStream is = context.getContentResolver().openInputStream(uri);
//                    if (is != null) {
//                        is.close();
//                        ret = true;
//                    }
//                } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                    String path = uri.getPath();
//                    //Log.d(TAG, "getBitmapFromUri, path: " + path);
//                    if (!TextUtils.isEmpty(path)) {
//                        File file = new File(path);
//                        if (file.exists() && file.canRead()) {
//                            ret = true;
//                        }
//                    }
//                } else if (uri.getScheme().startsWith("http")) {
//                    ret = true;
//                } else if (uri.toString().startsWith("data:image")) {
//                    ret = true;
//                } else {
//                    throw new RuntimeException("no handle scheme : " + uri.getScheme());
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "isValidUri", e);
//                ret = false;
//            }
//
//            Log.d(TAG, "isValidUri, ret: " + ret);
//            return ret;
//        }
//
//        ////@Nullable
//        private static Bitmap getBitmapFromHttpUri(final String uri, final int timeout) throws Exception {
//            //Log.d(TAG, "getBitmapFromHttpUri, uri: " + uri + ", timeout: " + timeout);
//            ExecutorService service = Executors.newSingleThreadExecutor();
//            Future<Bitmap> result = service.submit(new Callable<Bitmap>() {
//                @Override
//                public Bitmap call() throws Exception {
//                    try {
//                        return getBitmapFromHttpUriTimeout(uri, timeout);
//                    } catch (Throwable th) {
//                        Log.e(TAG, "getBitmapFromHttpUri, " + th);
//                    }
//                    return null;
//                }
//            });
//            Bitmap bitmap = result.get(timeout, TimeUnit.MILLISECONDS);
//            service.shutdown();
//            Log.d(TAG, "getBitmapFromHttpUri, bitmap byte: " + ((bitmap == null) ? "null" : bitmap.getByteCount()));
//            return bitmap;
//        }
//
//        ////@Nullable
//        private static Bitmap getBitmapFromHttpUri(final String uri) {
//            //Log.d(TAG, "getBitmapFromHttpUri, uri: " + uri);
//            try {
//                InputStream in = new java.net.URL(uri).openStream();
//                Bitmap bitmap = BitmapFactory.decodeStream(in);
//                in.close();
//                return bitmap;
//            } catch (IOException ex) {
//                Log.e(TAG, "getBitmapFromHttpUri", ex);
//            }
//            return null;
//        }

        ////@Nullable
        private static Bitmap getBitmapFromHttpUriTimeout(final String uri, int timeout) {
            //Log.d(TAG, "getBitmapFromHttpUriTimeout, uri: " + uri);
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(uri).openConnection();
                con.setConnectTimeout(timeout);
                con.setReadTimeout(timeout);
                InputStream in = con.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                return bitmap;
            } catch (IOException ex) {
                Log.e(TAG, "getBitmapFromHttpUriTimeout", ex);
            }
            return null;
        }

        //@Nullable
//        public static Rect getBitmapSize(String path) {
//            try {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(path, options);
//                return new Rect(0, 0, options.outWidth, options.outHeight);
//            } catch (Exception e) {
//                Log.e(TAG, "getBitmapSize", e);
//                return null;
//            }
//        }
//
//        //@Nullable
//        public static Rect getBitmapSizeByUri(Context context, Uri uri) throws Exception {
//
//            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
//                Log.d(TAG, "getBitmapSizeByUri options, width: " + options.outWidth + ", height: " + options.outHeight);
//                return new Rect(0, 0, options.outWidth, options.outHeight);
//            } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                String path = uri.getPath();
//                final BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(path, options);
//                Log.d(TAG, "getBitmapSizeByUri options, width: " + options.outWidth + ", height: " + options.outHeight);
//                return new Rect(0, 0, options.outWidth, options.outHeight);
//            } else if (uri.getScheme().startsWith("http")) {
//                return null;
//            } else {
//                Log.d(TAG, "no handle to get size scheme : " + uri.getScheme());
//                // throw new RuntimeException("no handle to get size scheme : " + uri.getScheme());
//            }
//            return null;
//        }
//
//        public static boolean isImageFile(String fileName) {
//            final String string = ".*\\.(png|jpg|bmp|gif|jpeg|wbmp|PNG|JPG|GIF|BMP|JPEG|WBMP)";
//            if (fileName != null && fileName.matches(string)) {
//                return true;
//            }
//
//            return false;
//        }
    }

    public static boolean isNetworkOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean ret = ((netInfo != null) && netInfo.isConnectedOrConnecting());
        Log.d(TAG, "isOnline, ret: " + ret);
        return ret;
    }
//
//    public static void setBackgroundDefaultRippleOutsideView(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {
//            view.setBackgroundResource(R.drawable.default_ripple_outside_view);
//        }
//    }

//    public static Drawable setRippleSelected(Context context) {
//        int pressed_id = R.drawable.editor_richtext_toolbar_pressed;
//        int selected_id = R.drawable.editor_richtext_toolbar_selected;
//        int pressed_state_id = R.drawable.editor_richtext_toolbar_state_pressed;
//        StateListDrawable d = new StateListDrawable();
//        d.addState(new int[]{android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), selected_id, context.getTheme()));
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            return ResourcesCompat.getDrawable(context.getResources(), pressed_state_id, context.getTheme());
//        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            d.addState(new int[]{android.R.attr.state_enabled}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//            d.addState(new int[]{android.R.attr.state_pressed}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//        }
//        return d;
//    }
//
//    public static Drawable setOvalRippleSelected(Context context) {
//        int pressed_id = R.drawable.common_oval_ripple_pressed;
//        int selected_id = R.drawable.editor_richtext_toolbar_selected;
//
//        StateListDrawable d = new StateListDrawable();
//        d.addState(new int[]{android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), selected_id, context.getTheme()));
//
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            return ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme());
//        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            d.addState(new int[]{android.R.attr.state_enabled}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//            d.addState(new int[]{android.R.attr.state_pressed}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//        }
//        return d;
//    }
//
//    public static Drawable setRoundButtonRippleSelected(Context context) {
//        int pressed_id = R.drawable.editor_round_richtext_toolbar_pressed;
//        int selected_id = R.drawable.editor_round_richtext_toolbar_selected;
//        int pressed_state_id = R.drawable.editor_richtext_toolbar_state_pressed;
//        StateListDrawable d = new StateListDrawable();
//        d.addState(new int[]{android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), selected_id, context.getTheme()));
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            return ResourcesCompat.getDrawable(context.getResources(), pressed_state_id, context.getTheme());
//        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            d.addState(new int[]{android.R.attr.state_enabled}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//            d.addState(new int[]{android.R.attr.state_pressed}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//        }
//        return d;
//    }
//
//    public static void setRippleToolbar(Toolbar toolbar) {
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            for (int i = 0, n = toolbar.getChildCount(); i < n; i++) {
//                if (toolbar.getChildAt(i) instanceof android.widget.ImageButton) {
//                    toolbar.getChildAt(i).setBackgroundResource(R.drawable.default_ripple_outside_view);
//                    break;
//                }
//            }
//        }
//    }
//
//    public static Drawable setRippleFocusSelected(Context context) {
//        int pressed_id = R.drawable.editor_richtext_toolbar_pressed;
//        int selected_id = R.drawable.editor_richtext_toolbar_selected;
//        int focused_id = R.drawable.memolist_focused_selected_item_selector;
//        int pressed_state_id = R.drawable.editor_richtext_toolbar_state_pressed;
//        StateListDrawable d = new StateListDrawable();
//        d.addState(new int[]{android.R.attr.state_focused}, ResourcesCompat.getDrawable(context.getResources(), focused_id, context.getTheme()));
//        d.addState(new int[]{android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), selected_id, context.getTheme()));
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            d.addState(new int[]{android.R.attr.state_enabled}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//            d.addState(new int[]{android.R.attr.state_pressed}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            return ResourcesCompat.getDrawable(context.getResources(), pressed_state_id, context.getTheme());
//        }
//        return d;
//    }
//
//    public static Drawable setRippleFocusSelectedAccessibility(Context context) {
//        int pressed_id = R.drawable.editor_richtext_toolbar_pressed_btn_shape;
//        int selected_id = R.drawable.editor_richtext_toolbar_selected_btn_shape;
//        int focused_id = R.drawable.memolist_focused_item_selector_btn_shape;
//        StateListDrawable d = new StateListDrawable();
//        d.addState(new int[]{android.R.attr.state_focused}, ResourcesCompat.getDrawable(context.getResources(), focused_id, context.getTheme()));
//        d.addState(new int[]{android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), selected_id, context.getTheme()));
//        d.addState(new int[]{-android.R.attr.state_focused, -android.R.attr.state_selected}, ResourcesCompat.getDrawable(context.getResources(), R.drawable.composer_show_btn_ripple_in_icon_text, context.getTheme()));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            d.addState(new int[]{android.R.attr.state_enabled}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//            d.addState(new int[]{android.R.attr.state_pressed}, ResourcesCompat.getDrawable(context.getResources(), pressed_id, context.getTheme()));
//        }
//        return d;
//    }

//    public static float convertDpToPixel(Context context, float dp) {
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
//        return px;
//    }
//
//    public static float convertPixelsToDp(Context context, float px) {
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
//        return dp;
//    }
//
//    public static float convertSpToPixel(Context context, float sp) {
//        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
//        return sp * scaledDensity;
//    }
//
//    public static float convertPixelToSp(Context context, float px) {
//        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
//        return px / scaledDensity;
//    }

//    private static int getScrollDeltaY(View view, int viewHeight) {
//        int pos[] = new int[2];
//        view.getLocationOnScreen(pos);
//        float viewY = pos[1];
//        float screenHeight = ScreenDimension.getScreenDimension(view.getContext()).height();
//        Log.d(TAG, "getScrollDeltaY, viewY : " + viewY + ", viewHeight : " + viewHeight + ", screenHeight : " + screenHeight);
//        int delta = (int) (viewY + viewHeight - screenHeight);
//        return delta <= 0 ? 0 : delta + 100;
//    }

//    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
//        if (drawable == null) {
//            return null;
//        }
////        if (drawable instanceof BitmapDrawable) {
////            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
////            if(bitmapDrawable.getBitmap() != null) {
////                return bitmapDrawable.getBitmap();
////            }
////        }
//        int width = drawable.getIntrinsicWidth();
//        width = width > 0 ? width : 1;
//        int height = drawable.getIntrinsicHeight();
//        height = height > 0 ? height : 1;
//
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, width, height);
//        drawable.draw(canvas);
//
//        return bitmap;
//    }
//
//    public static byte[] marshall(Parcelable parceable) {
//        Parcel parcel = Parcel.obtain();
//        parceable.writeToParcel(parcel, 0);
//        byte[] bytes = parcel.marshall();
//        parcel.recycle();
//        return bytes;
//    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

//    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
//        Parcel parcel = unmarshall(bytes);
//        return creator.createFromParcel(parcel);
//    }
//
//
//    public static void deleteFolder(String folder) {
//        File dir = new File(folder);
//        File[] files = dir.listFiles();
//        if (files == null) {
//            return;
//        }
//        for (File file : files) {
//            if (file.isDirectory()) {
//                deleteFolder(file.getAbsolutePath());
//            } else {
//                file.delete();
//            }
//        }
//        dir.delete();
//    }

    private static int MAX_TEXTURE_SIZE = -1;

    public static int getMaxTextureSize() {
        if (MAX_TEXTURE_SIZE > 0) {
            return MAX_TEXTURE_SIZE;
        }

        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;
        final int IMAGE_MAXIMUM_BITMAP_DIMENSION = 8192;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        MAX_TEXTURE_SIZE = Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);

        if (MAX_TEXTURE_SIZE > IMAGE_MAXIMUM_BITMAP_DIMENSION) {
            MAX_TEXTURE_SIZE = IMAGE_MAXIMUM_BITMAP_DIMENSION;
        }
        return MAX_TEXTURE_SIZE;
    }

//    public static boolean writeByteArrayToFile(String path, byte[] bytes) {
//        try {
//            FileOutputStream fos = new FileOutputStream(path);
//            fos.write(bytes);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "" + e.getMessage());
//            return false;
//        } catch (IOException e) {
//            Log.d(TAG, "" + e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    public static byte[] readFileToByteArray(String path) {
//        File file = new File(path);
//        long size = file.length();
//
//        if (size <= 0) {
//            return null;
//        }
//
//        byte[] bytes = new byte[(int) size];
//
//
//        try {
//            InputStream is = new FileInputStream(file);
//            int offset = 0;
//            int numRead = 0;
//            while (offset < bytes.length
//                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
//                offset += numRead;
//            }
//            if (offset < bytes.length) {
//                throw new IOException("Could not completely read file " + file.getName());
//            }
//            is.close();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "" + e.getMessage());
//            return null;
//        } catch (IOException e) {
//            Log.d(TAG, "" + e.getMessage());
//            return null;
//        }
//        return bytes;
//    }
//
//    // get color & drawable by version
//    @SuppressLint("NewApi")
//    public static int getColorByVersion(Context context, int resId) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return context.getColor(resId);
//        } else {
//            return context.getResources().getColor(resId);
//        }
//    }
//
//    @SuppressLint("NewApi")
//    public static Drawable getDrawableByVersion(Context context, int resId) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            return context.getDrawable(resId);
//        } else {
//            return context.getResources().getDrawable(resId);
//        }
//    }

    // about broadcasting to AOD
    private static String SOM_TAG = "ScreenOffMemo_Util";
    public static final String ACTION_AOD_REMOTEVIEWS_UPDATE = "com.samsung.android.app.aodservice.REMOTEVIEWS_UPDATE";
    public static final String KEY_TARGET = "target";
    public static final String KEY_TARGET_MEMO = "memo";
//    public static final int TYPE_MEMO_UNPIN = 0;
//    public static final int TYPE_MEMO_PIN = 1;

//    public static void sendBroadcastToAOD(Context context, int type) {
//        Log.d(SOM_TAG, "sendBroadcastToAOD() : " + context + ", " + type);
//
//        Intent intent = new Intent(ACTION_AOD_REMOTEVIEWS_UPDATE);
//        intent.putExtra(KEY_TARGET, KEY_TARGET_MEMO);
//        intent.putExtra("type", type);    // 0 : memo unpin, 1 : memo pin
//        String uriStr = getPinImagePath(context);
//
//        intent.putExtra("uri", uriStr);
//        context.sendBroadcast(intent);
//    }

//    public static int getPinType(Context context) {
//        SharedPreferences pref = context.getSharedPreferences(ScreenOffMemoService.SCREENOFFMEMO_PREF, context.MODE_PRIVATE);
//        int pinType = pref.getInt("PinType", 0);
//        return pinType;
//    }

    private static final String SAVED_IMAGE_FILE_EXTENSION = ".jpg";
    private static final String SAVED_IMAGE_FOLDER_NAME = ".pin";
    private static final String SAVED_IMAGE_FILE_NAME = "image";

//    public static String getPinImagePath(Context context) {
//        File extFile = null;
//        try {
//            extFile = new File(context.getExternalFilesDir(null).getAbsoluteFile() + File.separator + SAVED_IMAGE_FOLDER_NAME);
//            if (!extFile.exists() && !extFile.mkdir()) {
//                Log.d(TAG, "getPinImagePath - mkdir fail - return null");
//                return null;
//            }
//        } catch (Exception e) {
//
//        }
//
//        return extFile.getAbsolutePath() + File.separator + SAVED_IMAGE_FILE_NAME + SAVED_IMAGE_FILE_EXTENSION;
//    }
//
//    public static void clearPinDir(Context context, File dir) {
//        if (dir == null)
//            dir = new File(context.getExternalFilesDir(null).getAbsoluteFile() + File.separator + SAVED_IMAGE_FOLDER_NAME);
//
//        if (dir == null)
//            return;
//
//        File[] children = dir.listFiles();
//        try {
//            for (int i = 0; i < children.length; i++)
//                if (children[i].isDirectory())
//                    clearPinDir(context, children[i]);
//                else if (children[i].delete() == false) {
//                    Log.e(TAG, "clearApplicationCache() - fail to delete file : " + children[i]);
//                    continue;
//                }
//        } catch (Exception e) {
//            Log.d(TAG, "" + e.getMessage());
//        }
//    }
//
//    public static Uri getMediaUri(Context context, Uri mediaUri, String path) {
//        Uri uri;
//        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
//        File f = new File(path);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        context.sendBroadcast(mediaScanIntent);
//
//        Cursor mCursor = context.getContentResolver().query(mediaUri, null,
//                "_data" + "=? ",
//                new String[]{path}, null);
//
//        if (mCursor != null && mCursor.moveToFirst()) {
//            int id = mCursor.getInt(mCursor.getColumnIndex("_id"));
//            uri = Uri.withAppendedPath(mediaUri, "" + id);
//        } else {
//            ContentValues values = new ContentValues();
//            values.put("_data", path);
//            uri = context.getContentResolver().insert(
//                    mediaUri, values);
//        }
//        if (mCursor != null && !mCursor.isClosed()) {
//            mCursor.close();
//        }
//
//        return uri;
//    }

//    public static void stopRecyclerViewAnimationOnce(final RecyclerView view, Runnable runnable) {
//        final RecyclerView.ItemAnimator itemAnimator = view.getItemAnimator();
//        try {
//            view.setItemAnimator(null);
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "stopRecyclerViewAnimationOnce" + e);
//        }
//
//        runnable.run();
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                view.setItemAnimator(itemAnimator);
//            }
//        });
//    }

    public static boolean isLockTaskMode(Context context) {
        boolean isLockTaskMode = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isLockTaskMode = am.isInLockTaskMode();
        }

        Log.d(TAG, "isLockTaskMode, isLockTaskMode: " + isLockTaskMode);
        return isLockTaskMode;
    }

//    public static boolean isTaskRootAndLockTaskMode(Activity activity) {
//        boolean ret = false;
//        if (isLockTaskMode(activity) && activity.isTaskRoot()) {
//            ret = true;
//        }
//        Log.d(TAG, "isTaskRootAndLockTaskMode, ret: " + ret);
//        return ret;
//    }
//
//    public static CharSequence[] splitCharSequence(CharSequence cs, int splitIndex) {
//        CharSequence[] ret = new CharSequence[2];
//
//        CharSequence left = cs.subSequence(0, splitIndex);
//        Spannable temp = Util.convertSpannableString(left);
//        CharacterStyle styles[] = temp.getSpans(splitIndex, splitIndex, CharacterStyle.class);
//        for (CharacterStyle style : styles) {
//            temp.setSpan(newCharacterStyleInstance(style), temp.getSpanStart(style), temp.getSpanEnd(style), temp.getSpanFlags(style));
//            temp.removeSpan(style);
//        }
//        ret[0] = temp;
//
//        CharSequence right = cs.subSequence(splitIndex, cs.length());
//        temp = Util.convertSpannableString(right);
//        styles = temp.getSpans(splitIndex, splitIndex, CharacterStyle.class);
//        for (CharacterStyle style : styles) {
//            temp.setSpan(newCharacterStyleInstance(style), temp.getSpanStart(style), temp.getSpanEnd(style), temp.getSpanFlags(style));
//            temp.removeSpan(style);
//        }
//        ret[1] = temp;
//        return ret;
//    }
//
//    public static CharSequence subSequence(CharSequence cs, int start, int end) {
//        CharSequence left = cs.subSequence(start, end);
//        Spannable temp = Util.convertSpannableString(left);
//        CharacterStyle styles[] = temp.getSpans(start, end, CharacterStyle.class);
//        for (CharacterStyle style : styles) {
//            temp.setSpan(newCharacterStyleInstance(style), temp.getSpanStart(style), temp.getSpanEnd(style), temp.getSpanFlags(style));
//            temp.removeSpan(style);
//        }
//        return temp;
//    }

//    private static CharacterStyle newCharacterStyleInstance(CharacterStyle style) {
//        CharacterStyle ret = null;
//        if (style instanceof CustomForegroundColorSpan) {
//            ret = new CustomForegroundColorSpan(((CustomForegroundColorSpan) style).getForegroundColor());
//        } else if (style instanceof CustomUnderlineSpan) {
//            ret = new CustomUnderlineSpan();
//        } else if (style instanceof StyleSpan) {
//            ret = new StyleSpan(((StyleSpan) style).getStyle());
//        } else {
//            Log.d(TAG, "style new failed " + style);
//        }
//        return ret;
//    }

//    public static CharSequence replaceToCustomSpans(CharSequence text) {
//        SpannableStringBuilder str = new SpannableStringBuilder(text);
//        ParcelableSpan[] spans = str.getSpans(0, str.length(), ParcelableSpan.class);
//        for (ParcelableSpan span : spans) {
//            if (span instanceof UnderlineSpan) {
//                int start = str.getSpanStart(span);
//                int end = str.getSpanEnd(span);
//                str.removeSpan(span);
//                int flags = (start == 0) ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
//                str.setSpan(new CustomUnderlineSpan(), start, end, flags);
//            } else if (span instanceof ForegroundColorSpan) {
//                int start = str.getSpanStart(span);
//                int end = str.getSpanEnd(span);
//                int color = ((ForegroundColorSpan) span).getForegroundColor();
//                str.removeSpan(span);
//                int flags = (start == 0) ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
//                str.setSpan(new CustomForegroundColorSpan(color), start, end, flags);
//            } else if (span instanceof StyleSpan) {
//                int start = str.getSpanStart(span);
//                int end = str.getSpanEnd(span);
//                int style = ((StyleSpan) span).getStyle();
//                str.removeSpan(span);
//                int flags = (start == 0) ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
//                str.setSpan(new StyleSpan(style), start, end, flags);
//            }
//        }
//        return str;
//    }

//    private static Class<?> EmergencyManagerClass = null;
//    private static Method IsEmergencyMode = null;

//    public static boolean isEmergencyMode(Context context) {
//        try {
//            if (EmergencyManagerClass == null) {
//                EmergencyManagerClass = Class.forName("com.sec.android.emergencymode.EmergencyManager");
//            }
//            if (IsEmergencyMode == null) {
//                IsEmergencyMode = EmergencyManagerClass.getMethod("isEmergencyMode", Context.class);
//            }
//            return (boolean) IsEmergencyMode.invoke(EmergencyManagerClass, context);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//
//    private static Class<?> SystemPropertiesClass = null;
//    private static Method SystemPropertiesGet = null;
//    private static String CountryIsoCode = null;
    private static String ProductName = null;

//    private static String getSystemProperties(String name) {
//        try {
//            if (SystemPropertiesClass == null) {
//                SystemPropertiesClass = Class.forName("android.os.SystemProperties");
//            }
//            if (SystemPropertiesGet == null) {
//                SystemPropertiesGet = SystemPropertiesClass.getMethod("get", String.class);
//            }
//            return (String) SystemPropertiesGet.invoke(SystemPropertiesClass, name);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

//    public static String getCountryIsoCode() {
//        if (CountryIsoCode != null) {
//            return CountryIsoCode;
//        }
//        CountryIsoCode = getSystemProperties("ro.csc.countryiso_code");
//        return CountryIsoCode;
//    }

//    public static boolean isChinaModel() {
//        String countryiso_code = getCountryIsoCode();
//
//        if ("CN".equalsIgnoreCase(countryiso_code)) {
//            return true;
//        }
//
//        return false;
//    }

//    public static boolean isUSAModel() {
//        String countryiso_code = getCountryIsoCode();
//
//        if ("US".equalsIgnoreCase(countryiso_code)) {
//            return true;
//        }
//
//        return false;
//    }

    /*
    public static String getBrandName() {
        if (isJapanModel()) {
            return "Galaxy";
        } else {
            return "Samsung";
        }
    }
    */

//    public static String getProductName() {
//        if (ProductName != null) {
//            return ProductName;
//        }
//        ProductName = Build.DEVICE;
//        return ProductName;
//    }

//    public static boolean isNotesHelpModel(@NonNull Context context) {
//        try {
//            if ((context.getPackageManager().getPackageInfo(ON_DEVICE_HELP_PACKAGE_HLEPHUB, 0) != null &&
//                    (context.getPackageManager().getPackageInfo(ON_DEVICE_HELP_PACKAGE_HLEPHUB, 0).versionCode % 10) == 2)) {
//                String name = getProductName();
//                if (name != null) {
//                    name = name.toLowerCase();
//                    if (name.startsWith("grace") && isUSAModel()) {
//                        if (Util.isUPSM(context) || Util.isKnoxMode()) {
//                            return false;
//                        }
//                        return true;
//                    }
//                }
//            }
//            return false;
//        } catch (Exception e) {
//            return false;
//        }
//    }

//    public static boolean isUPSM(@NonNull Context context) {
//        int upsm = 0;
//        try {
//            upsm = Settings.System.getInt(context.getContentResolver(), SETTINGS_SYSTEM_ULTRA_POWERSAVING_MODE, 0);
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "IllegalArgumentException UPSM");
//        }
//        return (upsm == 1);
//    }

//    private static int MOBILEKEYBOARD_COVERED_YES = 1;

//    public static boolean isEnabledMobileKeyboard(Context context) {
//        android.content.res.Configuration conf = context.getResources().getConfiguration();
//        try {
//            Field getConfiguration = android.content.res.Configuration.class.getField("mobileKeyboardCovered");
//            if (getConfiguration != null && getConfiguration.getInt(conf) == MOBILEKEYBOARD_COVERED_YES) {
//                Log.d(TAG, "isEnabledMobileKeyboard return true");
//                return true;
//            }
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            Log.d(TAG, "isEnabledMobileKeyboard does not support mobileKeyboardCoverd field");
//        }
//        Log.d(TAG, "isEnabledMobileKeyboard return false");
//        return false;
//    }

//    public static int getUserId(int _default) {
//        try {
//            Method method = UserHandle.class.getDeclaredMethod("getCallingUserId");
//            int returnValue = ((Integer) method.invoke(null)).intValue();
//            return returnValue;
//        } catch (Exception e) {
//            Log.e(TAG, "getUserId", e);
//        }
//        return _default;
//    }
//
//    public static int getMyUserId() {
//        int returnValue = 0;
//
//        try {
//            Method method = UserHandle.class.getDeclaredMethod("myUserId");
//            returnValue = ((Integer) method.invoke(null)).intValue();
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//        return returnValue;
//    }


    // Actually I have no idea about the difference between isKnoxMode() and isRunningUnderKnox().
    // Just copied from previous Memo application. Sorry!! -_-;;
    //
//    public static boolean isKnoxMode() {
//        boolean knoxMode = false;
//
//        try {
//            Method method = UserHandle.class.getDeclaredMethod("getCallingUserId");
//            int returnValue = ((Integer) method.invoke(null)).intValue();
//            if (returnValue >= 100) {
//                knoxMode = true;
//            }
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//        return knoxMode;
//    }
//
//    public static boolean isSecureFolderMode() {
//        boolean secureFolderMode = false;
//        try {
//            Method method = UserHandle.class.getDeclaredMethod("getCallingUserId");
//            int returnValue = ((Integer) method.invoke(null)).intValue();
//            if (returnValue >= 150 && returnValue <= 160) {
//                secureFolderMode = true;
//            }
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//        return secureFolderMode;
//    }
//
//    public static boolean isRunningUnderKnox(@NonNull Context context) {
//        String packageName = context.getPackageName();
//        if (packageName.startsWith("sec_container_")) {
//            return true;
//        }
//        return false;
//    }

//    public static Spannable convertSpannableString(CharSequence string) {
//        if (string instanceof Spannable) {
//            return (Spannable) string;
//        } else {
//            return new SpannableString(string);
//        }
//    }

//    public static Spanned convertSpannedString(CharSequence string) {
//        if (string instanceof Spanned) {
//            return (Spanned) string;
//        } else {
//            return new SpannedString(string);
//        }
//    }
//
//    public static String getPathFromUri(Context context, Uri uri) {
//        //Log.d(TAG, "getPathFromUri, uri: " + uri);
//
//        String path = "";
//        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//            Cursor cursor = null;
//            try {
//                String[] data = {MediaStore.Files.FileColumns.DATA};
//                cursor = context.getContentResolver().query(uri, data, null, null, null);
//                if (cursor != null) {
//                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
//                    cursor.moveToFirst();
//                    path = cursor.getString(column_index);
//                }
//
//                if (cursor == null || path == null || (path != null && !path.contains(".sdoc"))) {
//                    String fileName = new File(getContentName(context.getContentResolver(), uri)).getName();
//                    String cachePath = context.getCacheDir() + "/" + fileName;
//                    if (Util.saveUriToFile(context, uri, cachePath)) {
//                        //Log.d(TAG, "getPathFromUri, cachePath: " + cachePath);
//                        path = cachePath;
//                    } else {
//                        path = "";
//                    }
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "getPathFromUri", e);
//            } finally {
//                if (cursor != null && !cursor.isClosed()) {
//                    cursor.close();
//                }
//            }
//        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//            path = uri.getPath();
//        }
//        //Log.d(TAG, "getPathFromUri, path: " + path);
//        return path;
//    }

//    private static String getContentName(ContentResolver resolver, Uri uri) {
//        Cursor cursor = null;
//        String res = null;
//        try {
//            cursor = resolver.query(uri, new String[]{
//                    MediaStore.MediaColumns.DISPLAY_NAME
//            }, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
//                if (nameIndex >= 0)
//                    res = cursor.getString(nameIndex);
//            }
//        } catch (SQLiteException e) {
//            Log.e(TAG, "Exception occurred in getContentName.");
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return res;
//    }
//
//    public static boolean saveUriToFile(Context context, Uri uri, String target) {
//
//        //Log.d(TAG, "saveUriToFile, uri: " + uri + ", target: " + target);
//
//        byte[] bytes = new byte[1024]; // kB at a time
//        try {
//            InputStream in = context.getContentResolver().openInputStream(uri);
//            if (in == null) {
//                Log.e(TAG, "saveUriToFile, can not open input stream.");
//                return false;
//            }
//            File file = new File(target);
//            if (file.exists()) {
//                file.delete();
//            }
//            if (!file.createNewFile()) {
//                Log.e(TAG, "saveUriToFile, can not create new file.");
//                return false;
//            }
//            OutputStream out = new FileOutputStream(file);
//
//            int bytesRead;
//            while ((bytesRead = in.read(bytes)) > 0) {
//                out.write(Arrays.copyOfRange(bytes, 0, Math.max(0, bytesRead)));
//            }
//            in.close();
//            out.close();
//        } catch (Exception e) {
//            Log.e(TAG, "saveUriToFile, ", e);
//            return false;
//        }
//
//        return true;
//    }

//    public static CharSequence concat(List<CharSequence> text) {
//        if (text.size() == 0) {
//            return "";
//        }
//
//        if (text.size() == 1) {
//            return text.get(0);
//        }
//
//        boolean spanned = false;
//        for (int i = 0; i < text.size(); i++) {
//            if (text.get(i) instanceof Spanned) {
//                spanned = true;
//                break;
//            }
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < text.size(); i++) {
//            sb.append(text.get(i));
//        }
//
//        if (!spanned) {
//            return sb.toString();
//        }
//
//        SpannableString ss = new SpannableString(sb);
//        int off = 0;
//        for (int i = 0; i < text.size(); i++) {
//            int len = text.get(i).length();
//
//            if (text.get(i) instanceof Spanned) {
//                copySpansFrom((Spanned) text.get(i), 0, len, Object.class, ss, off);
//            }
//
//            off += len;
//        }
//
//        return new SpannedString(ss);
//    }
//
//    public static CharSequence concat(CharSequence... text) {
//        if (text.length == 0) {
//            return "";
//        }
//
//        if (text.length == 1) {
//            return text[0];
//        }
//
//        boolean spanned = false;
//        for (int i = 0; i < text.length; i++) {
//            if (text[i] instanceof Spanned) {
//                spanned = true;
//                break;
//            }
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < text.length; i++) {
//            sb.append(text[i]);
//        }
//
//        if (!spanned) {
//            return sb.toString();
//        }
//
//        SpannableString ss = new SpannableString(sb);
//        int off = 0;
//        for (int i = 0; i < text.length; i++) {
//            int len = text[i].length();
//
//            if (text[i] instanceof Spanned) {
//                copySpansFrom((Spanned) text[i], 0, len, Object.class, ss, off);
//            }
//
//            off += len;
//        }
//
//        return new SpannedString(ss);
//    }

    private static void copySpansFrom(Spanned source, int start, int end,
                                      Class kind,
                                      Spannable dest, int destoff) {
        if (kind == null) {
            kind = Object.class;
        }

        Object[] spans = source.getSpans(start, end, kind);
        int size = spans.length;
        for (int i = 0; i < size; i++) {
            int st = source.getSpanStart(spans[i]);
            int en = source.getSpanEnd(spans[i]);
            int fl = source.getSpanFlags(spans[i]);

            if ((st < start && en < 0) || (st > source.length() && en > source.length())) {
                continue;
            }

            if (st < start)
                st = start;
            if (en > end)
                en = end;

            if (st > en) {
                continue;
            }

            dest.setSpan(spans[i], st - start + destoff, en - start + destoff,
                    fl);
        }
    }

//    public static String convertToArabicNumber(int number) {
//        return convertToArabicNumber(String.valueOf(number));
//    }

    public static String convertToArabicNumber(String number) {
        if (Locale.getDefault().getLanguage().equals("ar")) {
            return convertToArabic(number);
        } else
            return number;
    }

    private static char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};

    public static String convertToArabic(String number) {
        StringBuilder builder = new StringBuilder();
        int length = number.length();
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(number.charAt(i))) {
                builder.append(arabicChars[(number.charAt(i)) - 48]);
            } else {
                builder.append(number.charAt(i));
            }
        }
        return builder.toString();
    }

//    public static int isEnableAirViewSettings(Context context) {
//        return Settings.System.getInt(context.getContentResolver(), "pen_hovering", 0);
//    }

    private static final int FREE_SIZE_THRESHOLD = 100;

//    public static boolean checkStorageFreeSize() {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSizeLong();
//        long availableBlocks = stat.getAvailableBlocksLong();
//        long freeSize = availableBlocks * blockSize / 1048576;
//        if (freeSize > FREE_SIZE_THRESHOLD) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public static long getDifferenceDay(long startDate, long endDate) {
//        //1 minute = 60 seconds
//        //1 hour = 60 x 60 = 3600
//        //1 day = 3600 x 24 = 86400
//
//        //milliseconds
//        long different = endDate - startDate;
//
////        Log.d(TAG, "startDate : " + startDate);
////        Log.d(TAG, "endDate : "+ endDate);
////        Log.d(TAG, "different : " + different);
//
//        long secondsInMilli = 1000;
//        long minutesInMilli = secondsInMilli * 60;
//        long hoursInMilli = minutesInMilli * 60;
//        long daysInMilli = hoursInMilli * 24;
//
//        long elapsedDays = different / daysInMilli;
//        different = different % daysInMilli;
//
//        long elapsedHours = different / hoursInMilli;
////        different = different % hoursInMilli;
////
////        long elapsedMinutes = different / minutesInMilli;
////        different = different % minutesInMilli;
////
////        long elapsedSeconds = different / secondsInMilli;
//
//        return elapsedDays;
//    }

//    public static long getDifferenceHour(long startDate, long endDate) {
//        //1 minute = 60 seconds
//        //1 hour = 60 x 60 = 3600
//        //1 day = 3600 x 24 = 86400
//
//        //milliseconds
//        long different = endDate - startDate;
//
////        Log.d(TAG, "startDate : " + startDate);
////        Log.d(TAG, "endDate : "+ endDate);
////        Log.d(TAG, "different : " + different);
//
//        long secondsInMilli = 1000;
//        long minutesInMilli = secondsInMilli * 60;
//        long hoursInMilli = minutesInMilli * 60;
//        long daysInMilli = hoursInMilli * 24;
//
//        long elapsedDays = different / daysInMilli;
//        different = different % daysInMilli;
//
//        long elapsedHours = different / hoursInMilli;
////        different = different % hoursInMilli;
////
////        long elapsedMinutes = different / minutesInMilli;
////        different = different % minutesInMilli;
////
////        long elapsedSeconds = different / secondsInMilli;
//
//        return elapsedHours;
//    }
//
//
//    public static Rect convertRelative(Rect srcRect, float mPanX, float mPanY, float mRatio) {
//        Rect dstRect = new Rect();
//
//        dstRect.left = (int) ((srcRect.left - mPanX) * mRatio);
//        dstRect.right = (int) ((srcRect.right - mPanX) * mRatio);
//        dstRect.top = (int) ((srcRect.top - mPanY) * mRatio);
//        dstRect.bottom = (int) ((srcRect.bottom - mPanY) * mRatio);
//
//        return dstRect;
//    }
//
//    public static boolean isScreenLocked(Context context) {
//        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        boolean isLocked = km.inKeyguardRestrictedInputMode();
//        Log.d(TAG, "isScreenLocked, isLocked: " + isLocked);
//        return isLocked;
//    }
//
//    public static void clearSpans(Spannable spannableString) {
//        ParcelableSpan[] spans = spannableString.getSpans(0, spannableString.length(), ParcelableSpan.class);
//        for (int i = spans.length - 1; i >= 0; i--) {
//            spannableString.removeSpan(spans[i]);
//        }
//    }
//
//    public static boolean isIndianChar(char c) {
//        return (c >= 0x0900 && c < 0x0DFF);
//    }
//
//    public static boolean isKhmerChar(char c) {
//        return (c >= 0x1780 && c <= 0x17F9);
//    }
//
//    public static boolean isLaoChar(char c) {
//        return ((c >= 0x0E81 && c <= 0x0EDD) || (c >= 0xE001 && c <= 0xE018));
//    }
//
//    public static boolean isThaiChar(char c) {
//        return (c >= 0x0E01 && c < 0x0E5B);
//    }
//
//    public static boolean isHalant(char c) {
//        if ((c == 0x94d) || (c == 0x9cd)
//                || (c == 0xa4d) || (c == 0xacd)
//                || (c == 0xbcd) || (c == 0xc4d)
//                || (c == 0xccd) || (c == 0xd4d)
//                || (c == 0xddf) || (c == 0x0B4d)) {
//            return true;
//        }
//        return false;
//    }
//
//    public static int indexOfWordPrefix(CharSequence text, char[] prefix) {
//        int textLength = text.length();
//        int prefixLength = prefix.length;
//
//        if (prefixLength == 0 || textLength < prefixLength) {
//            return -1;
//        }
//
//        int i = 0;
//        while (i < textLength) {
//            // Skip non-word characters
//            while (i < textLength && (!Character.isLetterOrDigit(text.charAt(i)) && !Character.isSpaceChar(text.charAt(i)))) {
//                i++;
//            }
//
//            if (i + prefixLength > textLength) {
//                return -1;
//            }
//
//            // Compare the prefixes
//            int j = 0;
//            for (j = 0; j < prefixLength; j++) {
//                if (Character.toUpperCase(text.charAt(i + j)) != Character.toUpperCase(prefix[j])) {
//                    break;
//                }
//            }
//            if (j == prefixLength) {
//                return i;
//            }
//            i++;
//        }
//
//        return -1;
//    }


//    public static char[] getPrefixCharForIndian(TextPaint paint, CharSequence text, char[] prefix) {
//        int i = 0;
//        int pos = 0;
//        int len = text.length();
//
//        if (len == 0 || prefix == null) {
//            return null;
//        }
//
//        float[] widths = new float[len];
//        char[] chars = new char[len];
//
//        for (i = 0; i < prefix.length; i++) {
//            if (isIndianChar(prefix[i]) || isThaiChar(prefix[i])
//                    || isKhmerChar(prefix[i]) || isLaoChar(prefix[i])) {
//                break;
//            }
//        }
//        if (i == prefix.length) {
//            return null;
//        }
//
//        pos = indexOfWordPrefix(text, prefix);
//        if (pos < 0 || pos >= len) {
//            return null;
//        }
//
//        getChars(text, 0, len, chars, 0);
//        if (paint != null) {
//            try {
//                Paint obj = new Paint();
//                Method m = Paint.class.getMethod("getTextRunAdvances", char[].class, int.class, int.class, int.class, int.class, boolean.class, float[].class, int.class);
//                m.invoke(obj, chars, 0, len, 0, len, false, widths, 0);
//            } catch (NoSuchMethodException e) {
//                Log.e(TAG, e.getMessage());
//            } catch (IllegalAccessException e) {
//                Log.e(TAG, e.getMessage());
//            } catch (InvocationTargetException e) {
//                Log.e(TAG, e.getMessage());
//            }
//        }
//
//        int pre_pos_halant = pos;
//        if (isIndianChar(prefix[i])) {
//            while (pre_pos_halant > 0) {
//                if (isHalant(chars[pre_pos_halant - 1])) {
//                    pre_pos_halant = pre_pos_halant - 2;
//                } else {
//                    break;
//                }
//            }
//
//            if (pre_pos_halant < 0) {
//                return null;
//            }
//        } else {
//            while (pos > 0 && widths[pos] == 0) {
//                pos--;
//            }
//            pre_pos_halant = pos;
//        }
//
//        i = pos + prefix.length;
//        while (i < len) {
//            if (widths[i] != 0 && !isHalant(chars[i - 1])) {
//                break;
//            }
//            i++;
//        }
//
//        int destLength = i - pre_pos_halant;
//        char[] dest = new char[destLength];
//
//        for (int j = 0; j < destLength; j++) {
//            dest[j] = chars[pre_pos_halant + j];
//        }
//        return dest;
//    }

    public static void getChars(CharSequence s, int start, int end,
                                char[] dest, int destoff) {
        Class<? extends CharSequence> c = s.getClass();

        if (c == String.class)
            ((String) s).getChars(start, end, dest, destoff);
        else if (c == StringBuffer.class)
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        else if (c == StringBuilder.class)
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        else if (s instanceof GetChars)
            ((GetChars) s).getChars(start, end, dest, destoff);
        else {
            for (int i = start; i < end; i++)
                dest[destoff++] = s.charAt(i);
        }
    }

//    public static boolean isRtlTextMode(EditText editText, int start) {
//        if (editText == null || editText.length() == 0) {
//            return false;
//        }
//
//        boolean isRtlMode = false;
//        try {
//            isRtlMode |= editText.getLayout().isRtlCharAt(start == editText.length() ? editText.length() - 1 : 0);
//
//            Class<?> layout = Class.forName("android.text.Layout");
//            Method isLevelBoundaryMethod = layout.getMethod("isLevelBoundary", int.class);
//            isRtlMode |= (boolean) isLevelBoundaryMethod.invoke(editText.getLayout(), start == editText.length() ? editText.length() : 0);
//        } catch (Exception e) {
//            Log.e(TAG, "isRtlTextMode : " + e);
//        }
//
//        return isRtlMode;
//    }
//
//    public static void setTaskDescription(Activity activity, int colorId) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
//            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(activity.getString(FeatureUtils.isSecBrandAsGalaxy() ? R.string.notes_jp : R.string.app_name), bm, ContextCompat.getColor(activity, colorId));
//            activity.setTaskDescription(taskDesc);
//        }
//    }
//
//    public static boolean isTablet(Context context) {
//        return context != null && context.getResources().getInteger(R.integer.isTablet) == 1;
//    }

//    private static Boolean mIsTabS2 = null;
//    private static String[] mExceptModelList = {"SM-T715", "SM-T815"};

//    public static boolean isTabS2() {
//        if (mIsTabS2 == null) {
//            mIsTabS2 = new Boolean(false);
//            for (String n : mExceptModelList) {
//                if (Build.MODEL.equals(n)) {
//                    mIsTabS2 = true;
//                    break;
//                }
//            }
//        }
//        return mIsTabS2;
//    }
//
//    public static boolean isSpenModel() {
//        return new Spen().isFeatureEnabled(Spen.DEVICE_PEN);
//    }
//
//    public static String getDateFormat(long modifyTime) {
//        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMd");
//        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
//        return dateFormat.format(modifyTime);
//    }
//
//    public static boolean isScreenOffMemo(@NonNull Context context){
//        return (Settings.System.getInt(context.getContentResolver(), "screen_off_memo", 0) == 1);
//    }

    private static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

//    public static boolean isBatteryLowForSync(Context context) {
//        if(getBatteryLevel(context) < LIMIT_BATTERY_LEVEL_FOR_SYNC) {
//            return true;
//        }
//        return false;
//    }
}
