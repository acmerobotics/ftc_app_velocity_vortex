package com.acmerobotics.library.logging;

import java.util.ArrayList;
import java.util.List;

public class MultipleLogger extends Logger {

    private List<Logger> loggers;

    public MultipleLogger() {
        loggers = new ArrayList<Logger>();
    }

    public void addLogger(Logger logger) {
        loggers.add(logger);
    }

    @Override
    public void msg(String msg) {
        for (Logger logger : loggers) {
            logger.msg(msg);
        }
    }

    @Override
    public void error(String msg) {
        for (Logger logger : loggers) {
            logger.msg(msg);
        }
    }
}
