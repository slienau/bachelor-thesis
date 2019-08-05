package de.tuberlin.aot.thesis.slienau.algorithm;

public class Utils {
    public static double makePercent(double used, double total) {
        return round((used / total) * 100);
    }

    public static double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    /**
     * @param latency       in milliseconds
     * @param bitPerSecond  in bit/s
     * @param dataSizeKByte in KByte
     * @return transfer time in milliseconds
     */
    public static double calculateTransferTime(int latency, double bitPerSecond, double dataSizeKByte) {
        double dataSizeBit = dataSizeKByte * 1024 * 8; // KByte -> Byte -> bit
        double bitPerMillisecond = bitPerSecond / 1000; // bit/s -> bit/ms
        return round(latency + dataSizeBit / bitPerMillisecond);
    }

    /**
     * Converts Mbit to Bit
     *
     * @param mbit
     * @return
     */
    public static long mbitToBit(double mbit) {
        return (long) (mbit * Math.pow(10, 6));
    }
}
