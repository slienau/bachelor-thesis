package algorithm;

public class Utils {
    public static double makePercent(double used, double total) {
        return round((used / total) * 100);
    }

    public static double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    /**
     * @param latency                in milliseconds
     * @param bandwidthBitsPerSecond in bit/s
     * @param dataSizeKByte          in KByte
     * @return transfer time in milliseconds
     */
    public static double calculateTransferTime(int latency, double bandwidthBitsPerSecond, double dataSizeKByte) {
        double dataSize_bits = dataSizeKByte * 1024 * 8; // KByte -> Byte -> bit
        double bandwidth_bits_per_ms = bandwidthBitsPerSecond / 1000; // bits/s -> bits/ms
//        System.out.println(String.format("Transfer time for %skb with %sMbit/s and latency of %sms: %sms", dataSizeKByte, bandwidthBitsPerSecond / 1000000, latency, (latency + dataSize_bits / bandwidth_bits_per_ms)));
        return round(latency + dataSize_bits / bandwidth_bits_per_ms);
    }
}
