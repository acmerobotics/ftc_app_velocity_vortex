package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.opmodes.tester.DefaultTester;
import com.acmerobotics.velocityvortex.opmodes.tester.DistanceSensorTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRDeviceInterfaceModuleTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRMotorControllerTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRServoControllerTester;
import com.acmerobotics.velocityvortex.opmodes.tester.TCS34725ColorSensorTester;
import com.acmerobotics.velocityvortex.opmodes.tester.UltrasonicSensorTester;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDcMotorController;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDeviceInterfaceModule;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbServoController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TeleOp(name="Tester")
public class TestOpMode extends OpMode {

    public enum Mode {
        MAIN,
        DETAIL
    }

    private List<TesterBinding> bindings;
    private List<Tester> testers;
    private int testerIndex;
    private Mode mode;
    private StickyGamepad stickyGamepad1;
    private Set<String> names;
    private Tester currentTester;

    private long lastLoopTime;

    @Override
    public void init() {
        bindings = new ArrayList<>();
        makeBindings();

        stickyGamepad1 = new StickyGamepad(gamepad1);

        testers = new ArrayList<>();
        for (HardwareDevice device : hardwareMap.getAll(HardwareDevice.class)) {
            names = hardwareMap.getNamesOf(device);
            String name = names.size() > 0 ? names.iterator().next() : "";
            for (TesterBinding binding : bindings) {
                if (binding.matches(name, device)) {
                    if (binding.isValid())
                        testers.add(binding.newTester(name, device));
                    break;
                }
            }
        }

        mode = Mode.MAIN;
        testerIndex = 0;
    }

    public <T extends HardwareDevice> void bind(Class<T> deviceClass, Class<? extends Tester<T>> testerClass) {
        bind("", deviceClass, testerClass);
    }

    public <T extends HardwareDevice> void bind(String nameRegex, Class<T> deviceClass, Class<? extends Tester<T>> testerClass) {
        bindings.add(new TesterBinding(nameRegex, deviceClass, testerClass));
    }

    public void makeBindings() {
        bind(ModernRoboticsUsbDcMotorController.class, MRMotorControllerTester.class);
        bind(ModernRoboticsUsbServoController.class, MRServoControllerTester.class);
        bind(ModernRoboticsUsbDeviceInterfaceModule.class, MRDeviceInterfaceModuleTester.class);

        bind("amsColor", I2cDeviceSynch.class, TCS34725ColorSensorTester.class);

        bind(DistanceSensor.class, DistanceSensorTester.class);
        bind(UltrasonicSensor.class, UltrasonicSensorTester.class);

        bind(DcMotor.class, null);
        bind(Servo.class, null);

        bind(HardwareDevice.class, DefaultTester.class);
    }

    @Override
    public void loop() {
        long currentLoopTime = System.currentTimeMillis();
        long loopTime;
        if (lastLoopTime == 0) {
            loopTime = 0;
        } else {
            loopTime = currentLoopTime - lastLoopTime;
        }

        telemetry.clearAll();
        stickyGamepad1.update();
        switch (mode) {
            case MAIN:
                if (stickyGamepad1.dpad_down) {
                    testerIndex = Tester.cycleForward(testerIndex, 0, testers.size() - 1);
                }
                if (stickyGamepad1.dpad_up) {
                    testerIndex = Tester.cycleBackward(testerIndex, 0, testers.size() - 1);
                }

                currentTester = testers.get(testerIndex);

                if (stickyGamepad1.x) {
                    if (currentTester.isEnabled()) {
                        currentTester.disable();
                    } else {
                        currentTester.enable();
                    }
                }

                if (stickyGamepad1.dpad_right && currentTester.isEnabled()) {
                    mode = Mode.DETAIL;
                } else {
                    for (int i = 0; i < testers.size(); i++) {
                        Tester tester = testers.get(i);
                        String s = tester.getName() + " (" + tester.getType() + ")";
                        if (i == testerIndex) {
                            telemetry.addData("[>]", s);
                        } else if (!tester.isEnabled()) {
                            telemetry.addData("[X]", s);
                        } else {
                            telemetry.addData("[ ]", s);
                        }
                    }
                    telemetry.addData("loop_time", loopTime + "ms");
                }
                break;
            case DETAIL:
                if (stickyGamepad1.dpad_left) {
                    mode = Mode.MAIN;
                }

                telemetry.addData("name", currentTester.getName());
                telemetry.addData("type", currentTester.getType());
                if (currentTester.getId() != "") telemetry.addData("id", currentTester.getId());
                currentTester.loop(gamepad1, stickyGamepad1, telemetry);
        }

        lastLoopTime = System.currentTimeMillis();
    }
}
