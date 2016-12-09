package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by kelly on 12/6/2016.
 *
 */

public class Launcher {

    private double velocity;
    private double velocityStep = .1;

    private long rampTime = 1000;
    private boolean ramping;
    private double targetVelocity;
    private double initialVelocity;
    private long startTime;

    private DcMotor right, left, elevation;
    private Servo gate, trigger;

    private double  gateOpenPos = 0, //todo the servo positions
                    gateClosePos = 0,
                    triggerUpPos = 0,
                    triggerDownPos = 0;

    private boolean gateIsOpen, triggerIsUp;

    public Launcher (HardwareMap hardwareMap) {
        right = hardwareMap.dcMotor.get("launcherRight");
        left = hardwareMap.dcMotor.get ("launcherLeft");
        elevation = hardwareMap.dcMotor.get ("launcherElevation");

        left.setDirection(DcMotorSimple.Direction.REVERSE);
        right.setDirection (DcMotorSimple.Direction.FORWARD);

        setVelocity (0);

        trigger = hardwareMap.servo.get("trigger");
        gate = hardwareMap.servo.get("gate");

        triggerDown();
        gateClose();

    }

    /**
     * set the velocity of the launcher directly
     * @param velocity between 0 and one
     */
    public void setVelocity(double velocity) {
        velocity = velocity > 1 ? 1 : velocity;
        velocity = velocity < 0 ? 0 : velocity;
        this.velocity = velocity;
        right.setPower (velocity);
        left.setPower (velocity);
        ramping = false;
    }

    /**
     * set a target velocity to be ramped to over 1 second
     * updateVelocity must be called each loop in order for the velocity to be changed incrementally
     * @param target between 0 and 1
     */
    public void setTargetVelocity (double target) {
        if (target == velocity) return;
        ramping = true;
        startTime = System.currentTimeMillis();
        targetVelocity = target;
        initialVelocity = velocity;
    }

    /**
     * update the velocity to the current value in the ramping
     * does nothing if not currently ramping
     */
    public void updateVelocity () {
        if (!ramping) return;

        long diff = System.currentTimeMillis() - startTime;
        velocity = ((diff/rampTime)*(targetVelocity - initialVelocity)) + initialVelocity; //the percentage through the ramp, times the distance to go, plus the initial value

        if (diff >= rampTime) {
            velocity = targetVelocity;
            ramping = false;
        }

        right.setPower (velocity);
        left.setPower (velocity);
    }

    public void toggleVelocity () {
        if (velocity == 0) {
            setTargetVelocity(1);
        }
        else setTargetVelocity (0);
    }

    public double getVelocity () {
        return velocity;
    }

    public void velocityUp () {
        setVelocity (velocity + velocityStep);
    }

    public void velocityDown () {
        setVelocity (velocity - velocityStep);
    }

    public void stop() {
        setVelocity(0);
    }

    public void gateOpen () {
        gate.setPosition (gateOpenPos);
        gateIsOpen = true;
    }

    public void gateClose () {
        gate.setPosition(gateClosePos);
        gateIsOpen = false;
    }

    public void gateToggle () {
        if (gateIsOpen) gateClose();
        else gateOpen();
    }

    public void triggerUp () {
        trigger.setPosition(triggerUpPos);
        triggerIsUp = true;
    }

    public void triggerDown () {
        trigger.setPosition(triggerDownPos);
        triggerIsUp = false;
    }

    public void triggerToggle () {
        if (triggerIsUp) triggerDown();
        else triggerDown();
    }

}
