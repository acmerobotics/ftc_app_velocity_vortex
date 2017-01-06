package com.acmerobotics.library.logging;

public abstract class Logger {

    public abstract void msg(String msg);
    public abstract void error(String msg);

    public void msg(String format, Object... args) {
        msg(String.format(format, args));
    }

    public void error(String format, Object... args) {
        error(String.format(format, args));
    }

}