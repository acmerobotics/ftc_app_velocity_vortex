package com.acmerobotics.velocityvortex.opmodes.tuner;

import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

/**
 * @author Ryan
 */

public abstract class Tuner extends OpMode {

    protected StickyGamepad stickyGamepad1;
    private int pos;

    @Override
    public void init() {
        stickyGamepad1 = new StickyGamepad(gamepad1);

        telemetry.log().add("Ready!");
    }

    @Override
    public void loop() {
        stickyGamepad1.update();

        PIDController controller = getController();
        DifferentialControlLoopCoefficients coefficients = controller.getCoefficients();

        if (stickyGamepad1.dpad_right) {
            pos++;
        } else if (stickyGamepad1.dpad_left) {
            pos--;
        }

        if (pos < 0) {
            pos += 3;
        } else if (pos >= 3) {
            pos -= 3;
        }

        double incr = 0.001;
        if (gamepad1.a) {
            incr = 0.01;
        }
        if (gamepad1.b) {
            incr = 0.1;
        }

        if (stickyGamepad1.dpad_up) {
            switch (pos) {
                case 0:
                    coefficients.p += incr;
                    break;
                case 1:
                    coefficients.i += incr;
                    break;
                case 2:
                    coefficients.d += incr;
                    break;
            }
        } else if (stickyGamepad1.dpad_down) {
            switch (pos) {
                case 0:
                    coefficients.p -= incr;
                    break;
                case 1:
                    coefficients.i -= incr;
                    break;
                case 2:
                    coefficients.d -= incr;
                    break;
            }
        }

        if (gamepad1.left_bumper && gamepad1.right_bumper) {
            controller.reset();
        }

        telemetry.clearAll();
        telemetry.addData("pid", controller.toString());
        telemetry.addData("pos", pos);
    }

    protected abstract PIDController getController();
}
