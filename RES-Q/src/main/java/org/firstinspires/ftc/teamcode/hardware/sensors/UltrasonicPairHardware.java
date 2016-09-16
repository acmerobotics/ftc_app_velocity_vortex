package org.firstinspires.ftc.teamcode.hardware.sensors;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.control.Controller;
import org.firstinspires.ftc.teamcode.hardware.HardwareInterface;

/**
 * Created by Ryan on 12/9/2015.
 */
public class UltrasonicPairHardware extends HardwareInterface {

    private UltrasonicHardware us1, us2;

    private OpMode opMode;

    @Override
    public void init(OpMode mode) {
        this.opMode = mode;

        us1 = new UltrasonicHardware("us1");
        us2 = new UltrasonicHardware("us2");

        ((Controller) mode).registerHardwareInterface("us1", us1);
        ((Controller) mode).registerHardwareInterface("us2", us2);
    }

    @Override
    public String getStatusString() {
        return "diff: " + getDifference() + "  distance: " + getDistance();
    }

    public double getDistance() {
        return (us1.getDistance() + us2.getDistance()) / 2;
    }

    public double getDifference() {
        if (us1.getDistance() == 255 || us2.getDistance() == 255) {
            return 255;
        }
        return us1.getDistance() - us2.getDistance();
    }
}
