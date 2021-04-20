package com.gal.carapp;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class TimerCounter {
    private long duration;
    private long interval;
    private CountDownTimer timer;

    public TimerCounter(long duration, long interval) {
        this.duration = duration;
        this.interval = interval;
        this.timer = new CountDownTimer(duration, interval) {
            @Override
            public void onTick(long l) {
                Log.e(this.toString(), "Tick "+l/1000);
            }

            @Override
            public void onFinish() {
                Log.e(this.toString(), "Finish");
            }
        };
    }

    public void cancel() {
        timer.cancel();
    }

    public CountDownTimer start() {
        return timer.start();
    }


}
