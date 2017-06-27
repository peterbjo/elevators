package com.tingco.codechallenge.elevator.api;

import com.tingco.codechallenge.elevator.domain.StopEvent;

/**
 * @author Peter Björklund <mailto:peter.bjorklund@joors.com>
 * @since 1.0.0
 */
public interface ElevatorListener {
    /**
     * Triggers when an elevator stops
     * @param event the StopEvent
     */
    void onStopEvent(StopEvent event);
}
