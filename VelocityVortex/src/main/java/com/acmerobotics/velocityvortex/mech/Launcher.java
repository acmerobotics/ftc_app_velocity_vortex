package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by kelly on 12/6/2016.
 */

public class Launcher {

    private double velocity;

    private DcMotor right, left;

    public Launcher (HardwareMap hardwareMap) {
        right = hardwareMap.dcMotor.get("launcherRight");
        left = hardwareMap.dcMotor.get ("launcherLeft");

        left.setDirection(DcMotorSimple.Direction.REVERSE);
        right.setDirection (DcMotorSimple.Direction.FORWARD);
    }

    public void setVelocity(double velocity) {
        velocity = velocity > 1 ? 1 : velocity;
        velocity = velocity < 0 ? 0 : velocity;
        this.velocity = velocity;
        update();
    }

    public double getVelocity () {
        return velocity;
    }

    public void velocityUp () {
        setVelocity (velocity +.1);
    }

    public void velocityDown () {
        setVelocity (velocity - .1);
    }

    public void stop() {
        setVelocity(0);
    }

    public void update () {
        right.setPower (velocity);
        left.setPower (velocity);
    }


}
