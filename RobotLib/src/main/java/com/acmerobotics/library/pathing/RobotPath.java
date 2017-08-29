package com.acmerobotics.library.pathing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kelly on 8/23/2017.
 *
 */

public class RobotPath {

    private List<RobotPose> poses;

    public RobotPath (List<RobotPose> poses) {
        this.poses = poses;
    }

    public RobotPath (RobotPose[] poses) {
        this.poses = Arrays.asList(poses);
    }


    //timestamp, x, y, heading
    public RobotPath (String path) throws FileNotFoundException, IOException{
        poses = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("'");
            poses.add(new RobotPose(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]), Long.parseLong(tokens[0])));
        }

    }

    public List<RobotPose> getPoses () {
        return poses;
    }

    public RobotPose getPose (int index) {
        return poses.get(index);
    }

    public void addPose (RobotPose pose) {
        poses.add(pose);
    }

    public void addPose (RobotPose pose, int i) {
        poses.add(i, pose);
    }

    public double numPoses () {
        return poses.size();
    }

    public double length () {
        double length = 0;
        for (int i = 1; i < poses.size(); i++) {
            length += Math.hypot(poses.get(i).getX() - poses.get(i-1).getX(), Math.hypot(poses.get(i).getY(), poses.get(i-1).getY()));
        }
        return length;
    }

    public double length (int i, int il) {
        return new RobotPath(poses.subList(i, il)).length();
    }

    public RobotPose takeFirst () {
        return poses.remove(0);
    }

}