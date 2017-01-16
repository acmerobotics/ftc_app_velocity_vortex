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

    private double targetVelocity;
    private long lastTime;
    private boolean ramping;

    private double trim = 0; // positive moves to the right
    private double maxVelocity = 1;

    private DcMotor right, left, elevation;
    private Servo trigger;

    private double  gateOpenPos = 0, //todo the servo positions
                    gateClosePos = 0,
                    triggerUpPos = 0.66,
                    triggerDownPos = 1;

    private boolean gateIsOpen, triggerIsUp;

    public Launcher(HardwareMap hardwareMap) {
        right = hardwareMap.dcMotor.get("launcherRight");
        left = hardwareMap.dcMotor.get("launcherLeft");
        elevation = hardwareMap.dcMotor.get("launcherElevation");

        left.setDirection(DcMotorSimple.Direction.REVERSE);
        right.setDirection(DcMotorSimple.Direction.REVERSE);

        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        elevation.setDirection(DcMotorSimple.Direction.REVERSE);
        elevation.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        elevation.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elevation.setPower(0);

        setVelocity(0);

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
        if (ramping) return;
        this.velocity = Range.clip(velocity, 0, maxVelocity);
        if (velocity < .2) velocity = 0;
        right.setPower(Range.clip((velocity - trim), 0, maxVelocity));
        left.setPower(Range.clip((velocity + trim), 0, maxVelocity));
    }

    public void stop() {
        right.setPower(0);
        left.setPower(0);
    }

    public void setMaxVelocity(double max) {
        maxVelocity = Range.clip(max, 0, 1);
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public void maxVelocityUp() {
        setMaxVelocity(maxVelocity + .05);
    }

    public void maxVelocityDown() {
        setMaxVelocity(maxVelocity - .05);
    }

    public void setTargetVelocity(double target) {
        targetVelocity = Range.clip(target, 0, 1);
        lastTime = System.currentTimeMillis();

        ramping = true;
    }

    public void setTrim(double trim) {
        this.trim = Range.clip(trim, -.5, .5);
    }

    public double getTrim() {
        return trim;
    }

    public void trimUp() {
        setTrim(trim + .05);
    }

    public void trimDown() {
        setTrim(trim - .05);
    }

    public void toggleVelocity() {
        if (velocity == 0) {
            setVelocity(1);
        }
        else setVelocity(0);
    }

    public void updateVelocity() {
        if (!ramping) return;
        long diff = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        double percentChange = diff/15;
        double accelerationLeft = targetVelocity - velocity;
        if (Math.abs(accelerationLeft)*100 < percentChange) {
            velocity = targetVelocity;
            ramping = false;
        }
        else  {
            double acceleration = accelerationLeft>0 ? .01 : -.01;
            setVelocity(velocity + acceleration);
        }
    }

    public double getVelocity() {
        return velocity;
    }

    public void velocityUp() {
        setVelocity(velocity + velocityStep);
    }

    public void velocityDown() {
        setVelocity(velocity - velocityStep);
    }

    /*public void gateOpen() {
        gate.setPosition(gateOpenPos);
        gateIsOpen = true;
    }

    public void gateClose() {
        gate.setPosition(gateClosePos);
        gateIsOpen = false;
    }

    public void gateToggle() {
        if (gateIsOpen) gateClose();
        else gateOpen();
    }*/

    public void triggerUp() {
        trigger.setPosition(triggerUpPos);
        triggerIsUp = true;
    }

    public void triggerDown() {
        trigger.setPosition(triggerDownPos);
        triggerIsUp = false;
    }

    public void triggerToggle() {
        if (triggerIsUp) triggerDown();
        else triggerDown();
    }

    public void setElevationVelocity(double velocity) {
        elevation.setPower(Range.clip(velocity, -1, 1));
    }

}
