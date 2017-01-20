package com.acmerobotics.velocityvortex.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 *
 */

public class TankDrive extends OpMode {

    private DcMotor[] motors;

    public void init () {
        motors = new DcMotor[4];
        motors[0] = hardwareMap.dcMotor.get("leftFront");
        motors[1] = hardwareMap.dcMotor.get("rightFront");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = hardwareMap.dcMotor.get("rightBack");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = hardwareMap.dcMotor.get("leftBack");

        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void loop () {
        double left = gamepad1.left_stick_y;
        double right = gamepad1.right_stick_x;
        motors[0].setPower(left);
        motors[2].setPower(left);
        motors[1].setPower(right);
        motors[3].setPower(right);
    }

}
