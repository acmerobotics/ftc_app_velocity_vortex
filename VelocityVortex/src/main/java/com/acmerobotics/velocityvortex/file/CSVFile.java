package com.acmerobotics.velocityvortex.file;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;

public class CSVFile<T> extends DataFile {

    private static final String TAG = "CSVFile";

    private Field[] fields;
    private String[] fieldNames;

    public CSVFile(String filename, Class<? extends T> impl) {
        super(filename);

        fields = impl.getFields();
        fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }

        writeHeader();
    }

    public void writeHeader() {
        write(TextUtils.join(",", fieldNames) + "\n");
    }

    public void write(T o) {
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                values[i] = fields[i].get(o).toString();
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        write(TextUtils.join(",", fieldNames) + "\n");
    }

}
