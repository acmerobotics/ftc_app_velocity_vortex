package com.acmerobotics.velocityvortex.opmodes;

/**
 * @author Ryan
 */

public class Util {

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
