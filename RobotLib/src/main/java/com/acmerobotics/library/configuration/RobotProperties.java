package com.acmerobotics.library.configuration;

import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

public interface RobotProperties {
    DifferentialControlLoopCoefficients getTurnParameters();
    WheelType[] getWheelTypes();
    /** Get robot size in inches */
    double getRobotSize();
    double getDistanceSensorOffset();

}
