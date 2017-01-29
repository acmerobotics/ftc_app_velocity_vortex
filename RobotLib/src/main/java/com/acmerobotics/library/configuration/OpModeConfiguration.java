package com.acmerobotics.library.configuration;

import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

public class OpModeConfiguration {

    private static final RobotProperties SOFTWARE_BOT = new RobotProperties() {
        @Override
        public DifferentialControlLoopCoefficients getTurnParameters() {
            return new DifferentialControlLoopCoefficients(0.02, 0, 0);
        }

        @Override
        public double getWheelRadius() {
            return 2;
        }

        @Override
        public double getRobotSize() {
            return 18;
        }

        @Override
        public double getSonarSensorOffset() {
            return 7.625;
        }
    };

    private static final RobotProperties COMP_BOT = new RobotProperties() {
        @Override
        public DifferentialControlLoopCoefficients getTurnParameters() {
            return new DifferentialControlLoopCoefficients(0.035, 0, 0);
        }

        @Override
        public double getWheelRadius() {
            return 2;
        }

        @Override
        public double getRobotSize() {
            return 18;
        }

        @Override
        public double getSonarSensorOffset() {
            return 2.5;
        }
    };

    private static final String PREFS_NAME = "opmode";
    private static final String PREF_ALLIANCE_COLOR = "alliance_color";
    private static final String PREF_PARK_DEST = "park_dest";
    private static final String PREF_DELAY = "delay";
    private static final String PREF_NUM_BALLS = "num_balls";
    private static final String PREF_ROBOT_TYPE = "robot_type";
    private static final String PREF_LAST_HEADING = "last_heading";

    public enum AllianceColor {
        RED(0),
        BLUE(1);
        private int index;
        AllianceColor(int i) {
            index = i;
        }
        public int getIndex() {
            return index;
        }
        public static AllianceColor fromIndex(int i) {
            for (AllianceColor color : AllianceColor.values()) {
                if (color.getIndex() == i) {
                    return color;
                }
            }
            return null;
        }
    }

    public enum RobotType {
        COMPETITION(0, COMP_BOT),
        SOFTWARE(1, SOFTWARE_BOT);
        private int index;
        private RobotProperties props;
        RobotType(int i, RobotProperties p) {
            index = i;
            props = p;
        }
        public int getIndex() {
            return index;
        }
        public RobotProperties getProperties() {
            return props;
        }
        public static RobotType fromIndex(int i) {
            for (RobotType type : RobotType.values()) {
                if (type.getIndex() == i) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum ParkDest {
        NONE (0),
        CENTER (1),
        CORNER (2);
        private int index;
        ParkDest (int i) {
            index = i;
        }
        public int getIndex () { return index; }
        public static ParkDest fromIndex (int i) {
            for (ParkDest destination : ParkDest.values()) {
                if (destination.getIndex() == i )
                    return destination;
            }
            return null;
        }
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public OpModeConfiguration(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public AllianceColor getAllianceColor() {
        return AllianceColor.fromIndex(preferences.getInt(PREF_ALLIANCE_COLOR, 0));
    }

    public void setAllianceColor(AllianceColor color) {
        editor.putInt(PREF_ALLIANCE_COLOR, color.getIndex());
    }

    public ParkDest getParkDest() {
        return ParkDest.fromIndex(preferences.getInt(PREF_PARK_DEST, 0));
    }

    public void setParkDest(ParkDest dest) {
        editor.putInt(PREF_PARK_DEST, dest.getIndex());
    }

    public int getDelay() {
        return preferences.getInt(PREF_DELAY, 0);
    }

    public void setDelay(int delay) {
        if (delay >= 0 && delay <= 30) {
            editor.putInt(PREF_DELAY, delay);
        }
    }

    public int getNumberOfBalls() {
        return preferences.getInt(PREF_NUM_BALLS, 0);
    }

    public void setNumberOfBalls(int numBalls) {
        if (numBalls > 2) return;
        editor.putInt(PREF_NUM_BALLS, numBalls);
    }

    public RobotType getRobotType() {
        return RobotType.fromIndex(preferences.getInt(PREF_ROBOT_TYPE, 0));
    }

    public void setRobotType(RobotType type) {
        editor.putInt(PREF_ROBOT_TYPE, type.getIndex());
    }

    public double getLastHeading() {
        return preferences.getFloat(PREF_LAST_HEADING, 0);
    }

    public void setLastHeading(double heading) {
        editor.putFloat(PREF_LAST_HEADING, (float) heading);
    }

    public boolean commit() {
        return editor.commit();
    }

}
