package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

/**
 * @author Kelly Muir
 */

@TeleOp(name = "Color Tester")
public class ColorTester extends OpMode {

    TCS34725ColorSensor device;
    DataFile file;

    public void init() {
        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("color");
        device = new TCS34725ColorSensor(i2cDevice, true);
        device.initialize();
        file = new DataFile("color.csv");
    }

    public void loop() {

        double red = device.red();
        double green = device.green();
        double blue = device.blue();
        double alpha = device.alpha();

        double blueRed = blue / red;


        BeaconColors color = BeaconColors.UNKNOWN;
        if (blueRed > 2.5) color = BeaconColors.BLUE;
        else if (blueRed < .8) color = BeaconColors.RED;

        file.write(blueRed + ", " + red + ", " + blue + ", " + green + ", " + alpha);

        telemetry.addData("color", color.getName());

        telemetry.addData("blueRed", blueRed);
        telemetry.addData("red", red);
        telemetry.addData("blue", blue);
        telemetry.addData("alpha", alpha);
        telemetry.addData("green", green);

    }

    enum BeaconColors {
        RED {
            public String getName() {
                return "red";
            }
        },
        BLUE {
            public String getName() {
                return "blue";
            }
        },
        UNKNOWN {
            public String getName() {
                return "unknown";
            }
        };

        public abstract String getName();
    }
}
