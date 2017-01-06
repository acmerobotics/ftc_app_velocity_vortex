package com.acmerobotics.library.file;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Array;
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
        write(TextUtils.join(",", fieldNames));
    }

    public void write(T o) {
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                Object field = fields[i].get(o);
                if (field.getClass().isArray()) {
                    String arr = "[";
                    int length = Array.getLength(field);
                    for (int j = 0; j < length; j ++) {
                        arr +=  Array.get(field, j).toString() + ",";
                    }
                    values[i] = arr.substring(0, arr.length() - 1) + "]";
                } else {
                    values[i] = field.toString();
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        write(TextUtils.join(",", values));
    }

}
