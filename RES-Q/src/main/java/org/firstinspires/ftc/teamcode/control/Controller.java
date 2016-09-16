package org.firstinspires.ftc.teamcode.control;

import org.firstinspires.ftc.teamcode.hardware.HardwareInterface;

/**
 * Created by Admin on 1/28/2016.
 */
public interface Controller {
    public boolean registerHardwareInterface(String name, HardwareInterface hi);
    public boolean deregisterHardwareInterface(String name);
}
