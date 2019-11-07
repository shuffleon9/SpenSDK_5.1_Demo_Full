package com.samsung.android.sdk.pen.pg.utils.web.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

/**
 * Created by EUNJI on 2016-09-29.
 */
public class LetterTileProvider {
    private static final String TAG = "LetterTileProvider";

    /**
     * The {@link TextPaint} used to draw the letter onto the tile
     */
    private final TextPaint mPaint = new TextPaint();
    /**
     * The bounds that enclose the letter
     */
    private final Rect mBounds = new Rect();
    /**
     * The {@link Canvas} to draw on
     */
    private final Canvas mCanvas = new Canvas();
    /**
     * The first char of the name being displayed
     */
    private final char[] mFirstChar = new char[1];

    /**
     * Constructor for <code>LetterTileProvider</code>
     */
    public LetterTileProvider() {
        mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);
    }

    /**
     * @param displayChar The name used to create the letter for the tile
     * @param size        The desired size of the tile
     * @param color       The desired background color of the tile
     * @return A {@link Bitmap} that contains a letter used in the English
     * alphabet or digit, if there is no letter or digit available, a
     * default image is shown instead
     */
    public Bitmap getLetterTile(char displayChar, int size, int color) {
        Log.d(TAG, "getLetterTile, displayChar: " + displayChar + ", size: " + size + ", color: " + color);

        final Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        final Canvas c = mCanvas;
        c.setBitmap(bitmap);
        c.drawColor(color);

        if (isEnglishLetterOrDigit(displayChar)) {
            mFirstChar[0] = Character.toUpperCase(displayChar);
            mPaint.setTextSize(size * 0.7f);
            mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
            c.drawText(mFirstChar, 0, 1, size / 2, size / 2
                    + (mBounds.bottom - mBounds.top) / 2, mPaint);
        }
        return bitmap;
    }

    /**
     * @param c The char to check
     * @return True if <code>c</code> is in the English alphabet or is a digit,
     * false otherwise
     */
    private static boolean isEnglishLetterOrDigit(char c) {
        return ('A' <= c && c <= 'Z')
                || ('a' <= c && c <= 'z')
                || ('0' <= c && c <= '9');
    }
}
