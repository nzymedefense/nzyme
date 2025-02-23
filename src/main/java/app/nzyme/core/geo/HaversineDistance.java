package app.nzyme.core.geo;

public class HaversineDistance {

    private static final double EARTH_RADIUS_METERS = 6371000;

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians.
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate differences in radians.
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Apply the Haversine formula.
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Return the distance in meters.
        return EARTH_RADIUS_METERS * c;
    }

}
