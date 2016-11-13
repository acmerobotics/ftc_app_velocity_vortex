package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name="Beacon Auto")
public class BeaconAuto extends OpMode {

    /**
     * Enum to hold the autonomous FSM states.
     * TODO: Is this the best way to implement FSMs in Java?
     */
    public enum State {
        BEGIN,
        NAVIGATE_BEACON1,
        ANALYZE_BEACON1,
        NAVIGATE_BEACON2,
        ANALYZE_BEACON2,
        END
    }

    private VuforiaInterface vuforia;
    private MecanumDrive mecanumDrive;
    private State currentState;
    private OpModeConfiguration configuration;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);

        vuforia = new VuforiaInterface("vuforia", 0);

        mecanumDrive = new MecanumDrive(hardwareMap);

        currentState = State.BEGIN;
    }

    @Override
    public void init_loop() {
        telemetry.addData("alliance_color", configuration.getAllianceColor());
        telemetry.addData("delay", configuration.getDelay());
    }

    @Override
    public void loop() {
        telemetry.addData("state", currentState.toString());
        switch (currentState) {
            case BEGIN:
                break;
            case NAVIGATE_BEACON1:
                /**
                 * Compute the vector between the robot and the center of the tile in front of the
                 * first beacon. Rescale this vector to the proper magnitude and have the robot
                 * travel in that direction. Once we've gotten close; it may be useful to use the
                 * line following sensor to align fully.
                 */
                break;
            case ANALYZE_BEACON1:
                /**
                 * Analyze the beacon and press the appropriate button. This will likely need to be
                 * split into various subtasks.
                 */
                break;
            case NAVIGATE_BEACON2:
                /**
                 * Strafe horizontally from our current position until the line following array (or
                 * some other sensor) detects that we have reached the other beacon line.
                 */
                break;
            case ANALYZE_BEACON2:
                /**
                 * Analyze the beacon and press the appropriate like {@link State#ANALYZE_BEACON1}.
                 * These two tasks should share a lot of code.
                 */
                break;
            case END:
                break;
        }
    }
}
