package org.firstinspires.ftc.teamcode.hardware.drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.hardware.HardwareInterface;

/**
 * Created by Admin on 9/21/2015.
 */
public class DriveHardware extends HardwareInterface {

    public static final double SENSITIVITY = 1.5;
    //public static final double RAMP_TIME = 0.05;
    public static final double MAX_SPEED = 0.9;
    private DcMotor[] leftMotors = new DcMotor[2], rightMotors = new DcMotor[2];
    private HardwareMap hardwareMap;
    private OpMode opMode;
//    private double actualLeft, actualRight, targetLeft, targetRight;
//    private double leftRate, rightRate;

    @Override
    public void init(OpMode mode) {
        opMode = mode;
        hardwareMap = mode.hardwareMap;
        leftMotors[0] = this.hardwareMap.dcMotor.get("left1");
        leftMotors[1] = this.hardwareMap.dcMotor.get("left2");
        rightMotors[0] = this.hardwareMap.dcMotor.get("right1");
        rightMotors[1] = this.hardwareMap.dcMotor.get("right2");
    }

    @Override
    public String getStatusString() {
        return "left: " + leftMotors[0].getPower() + "/" + leftMotors[1].getPower() + "  right: " + rightMotors[0].getPower() + "/" + rightMotors[1].getPower();
    }

    public void setMotorSpeeds(double leftSpeed, double rightSpeed) {
//        targetLeft = -leftSpeed;
//        targetRight = rightSpeed;
//        leftRate = (targetLeft - actualLeft) / RAMP_TIME;
//        rightRate = (targetRight - actualRight) / RAMP_TIME;
//        actualLeft = -leftSpeed;
//        actualRight = rightSpeed;
        this.rightMotors[0].setDirection(DcMotor.Direction.REVERSE);
        this.rightMotors[1].setDirection(DcMotor.Direction.REVERSE);
        this.leftMotors[0].setPower(Range.clip(leftSpeed, -1, 1));
        this.leftMotors[1].setPower(Range.clip(leftSpeed, -1, 1));
        this.rightMotors[0].setPower(Range.clip(rightSpeed, -1, 1));
        this.rightMotors[1].setPower(Range.clip(rightSpeed, -1, 1));
    }

    public double mapMotorSpeed(double speed) {
        double newSpeed = MAX_SPEED * Math.pow(Math.abs(speed), SENSITIVITY);
        return speed > 0 ? newSpeed : -newSpeed;
    }

    public void setMappedMotorSpeeds(double leftSpeed, double rightSpeed) {
        setMotorSpeeds(mapMotorSpeed(leftSpeed), mapMotorSpeed(rightSpeed));
    }

    public void stopMotors() {
        setMotorSpeeds(0, 0);
    }

    public boolean movingLeft() {
        return leftMotors[0].getPower() > rightMotors[0].getPower();
    }

    public boolean movingRight() {
        return !movingLeft();
    }
}