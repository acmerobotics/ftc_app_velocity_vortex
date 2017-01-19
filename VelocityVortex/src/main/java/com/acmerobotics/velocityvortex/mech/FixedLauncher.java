package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * @author Ryan Brott
 */

public class FixedLauncher {

    public static final double TRIGGER_UP = 0.66;
    public static final double TRIGGER_DOWN = 1;

    private Servo trigger;
    private boolean triggered;
    private DcMotor left, right;
    private double leftPower, rightPower, trim;

    private double leftTarget, rightTarget;
    private long stopTime;
    private boolean ramping;

    public FixedLauncher(HardwareMap hardwareMap) {
        trigger = hardwareMap.servo.get("trigger");
        trigger.setPosition(TRIGGER_DOWN);

        left = hardwareMap.dcMotor.get("launcherLeft");
        right = hardwareMap.dcMotor.get("launcherRight");

        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void triggerUp() {
        if (!triggered) {
            trigger.setPosition(TRIGGER_UP);
            triggered = true;
        }
    }

    public void triggerDown() {
        if (triggered) {
            trigger.setPosition(TRIGGER_DOWN);
            triggered = false;
        }
    }

    public void triggerToggle() {
        if (triggered) {
            triggerDown();
        } else {
            triggerUp();
        }
    }

    public boolean isRunning() {
        return leftTarget != 0 || rightTarget != 0;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public double getTrim() {
        return trim;
    }

    public void setTrim(double trim) {
        this.trim = trim;
        setPower(leftPower, rightPower);
    }

    public void setPower(double power) {
        setPower(power, power);
    }

    public void setPower(double leftPower, double rightPower) {
        setPower(leftPower, rightPower, 0);
    }

    public void setPower(double leftPower, double rightPower, long rampTime) {
        if (leftPower == 0) {
            leftTarget = 0;
        } else {
            leftTarget = leftPower - trim;
        }
        if (rightPower == 0) {
            rightTarget = 0;
        } else {
            rightTarget = rightPower + trim;
        }

        if (rampTime == 0) {
            internalSetPower(leftTarget, rightTarget);
        } else {
            ramping = true;
            stopTime = System.currentTimeMillis() + rampTime;
            if (leftTarget == 0) {
                internalSetLeftPower(leftPower);
            }
            if (rightTarget == 0) {
                internalSetRightPower(rightPower);
            }
        }
    }

    public void update() {
        if (!ramping) return;
        long now = System.currentTimeMillis();
        if (now >= stopTime) {
            ramping = false;
            internalSetPower(leftTarget, rightTarget);
        } else {
            long dt = stopTime - now;
            if (leftTarget != 0) {
                internalSetLeftPower(leftPower + (leftTarget - leftPower) / dt);
            }
            if (rightTarget != 0) {
                internalSetRightPower(rightPower + (rightTarget - rightPower) / dt);
            }
        }
    }

    private void internalSetPower(double leftPower, double rightPower) {
        internalSetLeftPower(leftPower);
        internalSetRightPower(rightPower);
    }

    private void internalSetLeftPower(double power) {
        this.leftPower = Range.clip(power, -1, 1);
        left.setPower(leftPower);
    }

    private void internalSetRightPower(double power) {
        this.rightPower = Range.clip(power, -1, 1);
        right.setPower(rightPower);
    }

    public double getLeftPower() {
        return leftPower;
    }

    public double getRightPower() {
        return rightPower;
    }

}