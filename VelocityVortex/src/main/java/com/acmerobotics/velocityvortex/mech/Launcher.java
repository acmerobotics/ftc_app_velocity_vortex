package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * Created by kelly on 12/6/2016.
 *
 */

public class Launcher {

    private double velocity;
    private double velocityStep = .1;

    private long rampTime = 100;
    private double rampIncerment = .1;
    private double accelerationLeft;
    private long lastTime;

    private DcMotor right, left, elevation;
    private Servo trigger;

    private double  gateOpenPos = 0, //todo the servo positions
                    gateClosePos = 0,
                    triggerUpPos = .45,
                    triggerDownPos = 0;

    private boolean gateIsOpen, triggerIsUp;

    public Launcher (HardwareMap hardwareMap) {
        right = hardwareMap.dcMotor.get("launcherRight");
        left = hardwareMap.dcMotor.get ("launcherLeft");
        elevation = hardwareMap.dcMotor.get ("launcherElevation");

        left.setDirection(DcMotorSimple.Direction.REVERSE);
        right.setDirection (DcMotorSimple.Direction.REVERSE);

        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        elevation.setDirection (DcMotorSimple.Direction.REVERSE); //todo motor elevation motor direction - forward should be up
        elevation.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        elevation.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elevation.setPower (0);

        setVelocity (0);

        trigger = hardwareMap.servo.get("trigger");
        //gate = hardwareMap.servo.get("gate");

        triggerDown();
        //gateClose();

    }

    /**
     * set the velocity of the launcher directly
     * @param velocity between 0 and one
     */
    public void setVelocity(double velocity) {
        this.velocity = Range.clip(velocity, 0, 1);
        if (velocity < .2) velocity = 0;
        right.setPower (velocity);
        left.setPower (velocity);
    }

    public void setTargetVelocity (double target) {
        accelerationLeft = target - velocity;
        lastTime = System.currentTimeMillis();
    }

    public void toggleVelocity () {
        if (velocity == 0) {
            setVelocity(1);
        }
        else setVelocity (0);
    }

    public void updateVelocity () {
        long diff = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();
        if (diff < rampTime) {
            return;
        }
        if (accelerationLeft < velocityStep) setVelocity(velocity + accelerationLeft);
        else {
            if (accelerationLeft >= 0){
                setVelocity(velocity + velocityStep);
                accelerationLeft -= velocityStep;
            }else {
                setVelocity (velocity - velocityStep);
                accelerationLeft += velocityStep;
            }
        }

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

    /*public void gateOpen () {
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
    }*/

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

    public void setElevationVelocity (double velocity) {
        elevation.setPower(Range.clip(velocity, -1, 1));
    }

}
