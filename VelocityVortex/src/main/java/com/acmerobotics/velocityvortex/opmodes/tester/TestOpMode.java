package com.acmerobotics.velocityvortex.opmodes.tester;

import com.acmerobotics.library.file.LogFile;
import com.acmerobotics.library.logging.MultipleLogger;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.opmodes.tester.DefaultTester;
import com.acmerobotics.velocityvortex.opmodes.tester.DistanceSensorTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRDeviceInterfaceModuleTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRMotorControllerTester;
import com.acmerobotics.velocityvortex.opmodes.tester.MRServoControllerTester;
import com.acmerobotics.velocityvortex.opmodes.tester.TCS34725ColorSensorTester;
import com.acmerobotics.velocityvortex.opmodes.tester.Tester;
import com.acmerobotics.velocityvortex.opmodes.tester.TesterBinding;
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
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TeleOp(name = "Tester")
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
    private ElapsedTime loopTimer;
    private MultipleLogger logger;
    private LogFile logFile;

    @Override
    public void init() {
//        String logName = "tester_timer_" + System.currentTimeMillis() + ".txt";
//        logFile = new LogFile(logName);
        logger = new MultipleLogger();
//        logger.addLogger(logFile);

        loopTimer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        bindings = new ArrayList<>();
        makeBindings();

        stickyGamepad1 = new StickyGamepad(gamepad1);

//        logger.msg(logName);
        logger.msg("devices:");

        testers = new ArrayList<>();
        for (HardwareDevice device : hardwareMap.getAll(HardwareDevice.class)) {
            names = hardwareMap.getNamesOf(device);
            String name = names.size() > 0 ? names.iterator().next() : "";
            logger.msg("'%s': %s", name, device.getConnectionInfo());
            for (TesterBinding binding : bindings) {
                if (binding.matches(name, device)) {
                    if (binding.isValid())
                        testers.add(binding.newTester(name, device));
                    break;
                }
            }
        }

        logger.msg("loop_times:");

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
        double loopTime = loopTimer.milliseconds();

        logger.msg("%6.3f ms", loopTime);

        loopTimer.reset();

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
                    logger.msg("'%s' changed state to %b", currentTester.getName(), currentTester.isEnabled());
                }

                if (stickyGamepad1.dpad_right && currentTester.isEnabled()) {
                    mode = Mode.DETAIL;
                    logger.msg("'%s' selected", currentTester.getName());
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
                    logger.msg("'%s' deselected", currentTester.getName());
                }

                telemetry.addData("name", currentTester.getName());
                telemetry.addData("type", currentTester.getType());
                if (currentTester.getId().length() > 0)
                    telemetry.addData("id", currentTester.getId());
                currentTester.loop(gamepad1, stickyGamepad1, telemetry, logger);
        }
    }

    @Override
    public void stop() {
        try {
            logFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
