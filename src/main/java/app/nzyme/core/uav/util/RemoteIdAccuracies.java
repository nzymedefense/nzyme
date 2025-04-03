package app.nzyme.core.uav.util;

public class RemoteIdAccuracies {

    public static double verticalAccuracyToCursorOnTargetMeters(int accuracyCode) {
        return switch (accuracyCode) {
            case 1 -> 150;
            case 2 -> 45;
            case 3 -> 25;
            case 4 -> 10;
            case 5 -> 3;
            case 6 -> 1;
            default -> -1;
        };
    }

    public static double horizontalAccuracyToCursorOnTargetMeters(int accuracyCode) {
        return switch (accuracyCode) {
            case 1 -> 18520;
            case 2 -> 7408;
            case 3 -> 3704;
            case 4 -> 1852;
            case 5 -> 926;
            case 6 -> 556;
            case 7 -> 185;
            case 8 -> 93;
            case 9 -> 30;
            case 10 -> 10;
            case 11 -> 3;
            case 12 -> 1;
            default -> -1;
        };
    }

}
