package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.hardware.GyroSensor;

import org.firstinspires.ftc.teamcode.control.LinearRobotController;
import org.firstinspires.ftc.teamcode.hardware.drive.DriveHardware;
import org.firstinspires.ftc.teamcode.hardware.drive.SmartDriveHardware;

/**
 * Created by Ryan on 12/6/2015.
 */
public class GyroTurnTest extends LinearRobotController {

    public SmartDriveHardware smartDriveHardware;
    public GyroSensor gyroHardware;
    public DriveHardware driveHardware;

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        gyroHardware =  hardwareMap.gyroSensor.get("gyro");

        driveHardware = new DriveHardware();
        registerHardwareInterface("drive", driveHardware);
        smartDriveHardware = new SmartDriveHardware(driveHardware, gyroHardware);
        registerHardwareInterface("gyro_drive", smartDriveHardware);

        waitForStart();

        while(true) {
            waitMillis(5000);

            smartDriveHardware.turnLeftSync(90);
        }
    }
}
