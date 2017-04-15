package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2d;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * @author Ryan Brott
 */

@TeleOp(name = "IMU Test", group="Test")
public class IMUTest extends OpMode {

    private static final String XYZ_FORMAT_STRING = "%5.2f,%5.2f,%5.2f";

    private DataFile recordFile;
    private String baseFileName;
    private int recordingNum;
    private boolean recording;

    protected MecanumDrive mecanumDrive;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    private BNO055IMU imu;
    private StickyGamepad stickyGamepad1;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        mecanumDrive = new MecanumDrive(hardwareMap, properties);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        baseFileName = "imu_record_" + System.currentTimeMillis() + "_";

        stickyGamepad1 = new StickyGamepad(gamepad1);
    }

    @Override
    public void init_loop() {
        telemetry.addData(">", "moveToLineAndWait");
    }

    @Override
    public void loop() {
        stickyGamepad1.update();

        if (stickyGamepad1.a) {
            if (recording) {
                recordFile.close();
                recordFile = null;
                recording = false;
            } else {
                recordingNum++;
                recordFile = new DataFile(baseFileName + recordingNum + ".csv");
                recordFile.write("orient_x,orient_y,orient_z,acc_x,acc_y,acc_z,vel_x,vel_y,vel_z");
                recording = true;
            }
        }

        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        double omega = -gamepad1.right_stick_x;
        mecanumDrive.setVelocity(new Vector2d(x, y), omega);

        Orientation o = imu.getAngularOrientation();
        Acceleration a = imu.getLinearAcceleration();
        AngularVelocity v = imu.getAngularVelocity();
        telemetry.addData(">", recording ? "recording IMU data..." : "not recording");
        telemetry.addData("orientation", String.format(XYZ_FORMAT_STRING, o.thirdAngle, o.secondAngle, -o.firstAngle));
        telemetry.addData("linear_acceleration", String.format(XYZ_FORMAT_STRING, a.xAccel, a.yAccel, a.zAccel));
        telemetry.addData("angular_velocity", String.format(XYZ_FORMAT_STRING, v.xRotationRate, v.yRotationRate, v.zRotationRate));

        if (recording) {
            recordFile.write(
                    o.thirdAngle + "," + o.secondAngle + "," + (-o.firstAngle) + ","
                            + a.xAccel + "," + a.yAccel + "," + a.zAccel + ","
                            + v.xRotationRate + "," + v.yRotationRate + "," + v.zRotationRate
            );
        }
    }

    @Override
    public void stop() {
        if (recording) recordFile.close();
    }
}
