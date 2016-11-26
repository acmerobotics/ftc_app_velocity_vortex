package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="TurnTest")
public class TurnTest extends LinearOpMode {

    BNO055IMU imu;
    EnhancedMecanumDrive drive;

    @Override
    public void runOpMode() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap), imu);

        waitForStart();

        long start = System.currentTimeMillis();
        drive.turnSync(90, 0.5);
//        double feedback = 0;
//        while (opModeIsActive() && (Math.abs(drive.getHeadingError()) > 1 || Math.abs(feedback) > 0.0025)) {
//            feedback = drive.update();
//            telemetry.addData("feedback", feedback);
//            drive.log(telemetry);
//            telemetry.update();
//        }
//        drive.stop();
        double time = (System.currentTimeMillis() - start) / 1000.0;

        while (opModeIsActive()) {
            telemetry.addData("time", time + "s");
            try {
                telemetry.update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
