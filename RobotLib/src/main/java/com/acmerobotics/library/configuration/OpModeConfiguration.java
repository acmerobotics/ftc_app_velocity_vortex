package com.acmerobotics.library.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

import org.opencv.core.Mat;

public class OpModeConfiguration {

    private static final RobotProperties SOFTWARE_BOT = new RobotProperties() {
        @Override
        public DifferentialControlLoopCoefficients getTurnParameters() {
            return new DifferentialControlLoopCoefficients(0.02, 0, 0);
        }

        @Override
        public DifferentialControlLoopCoefficients getWallParameters() {
            return new DifferentialControlLoopCoefficients(0, 0, 0);
        }

        @Override
        public WheelType[] getWheelTypes() {
            return new WheelType[] {
                    new WheelType(MotorType.ANDYMARK_60, DcMotorSimple.Direction.FORWARD, 1, 2),
                    new WheelType(MotorType.ANDYMARK_60, DcMotorSimple.Direction.FORWARD, 1, 2),
                    new WheelType(MotorType.ANDYMARK_60, DcMotorSimple.Direction.FORWARD, 1, 2),
                    new WheelType(MotorType.ANDYMARK_60, DcMotorSimple.Direction.FORWARD, 1, 2)
            };
        }

        @Override
        public double getRobotSize() {
            return 18;
        }

        @Override
        public double getDistanceSensorOffset() {
            return 7.625;
        }
    };

    private static final RobotProperties COMP_BOT = new RobotProperties() {
        @Override
        public DifferentialControlLoopCoefficients getTurnParameters() {
            return new DifferentialControlLoopCoefficients(0.025, 0, 0);
        }

        @Override
        public DifferentialControlLoopCoefficients getWallParameters() {
            return new DifferentialControlLoopCoefficients(0.095, 0, 0);
        }

        @Override
        public WheelType[] getWheelTypes() {
            return new WheelType[] {
                    new WheelType(MotorType.ANDYMARK_20, DcMotorSimple.Direction.REVERSE, 1, 2),
                    new WheelType(MotorType.ANDYMARK_20, DcMotorSimple.Direction.FORWARD, 1, 2),
                    new WheelType(MotorType.ANDYMARK_20, DcMotorSimple.Direction.REVERSE, 1, 2),
                    new WheelType(MotorType.ANDYMARK_20, DcMotorSimple.Direction.FORWARD, 1, 2)
            };
        }

        @Override
        public double getRobotSize() {
            return 18;
        }

        @Override
        public double getDistanceSensorOffset() {
            return 2.5;
        }
    };

    private static final String PREFS_NAME = "opmode";
    private static final String PREF_ALLIANCE_COLOR = "alliance_color";
    private static final String PREF_PARK_DEST = "park_dest";
    private static final String PREF_DELAY = "delay";
    private static final String PREF_NUM_BALLS = "num_balls";
    private static final String PREF_MATCH_TYPE = "match_type";
    private static final String PREF_MATCH_NUMBER = "match_number";
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
        SOFTWARE(1, SOFTWARE_BOT),
        UNKNOWN(2, null);
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

    public enum MatchType {
        PRACTICE (0),
        QUALIFYING (1),
        SEMIFINAL (2),
        FINAL (3);
        private int index;
        MatchType(int i) {
            index = i;
        }
        public int getIndex() {
            return index;
        }
        public static MatchType fromIndex(int i) {
            for (MatchType type : MatchType.values()) {
                if (type.getIndex() == i) {
                    return type;
                }
            }
            return null;
        }
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public OpModeConfiguration(Context context) {
        this.context = context;
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
        String activeConfigName = getActiveConfigName();
        if (activeConfigName.equals("software")) {
            return RobotType.SOFTWARE;
        } else if (activeConfigName.equals("competition")) {
            return RobotType.COMPETITION;
        } else {
            return RobotType.UNKNOWN;
        }
    }

    public MatchType getMatchType() {
        return MatchType.fromIndex(preferences.getInt(PREF_MATCH_TYPE, 0));
    }

    public void setMatchType(MatchType type) {
        editor.putInt(PREF_MATCH_TYPE, type.getIndex());
    }

    public int getMatchNumber() {
        return preferences.getInt(PREF_MATCH_NUMBER, 1);
    }

    public void setMatchNumber(int num) {
        editor.putInt(PREF_MATCH_NUMBER, num);
    }

    public double getLastHeading() {
        return preferences.getFloat(PREF_LAST_HEADING, 0);
    }

    public void setLastHeading(double heading) {
        editor.putFloat(PREF_LAST_HEADING, (float) heading);
    }

    public String getActiveConfigName() {
        RobotConfigFileManager manager = new RobotConfigFileManager((Activity) context);
        return manager.getActiveConfig().getName();
    }

    public boolean commit() {
        return editor.commit();
    }

}
