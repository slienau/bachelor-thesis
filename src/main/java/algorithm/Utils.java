package algorithm;

public class Utils {
    public static double makePercent(double used, double total) {
        return round((used / total) * 100);
    }

    public static double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
