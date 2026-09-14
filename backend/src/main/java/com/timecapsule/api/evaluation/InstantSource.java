package com.timecapsule.api.evaluation;

import java.time.Instant;

@FunctionalInterface
public interface InstantSource {

	Instant now();

	static InstantSource systemUtc() {
		return Instant::now;
	}
}
