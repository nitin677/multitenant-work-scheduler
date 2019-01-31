package com.example.platform.mt.vo;

/**
 * Work scheduling strategy employed by the work queue manager to schedule tenant tasks.
 */
public enum SchedulingStrategy {
    /**
     * Fair queueing strategy.
     * https://en.wikipedia.org/wiki/Fair_queuing
     */
    FAIR_QUEUEING,
    /**
     * Weighted fair queueing strategy.
     * https://en.wikipedia.org/wiki/Weighted_fair_queueing
     */
    WEIGHTED_FAIR_QUEUEING;
}
