package app.nzyme.core.geo;

import java.util.List;

public final class GeoCenter {

    public static final class LatLon {
        public final double latDeg;
        public final double lonDeg;
        public LatLon(double latDeg, double lonDeg) {
            this.latDeg = latDeg;
            this.lonDeg = lonDeg;
        }
        @Override public String toString() {
            return String.format("Lat %.6f°, Lon %.6f°", latDeg, lonDeg);
        }
    }

    public static LatLon center(List<LatLon> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("At least one coordinate is required.");
        }
        if (points.size() == 1) return points.get(0);

        double x = 0, y = 0, z = 0;

        for (LatLon p : points) {
            double lat = Math.toRadians(p.latDeg);
            double lon = Math.toRadians(p.lonDeg);
            double clat = Math.cos(lat);
            x += clat * Math.cos(lon);
            y += clat * Math.sin(lon);
            z += Math.sin(lat);
        }

        int n = points.size();
        x /= n; y /= n; z /= n;

        // Avoid zero-length vector issues.
        if (x == 0 && y == 0 && z == 0) {
            // Fallback: use simple average of degrees. (for antipodal/symmetric cases)
            double avgLat = points.stream().mapToDouble(p -> p.latDeg).average().orElse(0);
            double avgLon = points.stream().mapToDouble(p -> p.lonDeg).average().orElse(0);
            return new LatLon(avgLat, normalizeLonDegrees(avgLon));
        }

        double lon = Math.atan2(y, x);
        double hyp = Math.sqrt(x*x + y*y);
        double lat = Math.atan2(z, hyp);

        return new LatLon(Math.toDegrees(lat), normalizeLonDegrees(Math.toDegrees(lon)));
    }

    private static double normalizeLonDegrees(double lonDeg) {
        // Normalize to (-180, 180]
        double lon = ((lonDeg + 180) % 360 + 360) % 360 - 180;
        if (lon == -180) lon = 180;
        return lon;
    }

}