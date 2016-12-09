package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by kelly on 12/7/2016.
 */

public class CollectorHardware {

    private double velocity;
    private DcMotor motor;

    private boolean running;

    public CollectorHardware (HardwareMap hardwareMap) {
        motor = hardwareMap.dcMotor.get("collector");
    }

    public void setVelocity (double velocity) {
        this.velocity = velocity;
        running = velocity > 0 ? true : false;
        motor.setPower (velocity);
    }

    public void run() {
        velocity = 1;
        running = true;
        motor.setPower (velocity);
    }

    public void stop () {
        velocity = 0;
        running = false;
        motor.setPower(0);
    }

    public void toggle () {
        if (running) stop ();
        else run ();
    }
}
