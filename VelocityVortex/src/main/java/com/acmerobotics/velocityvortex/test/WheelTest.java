package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Wheel Test")
public class WheelTest extends OpMode {

    private DcMotor[] wheels;
    private int wheelIndex;
    private boolean buttonDown;

    @Override
    public void init() {
        wheels = new DcMotor[4];
        wheels[0] = hardwareMap.dcMotor.get("left1");
        wheels[1] = hardwareMap.dcMotor.get("right1");
        wheels[2] = hardwareMap.dcMotor.get("right2");
        wheels[3] = hardwareMap.dcMotor.get("left2");

        wheelIndex = 0;
        buttonDown = false;
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            if (!buttonDown) {
                wheelIndex = (wheelIndex + 1) % 4;
                buttonDown = true;
            }
        } else {
            buttonDown = false;
        }
        double speed = gamepad1.left_stick_y;
        telemetry.addData("motor", wheelIndex);
        telemetry.addData("speed", speed);
        wheels[wheelIndex].setPower(speed);
    }
}
