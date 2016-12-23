package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    public static class Configuration {
        public double[] wheelRadii;
        public double offX = 1, offY = 1;

        public static Configuration createFixedRadius(double radius) {
            Configuration config = new Configuration();
            config.wheelRadii = new double[] { radius, radius, radius, radius };
            return config;
        }
    }

    public static final Vector2D INERT_VELOCITY = new Vector2D(0, 0);

    private Configuration configuration;

    private DcMotor[] motors;
    private Vector2D[] rollerDirs;
    private Vector2D[] rotDirs;

    private int[] offsets;

    public MecanumDrive(HardwareMap map, Configuration config) {
        configuration = config;

        motors = new DcMotor[4];
        motors[0] = map.dcMotor.get("leftFront");
        motors[1] = map.dcMotor.get("rightFront");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = map.dcMotor.get("rightBack");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = map.dcMotor.get("leftBack");

        rollerDirs = new Vector2D[4];
        rollerDirs[0] = new Vector2D(1, 1).normalize();
        rollerDirs[1] = new Vector2D(-1, 1).normalize();
        rollerDirs[2] = rollerDirs[0];
        rollerDirs[3] = rollerDirs[1];

        double offX = config.offX, offY = config.offY;
        rotDirs = new Vector2D[4];
        rotDirs[0] = new Vector2D(-offY, -offX).normalize();
        rotDirs[1] = new Vector2D(-offY, offX).normalize();
        rotDirs[2] = new Vector2D(offY, offX).normalize();
        rotDirs[3] = new Vector2D(offY, -offX).normalize();

        resetEncoders();
    }

    /**
     * Sets the angular velocity of the mecanum drive system. This includes both the translational
     * component and the angular component.
     * @param v translational velocity
     * @param angularSpeed angular speed
     */
    public void setVelocity(Vector2D v, double angularSpeed) {
        angularSpeed = Range.clip(angularSpeed, -1, 1);
        double speed;
        if (v.norm() > 1) {
            speed = 1;
        } else {
            speed = v.norm();
        }

        if (Math.abs(speed) > 0.0000001) {
            v = v.copy().normalize();
        }

        for (int i = 0; i < 4; i++) {
            Vector2D angularVelocity = rotDirs[i].copy().multiply(angularSpeed);
            Vector2D transVelocity = v.copy().multiply(Math.min(1 - angularSpeed, speed));
            transVelocity.add(angularVelocity);
            motors[i].setPower(transVelocity.dot(rollerDirs[i]));
        }

    }

    /**
     * Stop the motors.
     */
    public void stop() {
        for (DcMotor motor : motors) {
            motor.setPower(0);
        }
    }

    public void log(Telemetry telemetry) {
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            telemetry.addData("motor" + i, motor.getPower());
        }
    }

    public DcMotor[] getMotors() {
        return motors;
    }

    /**
     * Reset the encoder positions. This will reset the value of {@link #getPositions()}.
     */
    public void resetEncoders() {
        offsets = getRawPositions();
    }

    /**
     * Get the adjusted positions for each motor
     * @return an array of positions
     */
    public int[] getPositions() {
        int[] pos = getRawPositions();
        for (int i = 0; i < pos.length; i++) {
            pos[i] -= offsets[i];
        }
        return pos;
    }

    /**
     * Get the raw encoder positions for each motor
     * @return an array of the positions
     */
    public int[] getRawPositions() {
        int[] raw = new int[motors.length];
        for (int i = 0; i < motors.length; i++) {
            raw[i] = motors[i].getCurrentPosition();
        }
        return raw;
    }

    public int getMeanPosition() {
        int sum = 0;
        for (int pos : getPositions()) {
            sum += pos;
        }
        return sum / motors.length;
    }

    public void move(double inches, double speed) {
        DcMotor.RunMode[] prevModes = new DcMotor.RunMode[motors.length];
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            prevModes[i] = motor.getMode();
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            int ticks = (int) Math.round(inches / (2 * Math.PI * configuration.wheelRadii[i]));
            motor.setTargetPosition(motor.getCurrentPosition() + ticks);
            motor.setPower(speed);
        }
        boolean done = false;
        while (!done) {
            for (DcMotor motor : motors) {
                if (!motor.isBusy()) done = true;
            }
            Thread.yield();
        }
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            motor.setMode(prevModes[i]);
            motor.setPower(0);
        }
    }

}
