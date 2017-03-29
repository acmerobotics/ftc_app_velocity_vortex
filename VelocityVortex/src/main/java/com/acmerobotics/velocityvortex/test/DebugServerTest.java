package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.network.RobotDebugServer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ryan
 */

@Autonomous(name="DebugServerTest")
public class DebugServerTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        RobotDebugServer server = RobotDebugServer.getInstance();

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.a) {
                JSONObject object = new JSONObject();
                try {
                    object.put("time", System.currentTimeMillis());
                    object.put("button", "a");
                } catch (JSONException e) {

                }
                server.send(object);
            }
        }
    }
}
