package org.firstinspires.ftc.teamcode.opmodes.teleop;

/**
 * Created by Admin on 1/20/2016.
 */
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOp (No Limits)")
public class TeleOpNoLimits extends TeleOp {
    @Override
    public void init() {
        super.init();
        this.armHardware.setConstrained(false);
    }
}
