package com.samsung.android.sdk.pen.pg.utils.web;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.android.sdk.composer.document.SpenContentBase;
import com.samsung.android.sdk.composer.document.SpenContentWeb;
import com.samsung.android.sdk.pen.pg.utils.web.util.ComposerAsyncLooper;
import com.samsung.android.sdk.pen.pg.utils.web.util.Util;
import com.samsung.android.sdk.pen.pg.utils.web.util.WebCardData;

/**
 * Created by EUNJI on 2016-09-22.
 */
public class WebManager {
    private final static String TAG = "WebManager";

    private static Context mContext;

    private ComposerAsyncLooper mAsyncLooper = new ComposerAsyncLooper();

    static public void setContext(Context context) {
        mContext = context;
    }

    //from Native
    public boolean createWebData(final SpenContentWeb content) {

        Log.d(TAG, "createWebData!!!! java " + content.getUri());
        downloadWebDataAsync(content, 0);

        return true;
    }

    public boolean isRunning() {
        return mAsyncLooper.isLooperRunning();
    }

    public void stop() {
        mAsyncLooper.terminate();
    }

    private void downloadWebDataAsync(final SpenContentWeb content, final int tryCnt) {

        final String url = content.getUri();
        final ComposerAsyncLooper.State state = ComposerAsyncLooper.State.getState(content.getState());
        Log.d(TAG, "downloadWebDataAsync, state: " + state.name() + " , url:" + content.getUri() + ",content:" + content);

        switch (state) {
            case None:
                break;
            case Init:
                if (TextUtils.isEmpty(url)) {
                    //return ComposerAsyncLooper.State.Exception;
                    content.setState(SpenContentBase.STATE_EXCEPTION);
                    return;
                }

                if (!Util.isNetworkOnline(mContext)) {
                    //return ComposerAsyncLooper.State.Fail;
                    content.setState(SpenContentBase.STATE_FAIL);
                    return;
                }

                final int WEBCARD_TIMEOUT_SECOND = 8 * 1000;
                mAsyncLooper.request(new ComposerAsyncLooper.AsyncRunnable() {
                    @Override
                    public boolean run() {
//                        if(tryCnt==0)
//                            return false;
                        WebCardData data = WebCardData.createWebCard(mContext, url);
                        Log.d(TAG, "downloadWebDataAsync$run, data: " + data);

                        if (content == null) {
                            return false;
                        }
                        content.setTitle(Html.fromHtml(data.title).toString());
                        content.setBody(Html.fromHtml(data.description).toString());
                        content.setUri(data.url);
                        content.setThumbnailPath(data.path);
                        content.setImageTypeId(data.imageTypeId);

                        return true;
                    }
                }, new ComposerAsyncLooper.OnStateChangeListener() {
                    @Override
                    public void onStateChange(ComposerAsyncLooper.State state, Object obj) {
                        Log.d(TAG, "downloadWebDataAsync$onStateChange, state: " + state.name());
                        if (content == null) {
                            return;
                        }

                        content.setState(state.getId());

                        switch (state) {
                            case None:
                                break;
                            case Init:
                            case Ready:
                            case Running:
                                //content.setState(SpenContentBase.STATE_RUNNING);
                                break;
                            case Done:
                                //content.setState(SpenContentBase.STATE_DONE);
                                break;
                            case Fail:
                            case Exception:
                                //content.setState(SpenContentBase.STATE_EXCEPTION);
                                int executorHash = mAsyncLooper.getCurrentExecutorHash();
                                //if (mRecyclerView != null)
                            {
                                //final int tryCnt = paragraph.getInt(Paragraph.Key.Web.TRY_CNT, 1);
                                Log.d(TAG, "downloadWebDataAsync$onStateChange, current tryCnt: " + tryCnt);
                                final int MAX_TRY_COUNT = 2;
                                if (tryCnt < MAX_TRY_COUNT) {
                                    Log.d(TAG, "downloadWebDataAsync$onStateChange, request retry.");
                                    retry(content, tryCnt, executorHash);
                                }
                            }
                            break;
                            case Canceled:
                                //setState(nativeHolder, STATE_FAIL);
                                break;
                        }
                    }

                    private void retry(final SpenContentWeb content, final int TRY_CNT, final int executorHash) {
                        final int RETRY_AFTER_SECOND = 2000;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d(TAG, "downloadWebDataAsync$retry sleep " + RETRY_AFTER_SECOND);
                                    Thread.sleep(RETRY_AFTER_SECOND);
                                    Log.d(TAG, "downloadWebDataAsync$retry");
                                    if (mAsyncLooper.isExecutorHash(executorHash)) {
                                        int try_Cnt = TRY_CNT;
                                        try_Cnt += 1;
                                        Log.d(TAG, "downloadWebDataAsync$retry, set tryCnt: " + try_Cnt);

                                        downloadWebDataAsync(content, try_Cnt);
                                    }
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "downloadWebDataAsync$retry Fail: " + e.toString());
                                }
                            }
                        }).start();

                    }
                }, ComposerAsyncLooper.Priority.Low, WEBCARD_TIMEOUT_SECOND);
                break;
            case Ready:
                break;
            case Running:
                break;
            case Done:
                break;
            case Fail:
                break;
            case Exception:
                break;
        }
        //return state;

    }
}
