package com.acmerobotics.velocityvortex.opmodes.tester;

import com.acmerobotics.library.logging.Logger;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class MRDeviceInterfaceModuleTester extends Tester<ModernRoboticsUsbDeviceInterfaceModule> {

    private boolean blueLed, redLed;

    public MRDeviceInterfaceModuleTester(String name, ModernRoboticsUsbDeviceInterfaceModule device) {
        super(name, device);
    }

    @Override
    public String getType() {
        return "MR Device Interface Module";
    }

    @Override
    public String getId() {
        return device.getSerialNumber().toString();
    }

    @Override
    public void loop(Gamepad gamepad, StickyGamepad stickyGamepad, Telemetry telemetry, Logger logger) {
        blueLed = device.getLEDState(0);
        redLed = device.getLEDState(1);
        if (stickyGamepad.x) {
            blueLed = !blueLed;
            logger.msg("dim: blue led state change: %b", blueLed);
        }
        device.setLED(0, blueLed);
        if (stickyGamepad.y) {
            redLed = !redLed;
            logger.msg("dim: red led state change: %b", redLed);
        }
        device.setLED(1, redLed);

        telemetry.addData("blue LED (X)", blueLed);
        telemetry.addData("red LED (Y)", redLed);
    }
}
