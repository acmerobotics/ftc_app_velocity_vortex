package com.acmerobotics.velocityvortex.localization;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;

/**
 * Base class for interfaces to different localization methods, used by {@see Localizer}
 * Created by kelly on 10/23/2016.
 */
public abstract class LocalizationInterface {

    /**
     * Priority of LocalizationInterface, used for determining which takes precedent in case of contradiction</p>
     */
    public int priority;

    /**
     * Name used for debugging
     */
    public String name;

    /**
     * location of robot at last update, null if unknown
     */
    protected RobotLocation location;

    /**
     * Create LocalizationInterface with the robot at a specified position
     * @param name
     * @param priority
     * @param location
     */
    public LocalizationInterface(String name, int priority, RobotLocation location) {
        this.name = name;
        this.priority = priority;
        this.location = location;
    }

    public LocalizationInterface(String name, int priority, OpenGLMatrix location) {
        this(name, priority, RobotLocation.matrixToLocation(location));
    }

    public LocalizationInterface(String name, int priority) {
        this.name = name;
        this.priority = priority;
        this.location = null;
    }

    public void setLocation(RobotLocation location) {
        this.location = location;
    }

    public void setLocation(OpenGLMatrix locationGL) {
        setLocation(RobotLocation.matrixToLocation(locationGL));
    }

    public void update() {

    }

    public RobotLocation getLocation() {
        return location;
    }
}
