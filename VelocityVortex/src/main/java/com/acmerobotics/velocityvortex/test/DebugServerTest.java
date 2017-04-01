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

        long lastTime = System.currentTimeMillis();

        int i = 0;

        while (opModeIsActive()) {
            JSONObject object = new JSONObject();
            try {
                long time = System.currentTimeMillis();
                object.put("time", time);
                object.put("loop", time - lastTime);
                lastTime = time;
                if (gamepad1.a) {
                    object.put("button", "a");
                }
            } catch (JSONException e) {

            }
            if (i % 100 == 0) server.send(object);
            i++;
        }
    }
}
