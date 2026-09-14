package com.timecapsule.api.evaluation;

final class Haversine {

	private static final double EARTH_RADIUS_METERS = 6_371_000d;

	private Haversine() {
	}

	static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double deltaPhi = Math.toRadians(lat2 - lat1);
		double deltaLambda = Math.toRadians(lon2 - lon1);

		double a = Math.pow(Math.sin(deltaPhi / 2), 2)
				+ Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_METERS * c;
	}
}
