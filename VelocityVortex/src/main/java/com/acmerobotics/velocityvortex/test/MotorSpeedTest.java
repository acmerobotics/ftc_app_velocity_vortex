package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * @author Ryan
 */

@Disabled
@Autonomous(name = "Motor Speed Test")
public class MotorSpeedTest extends LinearOpMode {

    private DcMotor motor;

    @Override
    public void runOpMode() throws InterruptedException {
        motor = hardwareMap.dcMotor.get("motor");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        motor.setTargetPosition(1000000);
        motor.setMaxSpeed(6000);

        waitForStart();

        motor.setPower(1);

        sleep(500);

        resetStartTime();

        int startPos = motor.getCurrentPosition();

        sleep(10000);

        int endPos = motor.getCurrentPosition();
        double cps = (endPos - startPos) / getRuntime();

        motor.setPower(0);

        while (opModeIsActive()) {
            telemetry.addData("cps", cps);
            telemetry.addData("max_speed", motor.getMaxSpeed());
            telemetry.addData("manufacturer", motor.getManufacturer());
            telemetry.update();
        }
    }
}
