package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Disabled
@Autonomous(name="Distance Test")
public class DistanceTest extends OpMode {

    private DcMotor[] motors;
    private double[] finishTimes;
    private double startTime;

    @Override
    public void init() {
        motors = new DcMotor[4];
        motors[0] = hardwareMap.dcMotor.get("left1");
        motors[1] = hardwareMap.dcMotor.get("right1");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = hardwareMap.dcMotor.get("right2");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = hardwareMap.dcMotor.get("left2");

        finishTimes = new double[4];

        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
    }

    @Override
    public void loop() {
        if (startTime == 0) {
            startTime = getRuntime();
        }
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            motor.setTargetPosition(50000);
            motor.setPower(0.5);
            if (motor.isBusy()) {
                telemetry.addData("motor" + i, motor.getCurrentPosition());
            } else {
                if (finishTimes[i] == 0) {
                    finishTimes[i] = getRuntime() - startTime;
                }
                telemetry.addData("motor" + i, finishTimes[i] + "s");
            }
        }
    }

}
