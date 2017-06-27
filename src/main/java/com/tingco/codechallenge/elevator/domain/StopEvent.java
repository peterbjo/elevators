package com.tingco.codechallenge.elevator.domain;

import com.tingco.codechallenge.elevator.api.Elevator;

/**
 * @author Peter Björklund <mailto:peter.bjorklund@joors.com>
 * @since 1.0.0
 */
public class StopEvent {

    private final Integer floor;
    private final Elevator elevator;

    public StopEvent(Integer floor, Elevator elevator){
        this.floor = floor;
        this.elevator = elevator;
    }

    public Integer getFloor() {
        return floor;
    }

    public Elevator getElevator() {
        return elevator;
    }
}
