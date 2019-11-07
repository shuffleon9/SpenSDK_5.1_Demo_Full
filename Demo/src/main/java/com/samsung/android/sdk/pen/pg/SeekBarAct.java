package com.samsung.android.sdk.pen.pg;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.SeekBar;

import com.samsung.spensdk3.example.R;

public class SeekBarAct extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seek_bar);


        SeekBar seekBar = (SeekBar) findViewById(R.id.seek_bar_id);
        Resources.Theme theme = this.getTheme();
        seekBar.setMinimumHeight(19);
        seekBar.setPadding(7, 0, 7, 0);
        LayerDrawable thumbDrawable = (LayerDrawable) getResources().getDrawable(com.samsung.android.spen.R.drawable.snote_popup_progress_handler, theme);
        Drawable thumbColorDrawable = thumbDrawable.getDrawable(0);

        thumbDrawable.setDrawableByLayerId(com.samsung.android.spen.R.id.progress_handler_stroke, (Drawable) null);

        Drawable mThumbStrokeDrawable = thumbDrawable.getDrawable(1);
        seekBar.setThumb(thumbDrawable);

    }
}
