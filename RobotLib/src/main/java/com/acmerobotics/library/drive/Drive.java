package com.acmerobotics.library.drive;

/**
 * Created by kelly on 8/25/2017.
 *
 */

public interface Drive {

    double getVelocity();

    double getHeading();

    void move (double v, double omega);

}
