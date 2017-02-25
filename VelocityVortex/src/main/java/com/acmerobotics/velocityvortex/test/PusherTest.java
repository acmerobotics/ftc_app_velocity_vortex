package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by ACME Robotics on 2/24/2017.
 */

@TeleOp(name="Pusher Test", group="Test")
public class PusherTest extends OpMode {

    private BeaconPusher pusher;
    private DcMotor motor;

    @Override
    public void init() {
        pusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));
        motor = hardwareMap.dcMotor.get("pusher");
    }

    @Override
    public void loop() {
        telemetry.addData("pos", pusher.getCurrentPosition());

        motor.setPower(gamepad1.left_stick_y);
    }
}
