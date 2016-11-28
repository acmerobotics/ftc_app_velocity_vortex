package com.acmerobotics.velocityvortex.opmodes;

import com.qualcomm.hardware.ArmableUsbDevice;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TeleOp(name="Test Op Mode")
public class TestOpMode extends OpMode {

    public enum Mode {
        MAIN,
        DETAIL
    }

    private List<HardwareDevice> devices;
    private HardwareDevice currentDevice;
    private int currentDeviceIndex;
    private int currentDevicePort;
    private double servoPosition;
    private Mode mode;
    private StickyGamepad stickyGamepad1;
    private Set<String> names;

    @Override
    public void init() {
        stickyGamepad1 = new StickyGamepad(gamepad1);
        devices = new ArrayList<>();
        for (HardwareDevice device : hardwareMap.getAll(HardwareDevice.class)) {
            if (device instanceof ArmableUsbDevice) {
                devices.add(device);
            }
        }
        mode = Mode.MAIN;
    }

    @Override
    public void loop() {
        telemetry.clearAll();
        stickyGamepad1.update();
        switch (mode) {
            case MAIN:
                telemetry.addData("INFO", "use the dpad to select the right controller");

                if (stickyGamepad1.dpad_down && currentDeviceIndex != devices.size() - 1) {
                    currentDeviceIndex += 1;
                }
                if (stickyGamepad1.dpad_up && currentDeviceIndex != 0) {
                    currentDeviceIndex -= 1;
                }
                if (stickyGamepad1.dpad_right) {
                    currentDevice = devices.get(currentDeviceIndex);
                    mode = Mode.DETAIL;
                }

                for (int i = 0; i < devices.size(); i++) {
                    HardwareDevice device = devices.get(i);
                    if (i == currentDeviceIndex) {
                        telemetry.addData("[>]", getDeviceString(device));
                    } else {
                        telemetry.addData("[ ]", getDeviceString(device));
                    }
                }
                break;
            case DETAIL:
                telemetry.addData("INFO", "press left on the dpad to go back");

                if (stickyGamepad1.dpad_left) {
                    currentDevicePort = 0;
                    mode = Mode.MAIN;
                }

                telemetry.addData("device", getDeviceString(currentDevice));
                telemetry.addData("type", currentDevice.getDeviceName());
                if (currentDevice instanceof DcMotorController) {
                    if (stickyGamepad1.a || stickyGamepad1.b) {
                        currentDevicePort = 1 - currentDevicePort;
                    }
                    DcMotorController controller = (DcMotorController) currentDevice;
                    double power = -gamepad1.left_stick_y;
                    telemetry.addData("motor", currentDevicePort + 1);
                    telemetry.addData("power", power);
                    controller.setMotorPower(currentDevicePort + 1, power);
                    telemetry.addData("encoder", controller.getMotorCurrentPosition(currentDevicePort + 1));
                    telemetry.addData("voltage", Math.round(100.0 * ((VoltageSensor) controller).getVoltage()) / 100.0 + " V");
                } else if (currentDevice instanceof ServoController) {
                    ServoController controller = (ServoController) currentDevice;
                    if (stickyGamepad1.a) {
                        currentDevicePort = (currentDevicePort + 1) % 6;
                    }
                    if (stickyGamepad1.b) {
                        currentDevicePort = (currentDevicePort + 5) % 6;
                    }
                    if (stickyGamepad1.y) {
                        servoPosition += 0.025;
                    }
                    if (stickyGamepad1.x) {
                        servoPosition -= 0.025;
                    }
                    if (gamepad1.left_stick_y != 0) {
                        servoPosition = 0.5 * (1 - gamepad1.left_stick_y);
                    }
                    servoPosition = Range.clip(servoPosition, 0, 1);
                    telemetry.addData("servo", currentDevicePort + 1);
                    telemetry.addData("position", servoPosition);
                    controller.setServoPosition(currentDevicePort + 1, servoPosition);
                } else {
                    telemetry.addData("ERROR", "sorry, devices of this type are not supported");
                }
                break;
        }
    }

    private String getDeviceString(HardwareDevice device) {
        String deviceString;
        names = hardwareMap.getNamesOf(device);
        if (names.isEmpty()) {
            deviceString = device.getDeviceName();
            deviceString = deviceString.replaceAll("Modern Robotics", "MR");
        } else {
            deviceString = names.iterator().next();
        }
        if (device instanceof ArmableUsbDevice) {
            deviceString += " [" + ((ArmableUsbDevice) device).getSerialNumber().toString() + "]";
        }
        return deviceString;
    }
}
