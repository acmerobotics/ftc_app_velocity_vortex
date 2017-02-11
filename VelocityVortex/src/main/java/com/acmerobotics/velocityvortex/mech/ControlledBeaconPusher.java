package com.acmerobotics.velocityvortex.mech;

import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by kelly on 2/7/2017.
 */

public class ControlledBeaconPusher {
    public final double P_COEF = 0;
    public final double I_COEF = 0;
    public final double D_COEF = 0;

    private CRServo servo;
    private LinearPot sensor;
    private DistanceUnit unit;
    private double length;
    private double target;
    private PIDController pid;

    public ControlledBeaconPusher (HardwareMap map, double length, DistanceUnit unit) {
        this.unit = unit;
        this.length = length;

        ServoController servoController = map.servoController.get("servo");
        servo = new CRServoImpl(servoController, 3);

        AnalogInputController dim = map.deviceInterfaceModule.get("dim");
        sensor = new LinearPot(new AnalogInput(dim, 2), length, unit);

        pid = new PIDController(new DifferentialControlLoopCoefficients(P_COEF, I_COEF, D_COEF));
    }

    public void update () {
        double pos = sensor.getDistance(unit);
        double error = pos - target;
        double correction = pid.update(error);
        setPower(correction);
    }

    private void setPower (double power) {
        servo.setPower(Range.clip(power, -1, 1));
    }

    public void setPosition (double pos) {
        target = Range.clip(pos, 0, length);
    }

    public void extend () {
        setPosition(length);
    }

    public void retract () {
        setPosition(0);
    }

    public DistanceUnit getUnit() {
        return unit;
    }

    public double getLength() {
        return length;
    }
}
