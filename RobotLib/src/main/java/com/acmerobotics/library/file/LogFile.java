package com.acmerobotics.library.file;

import com.acmerobotics.library.logging.Logger;

public class LogFile extends Logger implements AutoCloseable {

    private DataFile file;

    public LogFile(String filename) {
        file = new DataFile(filename);
    }

    @Override
    public void msg(String msg) {
        file.write("INFO\t" + msg);
    }

    @Override
    public void error(String msg) {
        file.write("ERROR\t" + msg);
    }

    @Override
    public void close() throws Exception {
        file.close();
    }
}
