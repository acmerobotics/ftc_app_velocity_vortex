package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Disabled
@Autonomous(name="Horizontal Drive")
public class HorizontalDriveTest extends OpMode {

    private DcMotor[] motors;

    @Override
    public void init() {
        motors = new DcMotor[4];
        motors[0] = hardwareMap.dcMotor.get("left1");
        motors[1] = hardwareMap.dcMotor.get("right1");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = hardwareMap.dcMotor.get("right2");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = hardwareMap.dcMotor.get("left2");
    }

    @Override
    public void loop() {
        motors[0].setPower(1);
        motors[1].setPower(-1);
        motors[2].setPower(1);
        motors[3].setPower(-1);
    }
}
