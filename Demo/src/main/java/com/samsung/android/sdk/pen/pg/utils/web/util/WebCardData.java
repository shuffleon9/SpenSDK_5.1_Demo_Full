package com.samsung.android.sdk.pen.pg.utils.web.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by EUNJI on 2016-09-29.
 */
public class WebCardData {

    private static final String TAG = "WebCardData";

    public String title = "";
    public String description = "";
    public String imageUrl = "";
    public String url = "";
    public String path = "";
    public int imageTypeId = 0;

    public static final int IMAGE_TYPE_ID_NONE = 0;
    public static final int IMAGE_TYPE_ID_CONTENT = 1;
    public static final int IMAGE_TYPE_ID_FAVICON = 2;

    private WebCardData() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("title: ");
        sb.append(title);
        sb.append(", description: ");
        sb.append(description);
        sb.append(", imageUrl: ");
        sb.append(imageUrl);
        sb.append(", imageTypeId: ");
        sb.append(imageTypeId);
        sb.append(", path: ");
        sb.append(path);

        return sb.toString();
    }

    public static WebCardData createWebCard(Context context, String url) {
        Log.d(TAG, "createWebCard, url: " + url);

        WebCardData data = new WebCardData();
        data.url = url;

        TextCrawler tc = new TextCrawler();
        LinkSourceContent lsc = tc.makePreview(url);

        String title = lsc.getTitle();
        if (TextUtils.isEmpty(title)) {
            Log.d(TAG, "createWebCard, title is empty.");
            title = "";
        }
        data.title = title;

        String desc = lsc.getDescription();
        if (TextUtils.isEmpty(desc)) {
            Log.d(TAG, "createWebCard, desc is empty.");
            desc = "";
        }
        data.description = desc;

        LinkedHashMap<String, Integer> imageUrlCandidate = new LinkedHashMap<>();
        data.imageTypeId = IMAGE_TYPE_ID_NONE;
        if (lsc.getImages().size() > 0) {
            String imageUrl = lsc.getImages().get(0);
            if (!TextUtils.isEmpty(imageUrl)) {
                imageUrlCandidate.put(imageUrl, IMAGE_TYPE_ID_CONTENT);
            }
        }

        // imageUrlCandidate.put("http://www.google.com/s2/favicons?domain=" + url, IMAGE_TYPE_ID_FAVICON);

        //Context context = MemoApplication.getAppContext();

        //NEED TO CHECK
        String imagePath = null;
        String cachePath = Util.getImagePathByTimeInAppCache(context, "web", "jpg");
        //String cachePath = getFilesDir().getAbsolutePath() + File.separator +Util.getFileNameByTime("web","jpg");

        for (String imageUrl : imageUrlCandidate.keySet()) {
            int imageType = imageUrlCandidate.get(imageUrl);
            Bitmap bitmap = Util.downloadImage(imageUrl);
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.d(TAG, "createWebCard, width: " + width + ", height: " + height);
                int maxSize = Math.min(width, height);
                if (maxSize < 48) {
                    bitmap.recycle();
                    continue;
                }
                // int minSize = Math.min(width, height);
                // bitmap = ThumbnailUtils.extractThumbnail(bitmap, minSize, minSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                int maxWidth = (int) (1440 * 0.5f);//(ScreenDimension.getScreenMinSize(context) * 0.5f);
                bitmap = Util.Image.resizeBitmapImage(bitmap, maxWidth);
                if (bitmap != null) {
                    Util.saveBitmapToFileCache(bitmap, cachePath, Bitmap.CompressFormat.JPEG,
                            Constants.THUMBNAIL_COMPRESS_QUALITY);
                    bitmap.recycle();
                    if (new File(cachePath).exists()) {
                        imagePath = cachePath;
                        data.imageUrl = imageUrl;
                        data.imageTypeId = imageType;
                        break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(imagePath)) {
            imagePath = "";
            data.imageTypeId = IMAGE_TYPE_ID_NONE;
            final int MIN_WIDTH_SIZE = 768;
            Bitmap tileBitmap = new LetterTileProvider().getLetterTile(getFirstChar(url), MIN_WIDTH_SIZE, Color.GRAY);
            if (tileBitmap != null) {
                Util.saveBitmapToFileCache(tileBitmap, cachePath, Bitmap.CompressFormat.JPEG,
                        Constants.THUMBNAIL_COMPRESS_HIGH_QUALITY);
                if (new File(cachePath).exists()) {
                    Log.d(TAG, "createWebCard, set tile letter image.");
                    imagePath = cachePath;
                    data.imageUrl = Uri.fromFile(new File(cachePath)).toString();
                    data.imageTypeId = IMAGE_TYPE_ID_FAVICON;
                }
            }
        }
        data.path = imagePath;
        return data;
    }

    private static char getFirstChar(String strUrl) {
        char ret = 'W';
        Uri uri = Uri.parse(strUrl);
        String host = uri.getHost();
        Log.d(TAG, "getFirstChar, host: " + host);
        if (!TextUtils.isEmpty(host)) {
            ret = host.charAt(0);
            int dotCount = host.length() - host.replace(".", "").length();
            Log.d(TAG, "getFirstChar, dotCount: " + dotCount);
            if (dotCount > 1) {
                int dotNextIdx = host.indexOf('.') + 1;
                if (dotNextIdx < host.length()) {
                    ret = host.charAt(dotNextIdx);
                }
            }
        }
        Log.d(TAG, "getFirstChar, ret: " + ret);
        return ret;
    }
}
