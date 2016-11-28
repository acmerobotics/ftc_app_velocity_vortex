package com.acmerobotics.library.file;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataFile implements AutoCloseable {

    private static final String TAG = "DataFile";

    private File file;
    private BufferedReader reader;
    private BufferedWriter writer;

    public DataFile(String filename) {
        openFile(filename);
    }

    protected void openFile(String filename) {
        File dir = getStorageDir();
        dir.mkdirs();
        this.file = new File(dir, filename);
        try {
            this.file.createNewFile();
            this.reader = new BufferedReader(new FileReader(file));
            this.writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            Log.e(TAG, "IO error while trying to open data file " + file.getPath() + "\n" + e.getMessage());
        }
    }

    public static File getStorageDir() {
        return new File(Environment.getExternalStorageDirectory(), "ACME");
    }

    public BufferedReader getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public File getFile() {
        return file;
    }

    public void write(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            Log.e(TAG, "IO error while attempting to write data to file\n" + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "IO error while trying to close the data file\n" + e.getMessage());
        }
    }
}
