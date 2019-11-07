package com.samsung.android.sdk.pen.pg.example6_1;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.settingui.SpenSettingBrushLayout;

import java.util.HashMap;
import java.util.List;

public class DrawingSettingView extends FrameLayout {
    public static final int MODE_PEN = 1;

    private SpenSettingBrushLayout mBrushSettingView;

    String mCustomImagepath = "";

    public DrawingSettingView(Context context, HashMap<String, Integer> resourceIds, HashMap<String, String> fontName,
                              String imagePath, RelativeLayout canvasLayout) {
        super(context);
        initSettingView(context, resourceIds, fontName, imagePath, canvasLayout);
    }

    private void initSettingView(Context context, HashMap<String, Integer> resourceIds,
                                 HashMap<String, String> fontName, String imagePath, RelativeLayout canvasLayout) {
        if (fontName.containsKey("R.string.sdk_resource_path")) {
            mCustomImagepath = imagePath;
        }

        mBrushSettingView = new SpenSettingBrushLayout(context, mCustomImagepath, canvasLayout);
        mBrushSettingView.setVisibility(View.VISIBLE);
    }

    public boolean isSettingViewVisible(int nWhichSettingView) {
        switch (nWhichSettingView) {
            case MODE_PEN:
                return mBrushSettingView.isShown();

            default:
                return false;
        }
    }

    public void setSettingViewVisible(int nWhichSettingView, int visible) {
        switch (nWhichSettingView) {
            case MODE_PEN:
                mBrushSettingView.setVisibility(visible);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setSpenView(SpenSettingViewInterface spenView) {
        mBrushSettingView.setCanvasView(spenView);
    }

    public void setViewMode(int type, int viewMode) {
        switch (type) {
            case MODE_PEN:
                mBrushSettingView.setVisibility(VISIBLE);
                break;
        }
    }

    public void setPenInfo(SpenSettingPenInfo settingInfo) {
        mBrushSettingView.setInfo(settingInfo);
    }

    public void setPenInfoList(List<SpenSettingPenInfo> list) {
        mBrushSettingView.setPenInfoList(list);
    }

    public SpenSettingPenInfo getPenInfo() {
        return mBrushSettingView.getInfo();
    }

    public SpenSettingBrushLayout getBrushPenSettingLayout() {
        return mBrushSettingView;
    }

    public void close() {
        if (mBrushSettingView != null) {
            mBrushSettingView.close();
            mBrushSettingView = null;
        }
    }
}
