package de.tuberlin.aot.thesis.slienau.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Random;

public class NumberUtils {
    private static final Random rand = new Random();

    public static int getRandom(int lowerBound, int upperBound) {
        if (upperBound < lowerBound)
            throw new IllegalArgumentException("upper bound must be higher than lower bound");
        return rand.nextInt((upperBound + 1 - lowerBound)) + lowerBound;
    }

    public static double stringToDouble(String inputString) throws ParseException {
        inputString = inputString.replace(".", ",");
        NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
        Number number = format.parse(inputString);
        return number.doubleValue();
    }
}
