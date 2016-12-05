package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Disabled
@Autonomous(name="Speed Control Test")
public class SpeedControlTest extends LinearOpMode {

    private DcMotor[] motors;
    
    @Override
    public void runOpMode() {
        motors = new DcMotor[4];
        motors[0] = hardwareMap.dcMotor.get("left1");
        motors[1] = hardwareMap.dcMotor.get("right1");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = hardwareMap.dcMotor.get("right2");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = hardwareMap.dcMotor.get("left2");

        waitForStart();

        double start = getRuntime();
        while (opModeIsActive() && (getRuntime() - start) < 20) {
            for (int i = 0; i < 4; i++) {
                DcMotor motor = motors[i];
                motor.setPower(1);
            }
        }

        int[] pos = new int[4];
        for (int i = 0; i < 4; i++) {
            pos[i] = motors[i].getCurrentPosition();
            motors[i].setPower(0);
        }

        while (opModeIsActive()) {
            for (int i = 0; i < 4; i++) {
                telemetry.addData("motor" + i, pos[i]);
                telemetry.update();
            }
        }
    }
}
