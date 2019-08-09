package de.tuberlin.aot.thesis.slienau.utils;

import java.util.Random;

public class NumberUtils {
    private static final Random rand = new Random();

    public static int getRandom(int lowerBound, int upperBound) {
        if (upperBound < lowerBound)
            throw new IllegalArgumentException("upper bound must be higher than lower bound");
        return rand.nextInt((upperBound + 1 - lowerBound)) + lowerBound;
    }
}
