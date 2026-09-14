package com.timecapsule.api.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineTest {

	@Test
	void knustSampleDistanceWithinExpectedRange() {
		double knustLat = 6.6745;
		double knustLon = -1.5712;
		double nearbyLat = 6.6755;
		double nearbyLon = -1.5700;

		double distance = Haversine.distanceMeters(nearbyLat, nearbyLon, knustLat, knustLon);
		assertTrue(distance > 100 && distance < 300);
	}
}
