package com.acmerobotics.velocityvortex.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * @author Ryan
 */

@TeleOp(name="Demo",group="TeleOp")
public class DemoTeleOp extends MainTeleOp {

    public DemoTeleOp() {
        super(Mode.DEMO);
    }

}
