package de.tuberlin.aot.thesis.slienau.utils;

public class SchedulerUtils {

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

    /**
     * Converts Bit to Mbit
     *
     * @param bit
     * @return
     */
    public static double bitToMbit(double bit) {
        return bit / Math.pow(10, 6);
    }

    /**
     * @param cpuScore cpu benchmark score of executing cpu
     * @param time     time needed on executing cpu in milliseconds
     * @return
     */
    public static int calculateRequiredInstructionsForAppModule(int cpuScore, int time) {
        return (cpuScore * time) / 1000;
    }
}
