package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.OpModeConfiguration;

import org.junit.Test;

import java.util.Arrays;

import static com.acmerobotics.velocityvortex.opmodes.Auto.TILE_SIZE;
import static com.acmerobotics.velocityvortex.opmodes.BeaconAuto.WALL_DISTANCE;

/**
 * @author Ryan
 */

public class LinearPathExecutorTest {

//    private double halfWidth = 18;
//
//    @Test
//    public void testFieldNavigator() {
//        System.out.println("Field Nav");
//        HolonomicDrive drive = new MockHolonomicDrive();
//        FieldNavigator nav = new FieldNavigator(drive, OpModeConfiguration.AllianceColor.RED);
//        nav.setLocation(2.5 * TILE_SIZE, halfWidth);
//        nav.moveTo(2.5 * TILE_SIZE, TILE_SIZE + halfWidth - 6, null);
//        nav.moveTo(WALL_DISTANCE + halfWidth, 2.25 * TILE_SIZE, 180, null);
//        System.out.println();
//    }
//
//    @Test
//    public void testPathExecutor() {
//        System.out.println("Path Executor");
//        HolonomicDrive drive = new MockHolonomicDrive();
//        LinearPath path = new LinearPath(Arrays.asList(
//                new LinearPath.Waypoint(2.5 * TILE_SIZE, halfWidth),
//                new LinearPath.Waypoint(2.5 * TILE_SIZE, TILE_SIZE + halfWidth - 6),
//                new LinearPath.Waypoint(WALL_DISTANCE + halfWidth, 2.25 * TILE_SIZE, 180)
//        ));
//        LinearPathExecutor executor = new LinearPathExecutor(path, drive);
//        executor.execute(2, null);
//        System.out.println();
//    }

    @Test
    public void testBasicPathExecutor() {
        System.out.println("Basic Test");
        HolonomicDrive drive = new MockHolonomicDrive();
        LinearPath path = new LinearPath(Arrays.asList(
                new LinearPath.Waypoint(0, 0),
                new LinearPath.Waypoint(2, 2),
                new LinearPath.Waypoint(-2, 2, 0)
        ));
        LinearPathExecutor executor = new LinearPathExecutor(drive, path);
        executor.executeAll(null);

        System.out.println();
        System.out.println("Basic Test2");
        drive = new MockHolonomicDrive();
        path = new LinearPath(Arrays.asList(
                new LinearPath.Waypoint(0, 0),
                new LinearPath.Waypoint(2, 2),
                new LinearPath.Waypoint(-2, 2, 0)
        ));
        executor = new LinearPathExecutor(drive, path, true);
        executor.executeAll(null);
    }

}
