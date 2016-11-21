package com.acmerobotics.library.camera;

public enum CameraType {
    ANY,
    FRONT,
    REAR,
    CUSTOM;
    private int id;
    public static CameraType fromId(int id) {
        CameraType.CUSTOM.id = id;
        return CameraType.CUSTOM;
    }
    public int getId() {
        return id;
    }
}
