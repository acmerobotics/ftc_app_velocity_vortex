package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.control.RobotController;
import org.firstinspires.ftc.teamcode.hardware.drive.DriveHardware;

/**
 * Created by Admin on 9/21/2015.
 */
public class TankDrive extends RobotController {

    public void loop(DriveHardware driveHardware, Gamepad gamepad) {
        driveHardware.setMappedMotorSpeeds(-gamepad.left_stick_y, -gamepad.right_stick_y);
    }

}
