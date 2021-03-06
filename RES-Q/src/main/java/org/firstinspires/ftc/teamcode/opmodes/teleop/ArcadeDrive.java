package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.hardware.drive.DriveHardware;

/**
 * Created by Admin on 9/24/2015.
 */

public class ArcadeDrive {

    public void loop(DriveHardware driveHardware, Gamepad gamepad) {
        double leftRight = -gamepad.right_stick_x;
        double forwardBack = -gamepad.left_stick_y;
        
        double rightSpeed = 0;
        double leftSpeed = 0;
        
        if (Math.abs(forwardBack) > 0.05) {
            rightSpeed = forwardBack;
            leftSpeed = forwardBack;
        }
        if (Math.abs(leftRight) > 0.05) {
            rightSpeed += leftRight;
            leftSpeed -= leftRight;
        }
        
        driveHardware.setMappedMotorSpeeds(leftSpeed, rightSpeed);
    }

}
