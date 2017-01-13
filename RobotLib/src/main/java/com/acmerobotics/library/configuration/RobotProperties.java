package com.acmerobotics.library.configuration;

import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

public interface RobotProperties {
    DifferentialControlLoopCoefficients getTurnParameters();
    /** Get wheel radius in inches */
    double getWheelRadius();
}
