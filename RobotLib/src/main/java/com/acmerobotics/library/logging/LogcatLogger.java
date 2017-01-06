package com.acmerobotics.library.logging;

import android.util.Log;

public class LogcatLogger extends Logger {

    private String tag;

    public LogcatLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void msg(String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void error(String msg) {
        Log.e(tag, msg);
    }
}
