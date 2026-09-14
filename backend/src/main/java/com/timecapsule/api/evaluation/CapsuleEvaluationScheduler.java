package com.timecapsule.api.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CapsuleEvaluationScheduler {

	private static final Logger log = LoggerFactory.getLogger(CapsuleEvaluationScheduler.class);

	private final CapsuleEvaluationService capsuleEvaluationService;

	public CapsuleEvaluationScheduler(CapsuleEvaluationService capsuleEvaluationService) {
		this.capsuleEvaluationService = capsuleEvaluationService;
	}

	@Scheduled(fixedDelayString = "${timecapsule.evaluation.fixed-delay-ms:30000}")
	public void evaluateDueCapsules() {
		log.debug("Running scheduled capsule evaluation");
		capsuleEvaluationService.evaluateDueLockedCapsules();
	}
}
