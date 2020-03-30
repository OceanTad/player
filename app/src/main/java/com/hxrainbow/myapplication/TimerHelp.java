package com.hxrainbow.myapplication;

import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

public class TimerHelp {

    private Handler mHandler;
    private TimerTask mTask;
    private Timer mTimer;

    private long period = 1000;
    private long delay = 1000;

    private static boolean isRunning = false;
    private static int count = 0;

    public TimerHelp(long period, long delay) {
        this.period = period;
        this.delay = delay;
    }

    public synchronized void execute(final ExecuteTask task) {
        if (isRunning) {
            return;
        }
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        if (mTask == null) {
            mTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            count++;
                            task.update(count);
                        }
                    });
                }
            };
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }
        count = 0;
        mTimer.schedule(mTask, delay, period);
        isRunning = true;
    }

    public synchronized void cancel() {
        if (!isRunning) {
            return;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTask = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        isRunning = false;
        count = 0;
    }

    public synchronized void destroy() {
        if (mTimer != null) {
            if (isRunning) {
                mTimer.cancel();
                isRunning = false;
            }
            mTimer = null;
            mTask = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public interface ExecuteTask {

        void update(int count);

    }

}
