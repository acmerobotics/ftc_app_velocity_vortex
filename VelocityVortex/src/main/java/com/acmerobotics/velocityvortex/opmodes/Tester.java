package com.acmerobotics.velocityvortex.opmodes;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareDevice;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public abstract class Tester<T extends HardwareDevice> {

    protected T device;
    protected String hardwareName;

    public Tester(String name, T device) {
        this.hardwareName = name;
        this.device = device;
    }

    public static int cycleForward(int i, int min, int max) {
        if (i == max) {
            return min;
        } else {
            return i + 1;
        }
    }

    public static int cycleBackward(int i, int min, int max) {
        if (i == min) {
            return max;
        } else {
            return i - 1;
        }
    }

    public String getName() {
        return hardwareName;
    }

    public abstract String getType();
    public abstract String getId();
    public abstract void loop(Gamepad gamepad, StickyGamepad stickyGamepad, Telemetry telemetry);
}
