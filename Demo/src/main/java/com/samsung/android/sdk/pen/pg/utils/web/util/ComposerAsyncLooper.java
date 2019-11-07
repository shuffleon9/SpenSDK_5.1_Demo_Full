package com.samsung.android.sdk.pen.pg.utils.web.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by EUNJI on 2016-09-29.
 */
public class ComposerAsyncLooper {
    private static final String TAG = "ComposerAsyncLooper";

    private static final int TIMEOUT_JOB_SECOND = 10;

    public synchronized void request(AsyncRunnable r, OnStateChangeListener l, Priority p) {
        request(r, l, p, TIMEOUT_JOB_SECOND);
    }

    public synchronized void request(AsyncRunnable r, OnStateChangeListener l, Priority p, int timeoutSecond) {
        PriorityJob job = new PriorityJob();
        job.runnable = r;
        job.priority = p;
        job.listener = l;
        job.requestTime = System.currentTimeMillis();
        job.timeout = timeoutSecond;
        mQueue.add(job);
        Log.d(TAG, "execute, request job: " + job);
        if (job.listener == null) {
            job.listener = mEmptyListener;
        }
        job.listener.onStateChange(State.Ready, null);
        startLooperThread();
    }

    public int getCurrentExecutorHash() {
        int hash = mExecutor.hashCode();
        Log.d(TAG, "getCurrentExecutorHash, " + hash);
        return hash;
    }

    public boolean isExecutorHash(int hash) {
        int currentHash = mExecutor.hashCode();
        Log.d(TAG, "isExecutorHash, " + hash + ", currentHash: " + currentHash);
        return currentHash == hash;
    }

    public synchronized boolean remainMustDoPriority() {
        Log.d(TAG, "remainMustDoPriority start, mQueue size: " + mQueue.size());
        ArrayList<PriorityJob> mustDoJobList = new ArrayList<>();

        PriorityJob[] copy = mQueue.toArray(new PriorityJob[mQueue.size()]);
        for (PriorityJob job : copy) {
            if (job.priority == Priority.MustDo) {
                mustDoJobList.add(job);
            } else {
                if (job.listener != null) {
                    job.listener.onStateChange(State.Canceled, null);
                }
            }
        }
        mQueue.clear();
        mQueue.addAll(mustDoJobList);
        int mustDoJobListSize = mustDoJobList.size();

        boolean isRunningMustDoJob = false;
        Log.d(TAG, "remainMustDoPriority, mRunningJob size: " + mRunningJob.size());
        PriorityJob[] runningCopy = mRunningJob.toArray(new PriorityJob[mRunningJob.size()]);
        for (PriorityJob job : runningCopy) {
            if (job.priority == Priority.MustDo) {
                isRunningMustDoJob = true;
            }
        }

        Log.d(TAG, "remainMustDoPriority, done, mustDoJobListSize: " + mustDoJobListSize
                + ", isRunningMustDoJob: " + isRunningMustDoJob);

        boolean needToWait = (isRunningMustDoJob || (mustDoJobListSize > 0));
        Log.d(TAG, "remainMustDoPriority, done, needToWait: " + needToWait);
        if (needToWait) {
            startLooperThread();
        }
        return needToWait;
    }

    public synchronized void terminate() {
        Log.d(TAG, "terminate start, size: " + mQueue.size());
        PriorityJob[] copy = mQueue.toArray(new PriorityJob[mQueue.size()]);
        for (PriorityJob job : copy) {
            if (job.listener != null) {
                job.listener.onStateChange(State.Canceled, null);
            }
        }

        mQueue.clear();

        Log.d(TAG, "terminate runningJob, size: " + mRunningJob.size());
        mRunningJob.clear();

        terminateExecutorService(mExecutor);
        mExecutor = Executors.newFixedThreadPool(3, new TagThreadFactory("ComposerAsyncLooper$mExecutor"));
        // terminateExecutorService(mLooperThread);
        Log.d(TAG, "terminate, done");
    }

    public enum State {
        None,
        Init,
        Ready,
        Running,
        Done,
        Fail,
        Exception,
        Canceled;

        public int getId() {
            return ordinal();
        }

        public static State getState(int id) {
            return State.values()[id];
        }
    }

    public interface OnStateChangeListener {
        void onStateChange(State state, Object obj);
    }

    public interface AsyncRunnable {
        boolean run();
    }

    private static class PriorityJob {
        Priority priority;
        AsyncRunnable runnable;
        OnStateChangeListener listener;
        int timeout;
        long requestTime;

        @Override
        public String toString() {
            return "Priority: " + priority.name() + ", requestTime: " + requestTime + ", timeout: " + timeout;
        }
    }

    public enum Priority {
        Low,
        High,
        MustDo,
    }

