package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "Max Sonar Test", group="Test")
public class MaxSonarTest extends OpMode {

    private MaxSonarEZ1UltrasonicSensor sensor;
    private AnalogInput input;
    private static final double[] exps = {1, 0.15, 0.1, 0.075, 0.05};
    private ExponentialSmoother[] smoothers;
    private DataFile file;

    @Override
    public void init() {
        DeviceInterfaceModule dim = hardwareMap.deviceInterfaceModule.get("dim");
        input = new AnalogInput(dim, 0);
        sensor = new MaxSonarEZ1UltrasonicSensor(input);

        file = new DataFile("distance_sensor_smooth_" + System.currentTimeMillis() + ".csv");

        smoothers = new ExponentialSmoother[exps.length];
        for (int i = 0; i < exps.length; i++) {
            smoothers[i] = new ExponentialSmoother(exps[i]);
            file.write("smoothed_" + exps[i] + ",", false);
        }
        file.write();
    }

    @Override
    public void loop() {
        double distance = sensor.getDistance(DistanceUnit.INCH);
        for (int i = 0; i < exps.length; i++) {
            file.write(smoothers[i].update(distance) + ",", false);
        }
        file.write();
        telemetry.addData("raw_distance", String.format("%6.3f in", distance));
        telemetry.addData("voltage", input.getVoltage());
        telemetry.addData("max_voltage", input.getMaxVoltage());
    }

    @Override
    public void stop() {
        file.close();
    }
}
