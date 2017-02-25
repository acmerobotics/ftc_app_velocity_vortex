package com.acmerobotics.velocityvortex.opmodes.tuner;

import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by ACME Robotics on 2/22/2017.
 */

@TeleOp(name="Pusher PID Tuner", group="PID Tuner")
public class PusherPIDTuner extends Tuner {

    private BeaconPusher pusher;

    private StickyGamepad stickyGamepad1;

    @Override
    public void init() {
        super.init();

        stickyGamepad1 = new StickyGamepad(gamepad1);

        pusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));
    }

    @Override
    public void loop() {
        super.loop();

        stickyGamepad1.update();

        if (stickyGamepad1.right_bumper) {
            pusher.setTargetPosition(3);
        }
        if (stickyGamepad1.left_bumper) {
            pusher.setTargetPosition(1);
        }

        pusher.update();

        telemetry.addData("pusher_pos", pusher.getCurrentPosition());
    }

    @Override
    protected PIDController getController() {
        return pusher.getController();
    }
}