    private ExecutorService mExecutor = Executors.newFixedThreadPool(3, new TagThreadFactory("ComposerAsyncLooper$mExecutor"));
    private PriorityBlockingQueue<PriorityJob> mQueue = new PriorityBlockingQueue<>(10, new CompareDescending());
    private ArrayList<PriorityJob> mRunningJob = new ArrayList<>();

    private OnStateChangeListener mEmptyListener = new OnStateChangeListener() {
        @Override
        public void onStateChange(State state, Object obj) {
        }
    };

    private ExecutorService mLooperThread = Executors.newSingleThreadExecutor(new TagThreadFactory("ComposerAsyncLooper$mLooperThread"));

    private void startLooperThread() {
        Log.d(TAG, "startLooperThread, mIsLooperIsRunning: " + mIsLooperIsRunning + ", queue size: " + mQueue.size());
        if (!mIsLooperIsRunning) {
            mLooperThread.execute(new Runnable() {
                @Override
                public void run() {
                    execute();
                }
            });
        }
    }

    private boolean mIsLooperIsRunning = false;

    private void execute() {
        mIsLooperIsRunning = true;

        while (true) {
            Log.d(TAG, "execute, queue size: " + mQueue.size());
            if (mQueue.size() < 1) {
                break;
            }

            try {
                final PriorityJob job;
                synchronized (this) {
                    job = mQueue.poll(2, TimeUnit.SECONDS);
                }
                Log.d(TAG, "execute, poll job: " + job);
                if (job == null) {
                    Log.d(TAG, "execute, job is null.");
                    continue;
                }
                if (mExecutor.isTerminated() || mExecutor.isShutdown()) {
                    Log.d(TAG, "execute, executor is terminated.");
                    if (job.listener != null) {
                        job.listener.onStateChange(State.Canceled, null);
                    }
                    break;
                }
                int executorHash = mExecutor.hashCode();
                Log.d(TAG, "execute, executorHash: " + executorHash);
                Future<Boolean> future = mExecutor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mRunningJob.add(job);
                        if (job.listener != null) {
                            job.listener.onStateChange(State.Running, null);
                        }
                        Log.d(TAG, "execute$call, start, job: " + job);
                        boolean result = job.runnable.run();
                        Log.d(TAG, "execute$call, end, job: " + job);
                        return result;
                    }
                });

                try {
                    int timeout = job.timeout;
                    if (timeout < 1) {
                        timeout = TIMEOUT_JOB_SECOND;
                    }
                    boolean result = future.get(timeout, TimeUnit.SECONDS);
                    Log.d(TAG, "execute, result: " + result + ", timeout: " + timeout);
                    if (job.listener != null) {
                        if (mExecutor.hashCode() != executorHash) {
                            Log.d(TAG, "execute, execute changed.");
                            job.listener.onStateChange(State.Canceled, result);
                        } else {
                            job.listener.onStateChange((result ? State.Done : State.Fail), result);
                        }
                    }
                } catch (ExecutionException | TimeoutException e) {
                    Log.d(TAG, "execute, error; " + e.getMessage());
                    if (job.listener != null) {
                        job.listener.onStateChange(State.Exception, e);
                    }
                }
                mRunningJob.remove(job);
            } catch (InterruptedException e) {
                Log.d(TAG, "execute, error; " + e.getMessage());
            }
        }

        mIsLooperIsRunning = false;
        Log.d(TAG, "execute, finished");
        if (mFinishedListener != null) {
            mFinishedListener.onFinished();
            mFinishedListener = null;
        }
    }

    public boolean isLooperRunning() {
        Log.d(TAG, "isLooperRunning, mIsLooperIsRunning: " + mIsLooperIsRunning);
        return mIsLooperIsRunning;
    }

    private static class CompareDescending implements Comparator<PriorityJob> {
        @Override
        public int compare(PriorityJob o1, PriorityJob o2) {
            int ret = o2.priority.compareTo(o1.priority);
            if (ret == 0) {
                return (int) (o2.requestTime - o1.requestTime);
            }
            return ret;
        }
    }


    private void terminateExecutorService(ExecutorService es) {
        Log.d(TAG, "terminateExecutorService");
        try {
            es.shutdown();
            Log.d(TAG, "terminateExecutorService, wait executor service.");
            if (!es.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                Log.d(TAG, "terminateExecutorService, service is not terminate. shutdownNow");
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "terminateExecutorService", e);
            es.shutdownNow();
        }
    }

    private OnLooperFinishedListener mFinishedListener;

    public void setOnLooperFinishedListener(OnLooperFinishedListener l) {
        mFinishedListener = l;
    }

    public interface OnLooperFinishedListener {
        void onFinished();
    }
}
