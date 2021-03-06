package com.acmerobotics.velocityvortex.drive;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * @author Ryan
 */

public class Vector2dTest {

    @Test
    public void testVectorAdd() {
        Vector2d v = new Vector2d(0, 1);
        v.add(new Vector2d(2, 3));
        assertTrue("Vector addition doesn't work properly", v.equals(new Vector2d(2, 4)));
    }

    @Test
    public void testScalarMultiply() {
        Vector2d v = new Vector2d(0, 3);
        v.multiply(4);
        assertTrue("Scalar multiplication doesn't work properly", v.equals(new Vector2d(0, 12)));
    }

    @Test
    public void testNorm() {
        Vector2d v = new Vector2d(1, 4);
        assertEquals("Norm doesn't work properly", Math.hypot(1, 4), v.norm());
    }

    @Test
    public void testDotProduct() {
        Vector2d a = new Vector2d(2, 5);
        Vector2d b = new Vector2d(3, -1);
        assertEquals("Dot product doesn't work properly", 1, a.dot(b), 0.0001);
    }

    @Test
    public void testEqualReflexivity() {
        Vector2d v = new Vector2d(3, 4);
        assertTrue("Vector equality is not reflexive", v.equals(v));
    }

}
